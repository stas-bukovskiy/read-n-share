package httpcontroller

import (
	"context"
	"encoding/json"
	"github.com/gin-gonic/gin"
	"github.com/gorilla/websocket"
	"github.com/stas-bukovskiy/read-n-share/book-reader-api/internal/entity"
	"github.com/stas-bukovskiy/read-n-share/book-reader-api/internal/service"
	"github.com/stas-bukovskiy/read-n-share/book-reader-api/pkg/errs"
	"net/http"
)

type wsRoutes struct {
	routerContext
	upgrader websocket.Upgrader
}

func setupWSRoutes(options routerOptions) wsRoutes {
	wsRoutes := wsRoutes{
		routerContext: routerContext{
			services: options.services,
			cfg:      options.cfg,
			logger:   options.logger.Named("wsRoutes"),
		},
		upgrader: websocket.Upgrader{
			CheckOrigin: func(r *http.Request) bool {
				return true
			},
		},
	}

	group := options.router.Group("/ws")
	group.Use(newAuthMiddleware(options))
	{
		group.GET("/reader", errorHandler(options, wsRoutes.reader))
	}

	return wsRoutes
}

type readerRequestQuery struct {
	BookID string `form:"bookID" binding:"required"`
}

func (w *wsRoutes) reader(c *gin.Context) (interface{}, *httpErr) {
	logger := w.logger.Named("reader").WithContext(c.Request.Context())

	var query readerRequestQuery
	err := c.ShouldBindQuery(&query)
	if err != nil {
		logger.Info("failed to bind query", "err", err)
		return nil, &httpErr{
			Type:    httpErrTypeClient,
			Message: "invalid request",
			Details: err.Error(),
		}
	}

	conn, err := w.upgrader.Upgrade(c.Writer, c.Request, nil)
	if err != nil {
		logger.Error("failed to upgrade connection", "err", err)
		return nil, &httpErr{
			Type:    httpErrTypeServer,
			Message: "failed to upgrade connection",
			Details: err.Error(),
		}
	}
	defer conn.Close()

	updatedChan, bs, err := w.services.BookSync.Connect(c, c.Value("userID").(string), query.BookID)
	if err != nil {
		logger.Error("failed to connect to book sync", "err", err)
		return nil, &httpErr{
			Type:    httpErrTypeServer,
			Message: "failed to connect to book sync",
			Details: err.Error(),
		}
	}
	defer func(BookSync service.BookSyncService, ctx context.Context, userID, bookID string) {
		err := BookSync.Close(ctx, userID, bookID)
		if err != nil {
			logger.Error("failed to close book sync connection", "err", err)
		}
	}(w.services.BookSync, c, c.Value("userID").(string), query.BookID)

	// send initial message
	err = conn.WriteJSON(bs)
	if err != nil {
		logger.Error("failed to write message", "err", err)
	}

	go func(updatedChan chan *entity.BookSync) {
		for {
			msg, ok := <-updatedChan
			if !ok {
				logger.Info("channel closed")
				return
			}
			err := conn.WriteJSON(msg)
			if err != nil {
				logger.Error("failed to write message", "err", err)
				return
			}
		}
	}(updatedChan)

ReadMessagesLoop:
	for {
		msgType, data, err := conn.ReadMessage()
		if err != nil {
			logger.Error("failed to read message", "err", err)
			return nil, &httpErr{
				Type:    httpErrTypeServer,
				Message: "failed to read message",
				Details: err.Error(),
			}
		}
		logger.Info("received message", "msgType", msgType, "data", string(data))

		switch msgType {
		case websocket.PingMessage:
			logger.Info("received ping message")
			err = conn.WriteMessage(websocket.PongMessage, nil)
			continue ReadMessagesLoop
		case websocket.BinaryMessage, websocket.TextMessage:
			logger.Info("received binary/text message")

			msg := &entity.BookUserSettings{}
			marshalErr := json.Unmarshal(data, msg)
			if marshalErr != nil {
				logger.Info("failed to unmarshal message", "err", err, "data", string(data))
				continue ReadMessagesLoop
			}

			msg.UserID = c.Value("userID").(string)
			logger.Info("received message", "msg", msg)
			if msg.BookID == "" {
				logger.Info("bookID is empty")
				continue ReadMessagesLoop
			}

			err = w.services.BookSync.Receive(c, msg)
			if err != nil {
				if errs.IsExpected(err) {
					logger.Info("failed to receive message", "err", err)
					continue ReadMessagesLoop
				}
				logger.Error("failed to receive message", "err", err)
				return nil, &httpErr{
					Type:    httpErrTypeServer,
					Message: "failed to receive message",
					Details: err.Error(),
				}
			}

		case websocket.CloseMessage:
			logger.Info("closing connection")
			return nil, nil
		default:
			logger.Info("unknown message type", "msgType", msgType)
			continue ReadMessagesLoop
		}

	}
}
