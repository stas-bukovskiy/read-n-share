package service

import (
	"context"
	"github.com/stas-bukovskiy/read-n-share/book-reader-api/internal/entity"
)

type Storages struct {
	Book BookStorage
}

// BookStorage provides methods for storing books that are used in BookService.
type BookStorage interface {
	// Save saves book to storage.
	Save(ctx context.Context, book *entity.Book) (*entity.Book, error)
	// Update updates book in storage.
	Update(ctx context.Context, book *entity.Book) (*entity.Book, error)
	// List returns list of books from storage.
	List(ctx context.Context, filter *BookStorageListFilter) ([]*entity.Book, error)
	// Get returns book from storage.
	Get(ctx context.Context, bookID string) (*entity.Book, error)

	// CreateShareLink creates share link for book.
	CreateShareLink(ctx context.Context, link *entity.BookShareLink) (*entity.BookShareLink, error)
	// GetShareLink returns share link for book.
	GetShareLink(ctx context.Context, linkID string) (*entity.BookShareLink, error)
	// ListShareLinks returns list of share links for book.
	ListShareLinks(ctx context.Context, filter *BookShareLinkStorageListFilter) ([]*entity.BookShareLink, error)
	// DeleteShareLink deletes share link for book.
	DeleteShareLink(ctx context.Context, linkID string) error

	SaveUserBookSettings(ctx context.Context, userBookSettings *entity.BookUserSettings) (*entity.BookUserSettings, error)
	GetUserBookSettings(ctx context.Context, bookID, userID string) (*entity.BookUserSettings, error)
}

type BookStorageListFilter struct {
	UserID      *string
	GuestUserID *string
}

type BookShareLinkStorageListFilter struct {
	BookID *string
	UserID *string
}
