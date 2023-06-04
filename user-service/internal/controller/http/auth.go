package httpcontroller

import (
	"github.com/gin-gonic/gin"
	"github.com/stas-bukovskiy/read-n-share/user-service/internal/entity"
	"github.com/stas-bukovskiy/read-n-share/user-service/internal/service"
	"github.com/stas-bukovskiy/read-n-share/user-service/pkg/errs"
)

type authRoutes struct {
	routerContext
}

func setupAuthRoutes(options routerOptions) {
	routes := authRoutes{routerContext: routerContext{
		services: options.services,
		cfg:      options.cfg,
		logger:   options.logger.Named("authRoutes"),
	}}

	router := options.router.Group("/auth")
	{
		router.POST("/login", errorHandler(options, routes.login))
		router.GET("/verify", errorHandler(options, routes.verifyToken))
	}
}

type loginRequestBody struct {
	Email    string `json:"email"`
	Password string `json:"password"`
}

type loginResponse struct {
	User  *entity.User `json:"user"`
	Token string       `json:"token"`
}

func (r *authRoutes) login(c *gin.Context) (interface{}, *httpErr) {
	logger := r.logger.Named("login").WithContext(c.Request.Context())

	var req loginRequestBody
	if err := c.ShouldBindJSON(&req); err != nil {
		logger.Error("failed to bind request body", "error", err)
		return nil, &httpErr{
			Type:    httpErrTypeClient,
			Message: "failed to bind request body",
			Code:    "invalid_request_body",
			Details: err.Error(),
		}
	}
	logger = logger.With("request", req)

	output, err := r.services.Auth.Login(c.Request.Context(), &service.LoginOptions{
		Email:    req.Email,
		Password: req.Password,
	})
	if err != nil {
		if errs.IsExpected(err) {
			logger.Info(err.Error())
			return nil, &httpErr{
				Type:    httpErrTypeClient,
				Message: err.Error(),
				Code:    errs.GetCode(err),
			}
		}
		logger.Error("failed to login", "error", err)
		return nil, &httpErr{
			Type:    httpErrTypeServer,
			Message: "failed to login",
			Code:    "failed_to_login",
			Details: err.Error(),
		}
	}
	logger = logger.With("output", output)
	output.User.Password = ""

	logger.Info("successfully logged in")
	return &loginResponse{
		User:  output.User,
		Token: output.Token,
	}, nil
}

type verifyTokenRequestQuery struct {
	Token string `form:"token"`
}

type verifyTokenResponse struct {
	User *entity.User `json:"user"`
}

func (r *authRoutes) verifyToken(c *gin.Context) (interface{}, *httpErr) {
	logger := r.logger.Named("verifyToken").WithContext(c.Request.Context())

	var req verifyTokenRequestQuery
	if err := c.ShouldBindQuery(&req); err != nil {
		logger.Error("failed to bind request query", "error", err.Error())
		return nil, &httpErr{
			Type:    httpErrTypeClient,
			Message: "failed to bind request query",
			Code:    "invalid_request_body",
			Details: err.Error(),
		}
	}

	user, err := r.services.Auth.VerifyToken(c.Request.Context(), req.Token)
	if err != nil {
		if errs.IsExpected(err) {
			logger.Info(err.Error())
			return nil, &httpErr{
				Type:    httpErrTypeClient,
				Message: err.Error(),
				Code:    errs.GetCode(err),
			}
		}
		logger.Error("failed to verify token", "error", err)
		return nil, &httpErr{
			Type:    httpErrTypeServer,
			Message: "failed to verify token",
			Code:    "failed_to_verify_token",
			Details: err.Error(),
		}
	}
	logger = logger.With("user", user)
	user.Password = ""

	logger.Info("successfully verified token")
	return &verifyTokenResponse{
		User: user,
	}, nil
}
