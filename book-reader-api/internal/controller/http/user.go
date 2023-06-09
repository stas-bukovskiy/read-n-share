package httpcontroller

import (
	"github.com/gin-gonic/gin"
	"github.com/stas-bukovskiy/read-n-share/book-reader-api/pkg/errs"
)

type userRoutes struct {
	routerContext
}

func setupUserRoutes(options routerOptions) {
	userRoutes := userRoutes{
		routerContext: routerContext{
			services: options.services,
			cfg:      options.cfg,
			logger:   options.logger,
		},
	}

	group := options.router.Group("/user")
	{
		group.POST("/login", errorHandler(options, userRoutes.login))
	}
}

type loginRequestBody struct {
	Email    string `json:"email" binding:"required"`
	Password string `json:"password" binding:"required"`
}

type loginResponseBody struct {
	Token string `json:"token"`
}

func (u *userRoutes) login(c *gin.Context) (interface{}, *httpErr) {
	logger := u.logger.
		Named("login").
		WithContext(c.Request.Context())

	var requestBody loginRequestBody
	err := c.ShouldBindJSON(&requestBody)
	if err != nil {
		logger.Info("failed to bind requestBody", "err", err)
		return nil, &httpErr{
			Type:    httpErrTypeClient,
			Message: "invalid request",
			Details: err.Error(),
		}
	}

	token, err := u.services.Auth.Login(c.Request.Context(), requestBody.Email, requestBody.Password)
	if err != nil {
		if errs.IsExpected(err) {
			logger.Info("failed to login", "err", err)
			return nil, &httpErr{
				Type:    httpErrTypeClient,
				Message: "invalid credentials",
			}
		}
		logger.Error("failed to login", "err", err)
		return nil, &httpErr{
			Type:    httpErrTypeServer,
			Message: "internal server error",
			Details: err.Error(),
		}
	}

	return loginResponseBody{token}, nil
}
