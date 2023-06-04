package service

import (
	"context"
	"fmt"
	"github.com/stas-bukovskiy/read-n-share/user-service/internal/entity"
	"golang.org/x/crypto/bcrypt"
)

type userService struct {
	serviceContext
}

func NewUserService(opts *Options) *userService {
	return &userService{
		serviceContext: serviceContext{
			cfg:      opts.Config,
			storages: opts.Storages,
			logger:   opts.Logger.Named("UserService"),
		},
	}
}

func (s *userService) CreateUser(ctx context.Context, user *entity.User) (*entity.User, error) {
	logger := s.logger.Named("CreateUser").
		With("user", user).
		WithContext(ctx)

	hashedPassword, err := hashPassword(user.Password)
	if err != nil {
		logger.Error("failed to hash password", "error", err)
		return nil, fmt.Errorf("failed to hash password: %w", err)
	}
	logger.Debug("hashed password")
	user.Password = hashedPassword

	userWithEmail, err := s.storages.User.GetUser(&GetUserFilter{Email: &user.Email})
	if err != nil {
		logger.Error("failed to get user", "error", err)
		return nil, fmt.Errorf("failed to get user: %w", err)
	}
	if userWithEmail != nil {
		logger.Info("user with this email already exists")
		return nil, ErrCreateUserUserAlreadyExists
	}

	user, err = s.storages.User.CreateUser(user)
	if err != nil {
		logger.Error("failed to create user", "error", err)
		return nil, fmt.Errorf("failed to create user: %w", err)
	}

	logger.Info("successfully created user")
	return user, nil
}

func (s *userService) GetUser(ctx context.Context, options *GetUserOptions) (*entity.User, error) {
	logger := s.logger.Named("GetUser").
		With("options", options).
		WithContext(ctx)

	user, err := s.storages.User.GetUser(&GetUserFilter{
		ID:       options.ID,
		Email:    options.Email,
		Username: options.Username,
	})
	if err != nil {
		logger.Error("failed to get user", "error", err)
		return nil, fmt.Errorf("failed to get user: %w", err)
	}
	if user == nil {
		logger.Info("user not found")
		return nil, ErrGetUserUserNotFound
	}
	logger = logger.With("user", user)

	logger.Info("successfully got user")
	return user, nil
}

func hashPassword(password string) (string, error) {
	hash, err := bcrypt.GenerateFromPassword([]byte(password), bcrypt.DefaultCost)
	return string(hash), err
}
