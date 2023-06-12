package httpcontroller

import (
	"bytes"
	"fmt"
	"github.com/DataDog/gostackparse"
	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
	"github.com/stas-bukovskiy/read-n-share/book-reader-api/config"
	"github.com/stas-bukovskiy/read-n-share/book-reader-api/internal/service"
	"github.com/stas-bukovskiy/read-n-share/book-reader-api/pkg/errs"
	"github.com/stas-bukovskiy/read-n-share/book-reader-api/pkg/logging"
	"net/http"
	"runtime/debug"
	"strings"
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
	setupUploadRoutes(routerOptions)
	setupBookRoutes(routerOptions)
	setupWSRoutes(routerOptions)

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

// newAuthMiddleware is used to get auth token from request headers and validate it.
func newAuthMiddleware(options routerOptions) gin.HandlerFunc {
	logger := options.logger.Named("authMiddleware")

	return errorHandler(options, func(c *gin.Context) (interface{}, *httpErr) {
		// Get token and check if empty ("Bearer token")
		tokenStringRaw := c.GetHeader("Authorization")
		if tokenStringRaw == "" {
			if c.Query("auth") != "" {
				tokenStringRaw = fmt.Sprintf("Bearer %s", c.Query("auth"))
			} else {
				logger.Info("empty Authorization header", "tokenStringRaw", tokenStringRaw)
				return nil, &httpErr{Type: httpErrTypeClient, Message: "empty auth token"}
			}
		}

		// Split Bearer and token
		tokenStringArr := strings.Split(tokenStringRaw, " ")
		if len(tokenStringArr) != 2 {
			logger.Info("malformed auth token", "tokenStringArr", tokenStringArr)
			return nil, &httpErr{Type: httpErrTypeClient, Message: "malformed auth token"}
		}

		tokenString := tokenStringArr[1]
		logger.Info("got token", "tokenString", tokenString)
		logger.Info("request", "request", c.Request.Header)

		userID, err := options.services.Auth.VerifyToken(c, tokenString)
		if err != nil {
			if errs.IsExpected(err) {
				logger.Info(err.Error(), "tokenStringArr", tokenStringArr)
				return nil, &httpErr{Type: httpErrTypeClient, Code: fmt.Sprint(http.StatusUnauthorized), Message: err.Error()}
			}

			logger.Error("failed to verify access token", "tokenStringArr", tokenStringArr)
			return nil, &httpErr{
				Type:    httpErrTypeServer,
				Message: "failed to verify access token",
				Details: err,
			}
		}

		c.Set("userID", userID)

		return nil, nil
	})
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
