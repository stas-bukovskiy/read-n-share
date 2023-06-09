package service

import (
	"context"
	"fmt"
	"github.com/stas-bukovskiy/read-n-share/book-reader-api/internal/entity"
)

type bookService struct {
	serviceContext
}

func NewBookService(options *Options) BookService {
	return &bookService{
		serviceContext: serviceContext{
			apis:     options.APIs,
			cfg:      options.Config,
			logger:   options.Logger.Named("BookService"),
			storages: options.Storages,
		},
	}
}

func (s *bookService) ListBooks(ctx context.Context, options *BookServiceListOptions) ([]*entity.Book, error) {
	logger := s.logger.
		Named("ListBooks").
		WithContext(ctx).
		With("options", options)

	books, err := s.storages.Book.List(ctx, &BookStorageListFilter{
		UserID: &options.UserID,
	})
	if err != nil {
		logger.Error("failed to list books", "err", err)
		return nil, fmt.Errorf("failed to list books: %w", err)
	}
	logger = logger.With("books", books)

	logger.Info("books listed")
	return books, nil
}

func (s *bookService) GetBook(ctx context.Context, bookID, userID string) (*entity.Book, error) {
	logger := s.logger.
		Named("GetBook").
		WithContext(ctx).
		With("bookID", bookID, "userID", userID)

	book, err := s.storages.Book.Get(ctx, bookID)
	if err != nil {
		logger.Error("failed to get book", "err", err)
		return nil, fmt.Errorf("failed to get book: %w", err)
	}
	logger = logger.With("book", book)

	if book.OwnerUserID != userID {
		for _, guest := range book.GuestsIDs {
			if guest == userID {
				logger.Info("book retrieved")
				return book, nil
			}
		}
		return nil, ErrGetBookNotAllowed
	}

	logger.Info("book retrieved")
	return book, nil
}

func (s *bookService) GetBookURL(ctx context.Context, bookID, userID string) (string, error) {
	logger := s.logger.
		Named("GetBookURL").
		WithContext(ctx).
		With("bookID", bookID, "userID", userID)

	book, err := s.GetBook(ctx, bookID, userID)
	if err != nil {
		return "", err
	}

	url, err := s.apis.File.GetFileUrl(ctx, book.ID)
	if err != nil {
		logger.Error("failed to get url", "err", err)
		return "", fmt.Errorf("failed to get url: %w", err)
	}
	logger = logger.With("url", url)

	logger.Info("url retrieved")
	return url, nil
}
