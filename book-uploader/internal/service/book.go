package service

import (
	"context"
	"fmt"
	"github.com/stas-bukovskiy/read-n-share/book-uploader/internal/entity"
	"os"
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

func (s *bookService) GetBookContent(ctx context.Context, bookID, userID string) ([]byte, error) {
	logger := s.logger.
		Named("GetBookContent").
		WithContext(ctx).
		With("bookID", bookID, "userID", userID)

	book, err := s.GetBook(ctx, bookID, userID)
	if err != nil {
		return nil, err
	}

	fileContent, err := os.ReadFile(fmt.Sprintf("local/%s-book.epub", book.ID))
	if err != nil {
		if !os.IsNotExist(err) {
			logger.Error("failed to read file", "err", err)
			return nil, fmt.Errorf("failed to read file: %w", err)
		} else {
			newFile, err := os.Create(fmt.Sprintf("local/%s-book.epub", book.ID))
			if err != nil {
				logger.Error("failed to create file", "err", err)
				return nil, fmt.Errorf("failed to create file: %w", err)
			}

			err = s.apis.File.DownloadFile(book.ID, newFile)
			if err != nil {
				logger.Error("failed to download file", "err", err)
				return nil, fmt.Errorf("failed to download file: %w", err)
			}

			fileContent, err = os.ReadFile(fmt.Sprintf("local/%s-book.epub", book.ID))
			if err != nil {
				logger.Error("failed to read file", "err", err)
				return nil, fmt.Errorf("failed to read file: %w", err)
			}
		}
	}

	logger.Info("content retrieved")
	return fileContent, nil
}
