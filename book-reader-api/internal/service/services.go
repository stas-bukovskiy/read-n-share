package service

import (
	"context"
	"github.com/stas-bukovskiy/read-n-share/book-reader-api/config"
	"github.com/stas-bukovskiy/read-n-share/book-reader-api/internal/entity"
	"github.com/stas-bukovskiy/read-n-share/book-reader-api/pkg/errs"
	"github.com/stas-bukovskiy/read-n-share/book-reader-api/pkg/logging"
	"mime/multipart"
)

type Services struct {
	Auth   AuthService
	Upload UploadService
	Book   BookService
}

type Options struct {
	APIs     APIs
	Config   *config.Config
	Logger   logging.Logger
	Storages Storages
}

type serviceContext struct {
	apis     APIs
	cfg      *config.Config
	logger   logging.Logger
	storages Storages
}

type UploadService interface {
	UploadBook(ctx context.Context, options *UploadBookOptions) (*entity.Book, error)
	//ConvertBook(ctx context.Context, book *pb.Book) (*pb.Book, error)
}

type UploadBookOptions struct {
	File   multipart.File
	Header *multipart.FileHeader

	Title       string
	Author      string
	Description string
}

type BookService interface {
	ListBooks(ctx context.Context, options *BookServiceListOptions) ([]*entity.Book, error)
	GetBook(ctx context.Context, bookID, userID string) (*entity.Book, error)
	GetBookURL(ctx context.Context, bookID, userID string) (string, error)
	GetBookContent(ctx context.Context, bookID, userID string) ([]byte, error)
}

var (
	ErrGetBookNotAllowed = errs.New("book does not belong to user", "book does not belong to user")
)

type BookServiceListOptions struct {
	UserID string
}

// AuthService is used to authenticate user.
type AuthService interface {
	// VerifyToken is used to verify token. It returns user id if token is valid.
	// Otherwise, it returns error.
	VerifyToken(ctx context.Context, token string) (string, error)
	// Login is used to login user. It returns token if user is authenticated.
	Login(ctx context.Context, email, password string) (string, error)
}
