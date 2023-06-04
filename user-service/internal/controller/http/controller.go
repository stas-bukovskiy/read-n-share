package httpcontroller

import (
	"bytes"
	"fmt"
	"github.com/DataDog/gostackparse"
	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
	"github.com/stas-bukovskiy/read-n-share/user-service/config"
	"github.com/stas-bukovskiy/read-n-share/user-service/internal/service"
	"github.com/stas-bukovskiy/read-n-share/user-service/pkg/logging"
	"net/http"
	"runtime/debug"
)

type Options struct {
	Services service.Services
	Config   *config.Config
	Logger   logging.Logger
}

type routerContext struct {
	services service.Services
	cfg      *config.Config
	logger   logging.Logger
}

type routerOptions struct {
	router   *gin.RouterGroup
	services service.Services
	cfg      *config.Config
	logger   logging.Logger
}

func New(options Options) http.Handler {
	handler := gin.New()

	handler.Use(gin.Logger(), gin.Recovery(), requestIDMiddleware, corsMiddleware)

	// init router options
	routerOptions := routerOptions{
		router:   handler.Group("/api/v1"),
		services: options.Services,
		logger:   options.Logger.Named("HTTPController"),
		cfg:      options.Config,
	}

	routerOptions.router.GET("/ping", func(c *gin.Context) { c.Status(http.StatusOK) })

	setupUserRoutes(routerOptions)
	setupAuthRoutes(routerOptions)

	return handler
}

type httpErr struct {
	Type          httpErrType `json:"-"`
	Message       string      `json:"message"`
	Code          string      `json:"code,omitempty"`
	Details       interface{} `json:"details,omitempty"`
	InvalidFields interface{} `json:"invalidFields,omitempty"`
}

type httpErrType string

const (
	httpErrTypeServer httpErrType = "server"
	httpErrTypeClient httpErrType = "client"
)

func (e httpErr) Error() string {
	return fmt.Sprintf("%s: %s", e.Type, e.Message)
}

// errorHandler provides unified error handling for all handlers.
func errorHandler(options routerOptions, handler func(c *gin.Context) (interface{}, *httpErr)) gin.HandlerFunc {
	return func(c *gin.Context) {
		logger := options.logger.Named("wrapHandler")

		// Handle panics
		defer func() {
			if err := recover(); err != nil {
				stacktrace, errors := gostackparse.Parse(bytes.NewReader(debug.Stack()))
				if len(errors) > 0 || len(stacktrace) == 0 {
					logger.Error("get stacktrace errors", "stacktraceErrors", errors, "stacktrace", "unknown", "err", err)
				} else {
					logger.Error("unhandled error", "err", err, "stacktrace", stacktrace)
				}

				err := c.AbortWithError(http.StatusInternalServerError, fmt.Errorf("%v", err))
				if err != nil {
					logger.Error("failed to abort with error", "err", err)
				}
			}
		}()

		body, err := handler(c)

		// Check if middleware
		if body == nil && err == nil {
			return
		}
		logger = logger.With("body", body).With("err", err)

		if err != nil {
			if err.Type == httpErrTypeServer {
				logger.Error("internal server error")
				c.AbortWithStatusJSON(http.StatusInternalServerError, err)
			} else {
				logger.Info("client error")
				c.AbortWithStatusJSON(http.StatusUnprocessableEntity, err)
			}
			return
		}
		logger.Info("request handled")
		c.JSON(http.StatusOK, body)
	}
}

// corsMiddleware - used to allow incoming cross-origin requests.
func corsMiddleware(c *gin.Context) {
	c.Header("Access-Control-Allow-Origin", "*")
	c.Header("Access-Control-Allow-Methods", "*")
	c.Header("Access-Control-Allow-Headers", "*")
	c.Header("Content-Type", "application/json")

	if c.Request.Method != "OPTIONS" {
		c.Next()
	} else {
		c.AbortWithStatus(http.StatusOK)
	}
}

// requestIDMiddleware is used to add request id to gin context.
func requestIDMiddleware(c *gin.Context) {
	c.Set("RequestID", uuid.NewString())
}
