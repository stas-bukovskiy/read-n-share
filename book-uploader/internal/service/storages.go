package service

import (
	"context"
	"github.com/stas-bukovskiy/read-n-share/book-uploader/internal/entity"
)

type Storages struct {
	Book BookStorage
}

// BookStorage provides methods for storing books that are used in BookService.
type BookStorage interface {
	// Save saves book to storage.
	Save(ctx context.Context, book *entity.Book) (*entity.Book, error)
	// List returns list of books from storage.
	List(ctx context.Context, filter *BookStorageListFilter) ([]*entity.Book, error)
	// Get returns book from storage.
	Get(ctx context.Context, bookID string) (*entity.Book, error)
}

type BookStorageListFilter struct {
	UserID *string
}
