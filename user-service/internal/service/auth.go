package service

import (
	"context"
	"fmt"
	"github.com/stas-bukovskiy/read-n-share/user-service/internal/entity"
	"github.com/stas-bukovskiy/read-n-share/user-service/pkg/token"
	"golang.org/x/crypto/bcrypt"
)

type authService struct {
	serviceContext
}

func NewAuthService(options *Options) AuthService {
	return &authService{
		serviceContext: serviceContext{
			cfg:      options.Config,
			logger:   options.Logger.Named("AuthService"),
			storages: options.Storages,
		},
	}
}

func (s authService) Login(ctx context.Context, options *LoginOptions) (*LoginOutput, error) {
	logger := s.logger.Named("LoginUser").
		With("options", options).
		WithContext(ctx)

	user, err := s.storages.User.GetUser(&GetUserFilter{
		Email: &options.Email,
	})
	if err != nil {
		logger.Error("failed to get user", "error", err)
		return nil, fmt.Errorf("failed to get user: %w", err)
	}
	if user == nil {
		logger.Info("user not found")
		return nil, ErrLoginUserNotFound
	}
	logger = logger.With("user", user)

	err = bcrypt.CompareHashAndPassword([]byte(user.Password), []byte(options.Password))
	if err != nil {
		logger.Info("invalid password for user")
		return nil, ErrLoginInvalidPassword
	}

	token, err := token.SignToken(&token.TokenClaims{
		UserID:   user.ID,
		UserRole: user.Role,
	}, s.cfg.HMACSecret)
	if err != nil {
		logger.Error("failed to sign token", "error", err)
		return nil, fmt.Errorf("failed to sign token: %w", err)
	}

	logger.Info("successfully logged in user")
	return &LoginOutput{
		User:  user,
		Token: token,
	}, nil
}

func (s authService) VerifyToken(ctx context.Context, tkn string) (*entity.User, error) {
	logger := s.logger.Named("VerifyUser").
		With("token", tkn).
		WithContext(ctx)

	claims, err := token.VerifyToken(tkn, s.cfg.HMACSecret)
	if err != nil {
		logger.Info("unable to verify user", "error", err)
		return nil, ErrVerifyUserInvalidToken
	}

	user, err := s.storages.User.GetUser(&GetUserFilter{ID: &claims.UserID})
	if err != nil {
		logger.Error("failed to get user", "error", err)
		return nil, fmt.Errorf("failed to get user: %w", err)
	}
	if user == nil {
		logger.Info("user not found")
		return nil, ErrVerifyUserNotFound
	}
	logger = logger.With("user", user)

	logger.Info("successfully verified user")
	return user, nil
}
