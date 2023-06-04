package service

import (
	"context"
	"github.com/stas-bukovskiy/read-n-share/user-service/config"
	"github.com/stas-bukovskiy/read-n-share/user-service/internal/entity"
	"github.com/stas-bukovskiy/read-n-share/user-service/pkg/errs"
	"github.com/stas-bukovskiy/read-n-share/user-service/pkg/logging"
)

type Services struct {
	Auth AuthService
	User UserService
}

type Options struct {
	Config   *config.Config
	Logger   logging.Logger
	Storages Storages
}

type serviceContext struct {
	cfg      *config.Config
	logger   logging.Logger
	storages Storages
}

type AuthService interface {
	Login(ctx context.Context, options *LoginOptions) (*LoginOutput, error)
	VerifyToken(ctx context.Context, tkn string) (*entity.User, error)
}

var (
	ErrLoginUserNotFound    = errs.New("user not found", "user_not_found")
	ErrLoginInvalidPassword = errs.New("invalid password", "invalid_password")

	ErrVerifyUserInvalidToken = errs.New("invalid token", "invalid_token")
	ErrVerifyUserNotFound     = errs.New("user not found", "user_not_found")
)

type LoginOptions struct {
	Email    string
	Password string
}

type LoginOutput struct {
	User  *entity.User
	Token string
}

type UserService interface {
	CreateUser(ctx context.Context, user *entity.User) (*entity.User, error)
	GetUser(ctx context.Context, options *GetUserOptions) (*entity.User, error)
}

var (
	ErrCreateUserUserAlreadyExists = errs.New("user already exists", "user_already_exists")
	ErrGetUserUserNotFound         = errs.New("user not found", "user_not_found")
)

type GetUserOptions struct {
	ID       *string
	Email    *string
	Username *string
}
