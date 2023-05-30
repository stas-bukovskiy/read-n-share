package httpcontroller

import (
	"bytes"
	"fmt"
	"github.com/DataDog/gostackparse"
	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
	"github.com/stas-bukovskiy/read-n-share/book-uploader/config"
	"github.com/stas-bukovskiy/read-n-share/book-uploader/internal/service"
	"github.com/stas-bukovskiy/read-n-share/book-uploader/pkg/errs"
	"github.com/stas-bukovskiy/read-n-share/book-uploader/pkg/logging"
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
			logger.Info("empty Authorization header", "tokenStringRaw", tokenStringRaw)
			return nil, &httpErr{Type: httpErrTypeClient, Message: "empty auth token"}
		}

		// Split Bearer and token
		tokenStringArr := strings.Split(tokenStringRaw, " ")
		if len(tokenStringArr) != 2 {
			logger.Info("malformed auth token", "tokenStringArr", tokenStringArr)
			return nil, &httpErr{Type: httpErrTypeClient, Message: "malformed auth token"}
		}

		tokenString := tokenStringArr[1]

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

//
//// httpResponseError provides a base error type for all errors.
//type httpResponseError struct {
//	Type          httpErrType `json:"-"`
//	Message       string      `json:"message"`
//	Code          string      `json:"code,omitempty"`
//	Details       interface{} `json:"details,omitempty"`
//	InvalidFields interface{} `json:"invalidFields,omitempty"`
//}
//
//// httpErrType is used to define error type.
//type httpErrType string
//
//const (
//	// ErrorTypeServer is an "unexpected" internal server error.
//	ErrorTypeServer httpErrType = "server"
//	// ErrorTypeClient is an "expected" business error.
//	ErrorTypeClient httpErrType = "client"
//)
//
//// Error is used to convert an error to a string.
//func (e httpResponseError) Error() string {
//	return fmt.Sprintf("%s: %s", e.Type, e.Message)
//}
//
//// wrapHandler provides unified error handling for all handlers.
//func wrapHandler(options *routerOptions, handler func(r *http.Request) (interface{}, *httpResponseError)) http.HandlerFunc {
//	return func(w http.ResponseWriter, r *http.Request) {
//		logger := options.logger.Named("wrapHandler")
//
//		// handle panics
//		defer func() {
//			if err := recover(); err != nil {
//				// get stacktrace
//				stacktrace, errors := gostackparse.Parse(bytes.NewReader(debug.Stack()))
//				if len(errors) > 0 || len(stacktrace) == 0 {
//					logger.Error("get stacktrace errors", "stacktraceErrors", errors, "stacktrace", "unknown", "err", err)
//				} else {
//					logger.Error("unhandled error", "err", err, "stacktrace", stacktrace)
//				}
//
//				// return error
//				w.WriteHeader(http.StatusInternalServerError)
//				_, err := w.Write([]byte(fmt.Sprintf("internal server error: %v", err)))
//				if err != nil {
//					logger.Error("failed to write error", "err", err)
//				}
//			}
//		}()
//
//		body, err := handler(r)
//
//		// check if middleware
//		if body == nil && err == nil {
//			return
//		}
//		logger = logger.With("body", body).With("err", err)
//
//		// check error
//		if err != nil {
//			if err.Type == ErrorTypeServer {
//				logger.Error("internal server error")
//				w.WriteHeader(http.StatusInternalServerError)
//				_, err := w.Write([]byte(fmt.Sprintf("internal server error: %v", err)))
//				if err != nil {
//					logger.Error("failed to write error", "err", err)
//				}
//			} else {
//				logger.Info("client error")
//				w.WriteHeader(http.StatusUnprocessableEntity)
//
//				data, err := json.Marshal(err)
//				if err != nil {
//					logger.Error("failed to marshal error", "err", err)
//				}
//
//				_, err = w.Write(data)
//				if err != nil {
//					logger.Error("failed to write error", "err", err)
//				}
//			}
//			return
//		}
//		w.WriteHeader(http.StatusOK)
//
//		data, marshalErr := json.Marshal(body)
//		if marshalErr != nil {
//			logger.Error("failed to marshal body", "err", err)
//		}
//
//		_, writeErr := w.Write(data)
//		if writeErr != nil {
//			logger.Error("failed to write response", "err", err)
//		}
//	}
//}
//
//// newAuthMiddleware is used to get auth token from request headers and validate it.
//func newAuthMiddleware(options *routerOptions) func(http.Handler) http.Handler {
//	logger := options.logger.Named("newAuthMiddleware")
//
//	return func(next http.Handler) http.Handler {
//		return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
//			token, err := getAuthToken(r.Header.Get("Authorization"))
//			if err != nil {
//				logger.Error(err.Error())
//				http.Error(w, "Unauthorized", http.StatusUnauthorized)
//				return
//			}
//
//			userId, err := options.services.Auth.VerifyToken(r.Context(), token)
//			if err != nil {
//				if errs.IsExpected(err) {
//					logger.Info(err.Error())
//					http.Error(w, err.Error(), http.StatusUnauthorized)
//				} else {
//					logger.Error(err.Error())
//					http.Error(w, "Internal Server Error", http.StatusInternalServerError)
//				}
//				return
//			}
//
//			ctx := context.WithValue(r.Context(), "userId", userId)
//			// Use the request with the new context in the next handler
//			next.ServeHTTP(w, r.WithContext(ctx))
//		})
//	}
//}
//
//func getAuthToken(rawToken string) (string, error) {
//	if rawToken == "" {
//		return "", fmt.Errorf("empty auth token")
//	}
//
//	// Split Bearer and token
//	splitRawToken := strings.Split(rawToken, " ")
//	if len(splitRawToken) != 2 {
//		return "", fmt.Errorf("malformed auth token")
//	}
//
//	// Get token
//	token := splitRawToken[1]
//
//	return token, nil
//}
