package httpcontroller

import (
	"github.com/gin-gonic/gin"
	"github.com/stas-bukovskiy/read-n-share/user-service/internal/entity"
	"github.com/stas-bukovskiy/read-n-share/user-service/internal/service"
	"github.com/stas-bukovskiy/read-n-share/user-service/pkg/errs"
)

type userRoutes struct {
	routerContext
}

func setupUserRoutes(options routerOptions) {
	routes := userRoutes{routerContext: routerContext{
		services: options.services,
		cfg:      options.cfg,
		logger:   options.logger.Named("userRoutes"),
	}}

	router := options.router.Group("/users")
	{
		router.POST("", errorHandler(options, routes.createUser))
		router.GET("", errorHandler(options, routes.getUser))
	}
}

type createUserRequestBody struct {
	Email    string `json:"email"`
	Username string `json:"username"`
	Password string `json:"password"`
	Role     string `json:"role"`
}

type createUserResponse struct {
	User *entity.User `json:"user"`
}

func (r *userRoutes) createUser(c *gin.Context) (interface{}, *httpErr) {
	logger := r.logger.Named("createUser").WithContext(c.Request.Context())

	var req createUserRequestBody
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

	user := &entity.User{
		Email:    req.Email,
		Username: req.Username,
		Password: req.Password,
		Role:     req.Role,
	}

	user, err := r.services.User.CreateUser(c.Request.Context(), user)
	if err != nil {
		if errs.IsExpected(err) {
			logger.Info(err.Error())
			return nil, &httpErr{
				Type:    httpErrTypeClient,
				Message: err.Error(),
				Code:    errs.GetCode(err),
			}
		}
		logger.Error("failed to create user", "error", err)
		return nil, &httpErr{
			Type:    httpErrTypeServer,
			Message: "failed to create user",
			Code:    "internal_server_error",
			Details: err.Error(),
		}
	}
	logger = logger.With("user", user)
	user.Password = ""

	logger.Info("user created")
	return createUserResponse{User: user}, nil
}

type getUserRequestBody struct {
	ID       *string `form:"id"`
	Email    *string `form:"email"`
	Username *string `form:"username"`
}

type getUserResponse struct {
	User *entity.User `json:"user"`
}

func (r *userRoutes) getUser(c *gin.Context) (interface{}, *httpErr) {
	logger := r.logger.Named("getUser").WithContext(c.Request.Context())

	var req getUserRequestBody
	if err := c.ShouldBindQuery(&req); err != nil {
		logger.Error("failed to bind request query", "error", err)
		return nil, &httpErr{
			Type:    httpErrTypeClient,
			Message: "failed to bind request query",
			Code:    "invalid_request_query",
			Details: err.Error(),
		}
	}
	logger = logger.With("request", req)

	user, err := r.services.User.GetUser(c.Request.Context(), &service.GetUserOptions{
		ID:       req.ID,
		Email:    req.Email,
		Username: req.Username,
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
		logger.Error("failed to get user", "error", err)
		return nil, &httpErr{
			Type:    httpErrTypeServer,
			Message: "failed to get user",
			Code:    "internal_server_error",
			Details: err.Error(),
		}
	}
	logger = logger.With("user", user)
	user.Password = ""

	logger.Info("user found")
	return getUserResponse{User: user}, nil
}
