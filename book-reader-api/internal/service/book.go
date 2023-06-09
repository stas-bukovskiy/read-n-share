package service

import (
	"context"
	"fmt"
	"github.com/google/uuid"
	"github.com/stas-bukovskiy/read-n-share/book-reader-api/internal/entity"
	"time"
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

func (s *bookService) ListBooks(ctx context.Context, options *BookServiceListOptions) ([]*entity.UserBook, error) {
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

	var userBooks []*entity.UserBook
	for _, book := range books {
		settings, err := s.storages.Book.GetUserBookSettings(ctx, book.ID, options.UserID)
		if err != nil {
			logger.Error("failed to get user book settings", "err", err)
			return nil, fmt.Errorf("failed to get user book settings: %w", err)
		}
		userBooks = append(userBooks, &entity.UserBook{
			Book:     book,
			Settings: settings,
			IsGuest:  false,
		})
	}

	guestBooks, err := s.storages.Book.List(ctx, &BookStorageListFilter{
		GuestUserID: &options.UserID,
	})
	if err != nil {
		logger.Error("failed to list guest books", "err", err)
		return nil, fmt.Errorf("failed to list guest books: %w", err)
	}
	logger = logger.With("guestBooks", guestBooks)

	for _, book := range guestBooks {
		settings, err := s.storages.Book.GetUserBookSettings(ctx, book.ID, options.UserID)
		if err != nil {
			logger.Error("failed to get user book settings", "err", err)
			return nil, fmt.Errorf("failed to get user book settings: %w", err)
		}
		userBooks = append(userBooks, &entity.UserBook{
			Book:     book,
			Settings: settings,
			IsGuest:  true,
		})
	}

	books = append(books, guestBooks...)

	logger.Info("books listed")
	return userBooks, nil
}

func (s *bookService) UpdateBook(ctx context.Context, options *UpdateBookOptions) (*entity.UserBook, error) {
	logger := s.logger.
		Named("UpdateBook").
		WithContext(ctx).
		With("options", options)

	userBook, err := s.GetBook(ctx, options.BookID, options.UserID)
	if err != nil {
		return nil, err
	}

	if userBook.Book.OwnerUserID != options.UserID {
		logger.Info("userBook not owned")
		return nil, ErrBookNotOwned
	}

	if options.Title != nil {
		userBook.Book.Title = *options.Title
	}
	if options.Author != nil {
		userBook.Book.Author = *options.Author
	}
	if options.Description != nil {
		userBook.Book.Description = *options.Description
	}

	updatedBook, err := s.storages.Book.Update(ctx, userBook.Book)
	if err != nil {
		logger.Error("failed to save userBook", "err", err)
		return nil, fmt.Errorf("failed to save userBook: %w", err)
	}
	logger = logger.With("updatedBook", updatedBook)
	userBook.Book = updatedBook

	logger.Info("userBook updated")
	return userBook, nil
}

func (s *bookService) GetBook(ctx context.Context, bookID, userID string) (*entity.UserBook, error) {
	logger := s.logger.
		Named("GetBook").
		WithContext(ctx).
		With("bookID", bookID, "userID", userID)

	book, err := s.storages.Book.Get(ctx, bookID)
	if err != nil {
		logger.Error("failed to get book", "err", err)
		return nil, fmt.Errorf("failed to get book: %w", err)
	}
	if book == nil {
		logger.Error("book not found")
		return nil, ErrBookNotFound
	}
	logger = logger.With("book", book)

	if book.OwnerUserID != userID && !containsString(book.GuestsIDs, userID) {
		logger.Error("user is not allowed to get book")
		return nil, ErrGetBookNotAllowed
	}

	settings, err := s.storages.Book.GetUserBookSettings(ctx, book.ID, userID)
	if err != nil {
		logger.Error("failed to get user book settings", "err", err)
		return nil, fmt.Errorf("failed to get user book settings: %w", err)
	}
	if settings == nil {
		settings = &entity.BookUserSettings{
			BookID:   book.ID,
			UserID:   userID,
			Location: "",
			Chapter:  "N/A",
		}
	}
	logger = logger.With("settings", settings)

	userBook := &entity.UserBook{
		Book:     book,
		Settings: settings,
		IsGuest:  book.OwnerUserID != userID,
	}

	logger.Info("book retrieved")
	return userBook, nil
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
	logger = logger.With("book", book)

	url, err := s.apis.File.GetFileUrl(ctx, book.Book.ID)
	if err != nil {
		logger.Error("failed to get url", "err", err)
		return "", fmt.Errorf("failed to get url: %w", err)
	}
	logger = logger.With("url", url)

	logger.Info("url retrieved")
	return url, nil
}

func (s *bookService) ListBookUserSettings(ctx context.Context, bookID, userID string) ([]*entity.BookUserSettings, error) {
	logger := s.logger.
		Named("ListBookUserSettings").
		WithContext(ctx).
		With("bookID", bookID, "userID", userID)

	book, err := s.GetBook(ctx, bookID, userID)
	if err != nil {
		return nil, err
	}

	var settings []*entity.BookUserSettings
	ownerSettings, err := s.storages.Book.GetUserBookSettings(ctx, bookID, book.Book.OwnerUserID)
	if err != nil {
		logger.Error("failed to get owner settings", "err", err)
		return nil, fmt.Errorf("failed to get owner settings: %w", err)
	}
	if ownerSettings != nil {
		settings = append(settings, ownerSettings)
	}

	for _, guestID := range book.Book.GuestsIDs {
		guestSettings, err := s.storages.Book.GetUserBookSettings(ctx, bookID, guestID)
		if err != nil {
			logger.Error("failed to get guest settings", "err", err)
			return nil, fmt.Errorf("failed to get guest settings: %w", err)
		}
		if guestSettings != nil {
			settings = append(settings, guestSettings)
		}
	}
	logger = logger.With("settings", settings)

	logger.Info("settings retrieved")
	return settings, nil
}

func (s *bookService) CreateShareLink(ctx context.Context, options CreateShareLinkOptions) (*entity.BookShareLink, error) {
	logger := s.logger.
		Named("CreateShareLink").
		WithContext(ctx).
		With("options", options)

	book, err := s.GetBook(ctx, options.BookID, options.UserID)
	if err != nil {
		return nil, err
	}
	logger = logger.With("book", book)

	if book.Book.OwnerUserID != options.UserID {
		logger.Info("user is not owner")
		return nil, ErrCreateShareLinkNotAllowed
	}

	shareLink, err := s.storages.Book.CreateShareLink(ctx, &entity.BookShareLink{
		ID:          uuid.NewString(),
		BookID:      book.Book.ID,
		OwnerUserID: options.UserID,
		ExpiresAt:   options.ExpiresAt,
	})
	if err != nil {
		logger.Error("failed to create share link", "err", err)
		return nil, fmt.Errorf("failed to create share link: %w", err)
	}

	logger.Info("share link created")
	return shareLink, nil
}

func (s *bookService) ListShareLinks(ctx context.Context, userID string, options *ListShareLinksOptions) ([]*entity.BookShareLink, error) {
	logger := s.logger.
		Named("ListShareLinks").
		WithContext(ctx).
		With("userID", userID, "options", options)

	if options.BookID == nil && options.UserID == nil {
		logger.Info("bookID or userID must be provided")
		return nil, ErrListShareLinksOptionsNeeded
	}

	if options.BookID != nil {
		book, err := s.GetBook(ctx, *options.BookID, userID)
		if err != nil {
			return nil, err
		}
		logger = logger.With("book", book)

		if book.Book.OwnerUserID != userID {
			logger.Info("user is not owner")
			return nil, ErrListShareLinksNotAllowed
		}
	}

	if options.UserID != nil && *options.UserID != userID {
		logger.Info("user is not owner")
		return nil, ErrListShareLinksNotAllowed
	}

	shareLinks, err := s.storages.Book.ListShareLinks(ctx, &BookShareLinkStorageListFilter{
		BookID: options.BookID,
		UserID: options.UserID,
	})
	if err != nil {
		logger.Error("failed to list share links", "err", err)
		return nil, fmt.Errorf("failed to list share links: %w", err)
	}
	logger = logger.With("shareLinks", shareLinks)

	logger.Info("share links listed")
	return shareLinks, nil
}

func (s *bookService) DeleteShareLink(ctx context.Context, linkID, userID string) error {
	logger := s.logger.
		Named("DeleteShareLink").
		WithContext(ctx).
		With("linkID", linkID, "userID", userID)

	shareLink, err := s.storages.Book.GetShareLink(ctx, linkID)
	if err != nil {
		logger.Error("failed to get share link", "err", err)
		return fmt.Errorf("failed to get share link: %w", err)
	}
	if shareLink == nil {
		logger.Info("share link not found")
		return ErrDeleteShareLinkNotFound
	}
	logger = logger.With("shareLink", shareLink)

	if shareLink.OwnerUserID != userID {
		logger.Info("user is not owner")
		return ErrDeleteShareLinkNotAllowed
	}

	err = s.storages.Book.DeleteShareLink(ctx, linkID)
	if err != nil {
		logger.Error("failed to delete share link", "err", err)
		return fmt.Errorf("failed to delete share link: %w", err)
	}

	logger.Info("share link deleted")
	return nil
}

func (s *bookService) ShareBook(ctx context.Context, linkID, userID string) (*entity.Book, error) {
	logger := s.logger.
		Named("ShareBook").
		WithContext(ctx).
		With("linkID", linkID, "userID", userID)

	shareLink, err := s.storages.Book.GetShareLink(ctx, linkID)
	if err != nil {
		logger.Error("failed to get share link", "err", err)
		return nil, fmt.Errorf("failed to get share link: %w", err)
	}
	if shareLink == nil {
		logger.Info("share link not found")
		return nil, ErrShareBookLinkNotFound
	}
	logger = logger.With("shareLink", shareLink)

	if shareLink.ExpiresAt.Before(time.Now()) {
		logger.Info("share link expired")
		return nil, ErrShareBookExpired
	}

	book, err := s.storages.Book.Get(ctx, shareLink.BookID)
	if err != nil {
		logger.Error("failed to get book", "err", err)
		return nil, err
	}
	if book == nil {
		logger.Info("book not found")
		return nil, ErrBookNotFound
	}
	logger = logger.With("book", book)

	if book.OwnerUserID == userID || containsString(book.GuestsIDs, userID) {
		logger.Info("user already has access to book")
		return book, nil
	}

	book.GuestsIDs = append(book.GuestsIDs, userID)
	if book, err = s.storages.Book.Update(ctx, book); err != nil {
		logger.Error("failed to update book", "err", err)
		return nil, fmt.Errorf("failed to update book: %w", err)
	}
	logger = logger.With("updatedBook", book)

	logger.Info("book shared")
	return book, nil
}
