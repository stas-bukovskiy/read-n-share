package service

import (
	"context"
	"fmt"
	"github.com/stas-bukovskiy/read-n-share/book-reader-api/pkg/errs"
)

type authService struct {
	serviceContext
}

func NewAuthService(options *Options) *authService {
	return &authService{
		serviceContext: serviceContext{
			cfg:    options.Config,
			logger: options.Logger.Named("AuthService"),
			apis:   options.APIs,
		},
	}
}

func (s *authService) VerifyToken(ctx context.Context, token string) (string, error) {
	logger := s.logger.
		Named("VerifyToken").
		WithContext(ctx).
		With("token", token)

	user, err := s.apis.User.VerifyToken(ctx, token)
	if err != nil {
		if errs.IsExpected(err) {
			logger.Info("unable to verify token", "reason", err)
			return "", err
		}
		logger.Error("failed to verify token", "err", err)
		return "", fmt.Errorf("failed to verify token: %w", err)
	}
	logger = logger.With("user", user)

	logger.Info("token verified")
	return user.ID, nil
}

func (s *authService) Login(ctx context.Context, email, password string) (string, error) {
	logger := s.logger.
		Named("Login").
		WithContext(ctx).
		With("email", email, "password", password)

	output, err := s.apis.User.Login(ctx, email, password)
	if err != nil {
		if errs.IsExpected(err) {
			logger.Info("unable to login", "reason", err)
			return "", err
		}
		logger.Error("failed to login", "err", err)
		return "", fmt.Errorf("failed to login: %w", err)
	}
	logger = logger.With("user", output.User)

	logger.Info("user logged in")
	return output.Token, nil
}
