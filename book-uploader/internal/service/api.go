package service

import (
	"context"
	"github.com/stas-bukovskiy/read-n-share/book-uploader/internal/entity"
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
}

type LoginOutput struct {
	Token string `json:"token"`
	User  *entity.User
}
