package service

import (
	"context"
	"io"
)

type APIs struct {
	File FileStorage
}

type FileStorage interface {
	UploadFile(ctx context.Context, key string, reader io.Reader) error
	DeleteFile(ctx context.Context, key string) error
	GetFileUrl(ctx context.Context, key string) (string, error)
}
