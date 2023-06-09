package service

import (
	"context"
	"github.com/stas-bukovskiy/read-n-share/book-reader-api/config"
	"github.com/stas-bukovskiy/read-n-share/book-reader-api/internal/entity"
	"github.com/stas-bukovskiy/read-n-share/book-reader-api/pkg/errs"
	"github.com/stas-bukovskiy/read-n-share/book-reader-api/pkg/logging"
	"mime/multipart"
	"time"
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
	UpdateBook(ctx context.Context, options *UpdateBookOptions) (*entity.Book, error)
	GetBook(ctx context.Context, bookID, userID string) (*entity.Book, error)
	GetBookURL(ctx context.Context, bookID, userID string) (string, error)

	CreateShareLink(ctx context.Context, options CreateShareLinkOptions) (*entity.BookShareLink, error)
	ListShareLinks(ctx context.Context, userID string, options *ListShareLinksOptions) ([]*entity.BookShareLink, error)
	DeleteShareLink(ctx context.Context, linkID, userID string) error

	ShareBook(ctx context.Context, linkID, userID string) (*entity.Book, error)
}

var (
	ErrBookNotFound = errs.New("book not found", "book not found")

	ErrGetBookNotAllowed = errs.New("book does not belong to user", "book does not belong to user")

	ErrBookNotOwned = errs.New("book does not belong to user", "book does not belong to user")

	ErrCreateShareLinkNotAllowed = errs.New("book does not belong to user", "book does not belong to user")

	ErrListShareLinksOptionsNeeded = errs.New("book id or user id must be provided", "book id or user id must be provided")
	ErrListShareLinksNotAllowed    = errs.New("book does not belong to user", "book does not belong to user")

	ErrDeleteShareLinkNotFound   = errs.New("share link not found", "share link not found")
	ErrDeleteShareLinkNotAllowed = errs.New("book does not belong to user", "book does not belong to user")

	ErrShareBookLinkNotFound = errs.New("share link not found", "share link not found")
	ErrShareBookExpired      = errs.New("share link expired", "share link expired")
)

type UpdateBookOptions struct {
	BookID      string
	UserID      string
	Title       *string
	Author      *string
	Description *string
}

type BookServiceListOptions struct {
	UserID string
}

type CreateShareLinkOptions struct {
	BookID    string
	UserID    string
	ExpiresAt *time.Time
}

type ListShareLinksOptions struct {
	BookID *string
	UserID *string
}

// AuthService is used to authenticate user.
type AuthService interface {
	// VerifyToken is used to verify token. It returns user id if token is valid.
	// Otherwise, it returns error.
	VerifyToken(ctx context.Context, token string) (string, error)
	// Login is used to login user. It returns token if user is authenticated.
	Login(ctx context.Context, email, password string) (string, error)
}
