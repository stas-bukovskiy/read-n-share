package user

import (
	"context"
	"encoding/json"
	"fmt"
	"github.com/go-resty/resty/v2"
	"github.com/stas-bukovskiy/read-n-share/book-uploader/config"
	"github.com/stas-bukovskiy/read-n-share/book-uploader/internal/entity"
	"github.com/stas-bukovskiy/read-n-share/book-uploader/internal/service"
	"github.com/stas-bukovskiy/read-n-share/book-uploader/pkg/errs"
	"github.com/stas-bukovskiy/read-n-share/book-uploader/pkg/logging"
)

type userAPI struct {
	resty  *resty.Client
	logger logging.Logger
}

func New(cfg *config.Config, logger logging.Logger) *userAPI {
	r := resty.New()
	r.SetBaseURL(cfg.Auth.Host)

	return &userAPI{
		resty:  r,
		logger: logger.Named("UserAPI"),
	}
}

type verifyTokenResponseBody struct {
	User struct {
		ID       string `json:"id"`
		Email    string `json:"email"`
		Username string `json:"username"`
		Role     string `json:"role"`
	} `json:"user"`
}

type apiError struct {
	Code    string `json:"code"`
	Message string `json:"message"`
}

func (u *userAPI) VerifyToken(ctx context.Context, token string) (*entity.User, error) {
	logger := u.logger.
		Named("VerifyToken").
		WithContext(ctx).
		With("token", token)

	var respBody verifyTokenResponseBody
	resp, err := u.resty.R().
		SetQueryParam("token", token).
		SetResult(&respBody).
		Get("/auth/verify")
	if err != nil {
		logger.Error("failed to verify token", "err", err)
		return nil, fmt.Errorf("failed to verify token: %w", err)
	}
	if resp.IsError() || resp.StatusCode() != 200 {
		if resp.StatusCode() == 422 {
			var apiErr apiError
			if err := json.Unmarshal(resp.Body(), &apiErr); err != nil {
				return nil, fmt.Errorf("failed to send SMS, status code: %d", resp.StatusCode())
			}
			logger = logger.With("apiErr", apiErr)

			logger.Info("unable to verify token", "reason", "unauthorized")
			return nil, &errs.Err{
				Code:    apiErr.Code,
				Message: apiErr.Message,
			}
		}
		logger.Info("unable to verify token", "reason", resp.Status())
		return nil, fmt.Errorf("unable to verify token: %s, body: %s", resp.Status(), resp.Body())
	}

	logger.Info("token verified")
	return &entity.User{
		ID:       respBody.User.ID,
		Email:    respBody.User.Email,
		Username: respBody.User.Username,
		Role:     respBody.User.Role,
	}, nil
}

type loginResponseBody struct {
	Token string `json:"token"`
	User  struct {
		ID       string `json:"id"`
		Email    string `json:"email"`
		Username string `json:"username"`
		Role     string `json:"role"`
	}
}

func (u *userAPI) Login(ctx context.Context, email, password string) (*service.LoginOutput, error) {
	logger := u.logger.
		Named("Login").
		WithContext(ctx).
		With("email", email, "password", password)

	var respBody loginResponseBody
	resp, err := u.resty.R().
		SetBody(map[string]string{
			"email":    email,
			"password": password,
		}).
		SetResult(&respBody).
		Post("/auth/login")
	if err != nil {
		logger.Error("failed to login", "err", err)
		return nil, fmt.Errorf("failed to login: %w", err)
	}
	if resp.IsError() || resp.StatusCode() != 200 {
		if resp.StatusCode() == 422 {
			var apiErr apiError
			if err := json.Unmarshal(resp.Body(), &apiErr); err != nil {
				return nil, fmt.Errorf("failed to send SMS, status code: %d", resp.StatusCode())
			}
			logger = logger.With("apiErr", apiErr)

			logger.Info("unable to login", "reason", "unauthorized")
			return nil, &errs.Err{
				Code:    apiErr.Code,
				Message: apiErr.Message,
			}
		}
		logger.Info("unable to login", "reason", resp.Status())
		return nil, fmt.Errorf("unable to login: %s, body: %s", resp.Status(), resp.Body())
	}

	logger.Info("login success")
	return &service.LoginOutput{
		User: &entity.User{
			ID:       respBody.User.ID,
			Email:    respBody.User.Email,
			Username: respBody.User.Username,
			Role:     respBody.User.Role,
		},
		Token: respBody.Token,
	}, nil
}
