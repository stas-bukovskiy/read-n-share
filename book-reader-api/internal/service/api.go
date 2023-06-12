package service

import (
	"context"
	"github.com/stas-bukovskiy/read-n-share/book-reader-api/internal/entity"
	"io"
)

type APIs struct {
	File FileStorage
	User UserAPI
}

type FileStorage interface {
	UploadFile(ctx context.Context, key string, reader io.Reader) error
	DeleteFile(ctx context.Context, key string) error
	GetFileUrl(ctx context.Context, key string) (string, error)
}

type UserAPI interface {
	VerifyToken(ctx context.Context, token string) (*entity.User, error)
	Login(ctx context.Context, email, password string) (*LoginOutput, error)

	GetUser(ctx context.Context, options *GetUserOptions) (*entity.User, error)
}

type GetUserOptions struct {
	ID       *string
	Email    *string
	Username *string
}

type LoginOutput struct {
	Token string `json:"token"`
	User  *entity.User
}
