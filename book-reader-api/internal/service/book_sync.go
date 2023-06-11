package service

import (
	"context"
	"fmt"
	"github.com/stas-bukovskiy/read-n-share/book-reader-api/internal/entity"
	"math/rand"
	"strings"
	"sync"
	"time"
)

type bookSyncService struct {
	serviceContext
}

func NewBookSyncService(options *Options) BookSyncService {
	return &bookSyncService{
		serviceContext: serviceContext{
			apis:     options.APIs,
			cfg:      options.Config,
			logger:   options.Logger.Named("BookSyncService"),
			storages: options.Storages,
		},
	}
}

var connections sync.Map

func (b *bookSyncService) Connect(ctx context.Context, userID, bookID string) (chan *entity.BookSync, *entity.BookSync, error) {
	logger := b.logger.
		Named("Connect").
		WithContext(ctx).
		With("userID", userID, "bookID", bookID)

	book, err := b.storages.Book.Get(ctx, bookID)
	if err != nil {
		logger.Error("failed to get book", "err", err)
		return nil, nil, err
	}
	if book == nil {
		logger.Info("book not found")
		return nil, nil, ErrBookNotFound
	}
	if book.OwnerUserID != userID && !containsString(book.GuestsIDs, userID) {
		logger.Info("user is not owner or guest of the book")
		return nil, nil, ErrBookNotFound
	}
	logger = logger.With("book", book)

	userSettings, err := b.storages.Book.GetUserBookSettings(ctx, bookID, userID)
	if err != nil {
		logger.Error("failed to get user book settings", "err", err)
		return nil, nil, err
	}
	if userSettings == nil {
		userSettings = &entity.BookUserSettings{
			BookID:   book.ID,
			UserID:   userID,
			Progress: 0,
			Colour:   generateRandomHexColor(),
		}
	}
	logger = logger.With("userSettings", userSettings)

	connection, _ := connections.LoadOrStore(bookID, &entity.BookSync{
		BookID: bookID,
		Users:  []*entity.BookSyncConnectedUsers{},
	})
	logger = logger.With("connection", connection)

	ch := make(chan *entity.BookSync, 1)
	connection.(*entity.BookSync).Users = append(connection.(*entity.BookSync).Users, &entity.BookSyncConnectedUsers{
		UserSettings:   userSettings,
		UpdatesChannel: ch,
	})

	connections.Store(bookID, connection)

	for _, user := range connection.(*entity.BookSync).Users {
		if user.UserSettings.UserID != userID {
			user.UpdatesChannel <- connection.(*entity.BookSync)
		}
	}

	logger.Info("connection created")
	return ch, connection.(*entity.BookSync), nil
}

func (b *bookSyncService) Receive(ctx context.Context, updatedSettings *entity.BookUserSettings) error {
	logger := b.logger.
		Named("Receive").
		WithContext(ctx).
		With("updatedSettings", updatedSettings)

	connection, ok := connections.Load(updatedSettings.BookID)
	if !ok {
		logger.Info("connection not found, waiting for connection")
		time.Sleep(1 * time.Second)
		connection, ok = connections.Load(updatedSettings.BookID)
		if !ok {
			logger.Info("connection not found")
			return ErrBookNotFound
		}
	}
	logger = logger.With("connection", connection)
	logger.Info("connection found")

	bookSync := connection.(*entity.BookSync)
	logger = logger.With("bookSync", bookSync)
	logger.Info("bookSync found")

	book, err := b.storages.Book.Get(ctx, updatedSettings.BookID)
	if err != nil {
		logger.Error("failed to get book", "err", err)
		return fmt.Errorf("failed to get book: %w", err)
	}
	if book == nil {
		logger.Info("book not found")
		return ErrBookNotFound
	}

	updatedSettings.Chapter = strings.ReplaceAll(updatedSettings.Chapter, "\n", "")
	updatedSettings.Chapter = strings.TrimSpace(updatedSettings.Chapter)

	for index, chapter := range book.Chapters {
		if strings.ToLower(updatedSettings.Chapter) == strings.ToLower(chapter) {
			updatedSettings.Chapter = chapter
			updatedSettings.Progress = int(float64(index) / float64(len(book.Chapters)) * 100)
			logger.Info("chapter found", "chapter", chapter, "progress", updatedSettings.Progress)
			break
		}
	}
	logger = logger.With("updatedSettings", updatedSettings)

	for _, user := range bookSync.Users {
		if user.UserSettings.UserID == updatedSettings.UserID {
			user.UserSettings = updatedSettings
			connections.Store(updatedSettings.BookID, bookSync)
			break
		}
	}

	if !bookSync.IsTicking {
		go func(bs *entity.BookSync) {
			bs.Ticker = time.NewTicker(2 * time.Second)
			bs.IsTicking = true
			connections.Store(updatedSettings.BookID, bs)

			<-bs.Ticker.C

			for _, user := range bs.Users {
				user.UpdatesChannel <- bs
			}
			logger.Debug("sent updates to users")

			bs.IsTicking = false
			bs.Ticker.Stop()
			connections.Store(updatedSettings.BookID, bs)

			for _, user := range bs.Users {
				_, err := b.storages.Book.SaveUserBookSettings(ctx, user.UserSettings)
				if err != nil {
					logger.Error("failed to save user book settings", "err", err)
				}
			}
			logger.Debug("saved user book settings")
		}(bookSync)
	}

	logger.Info("settings received")
	return nil
}

func (b *bookSyncService) Close(ctx context.Context, userID, bookID string) error {
	logger := b.logger.
		Named("Close").
		WithContext(ctx).
		With("userID", userID, "bookID", bookID)

	connection, ok := connections.Load(bookID)
	if !ok {
		logger.Info("connection not found")
		return ErrBookNotFound
	}
	logger = logger.With("connection", connection)

	bookSync := connection.(*entity.BookSync)
	logger = logger.With("bookSync", bookSync)
	logger.Info("closing connection")

	for i, user := range bookSync.Users {
		if user.UserSettings.UserID == userID {
			bookSync.Users = append(bookSync.Users[:i], bookSync.Users[i+1:]...)
			break
		}
	}

	if len(bookSync.Users) == 0 {
		logger.Info("no more users connected, deleting connection")
		connections.Delete(bookID)
	} else {
		logger.Info("users still connected, updating connection")
		connections.Store(bookID, bookSync)
	}

	logger.Info("connection closed")
	return nil
}

func containsString(list []string, str string) bool {
	for _, s := range list {
		if s == str {
			return true
		}
	}
	return false
}

func generateRandomHexColor() string {
	r := rand.New(rand.NewSource(time.Now().UnixNano()))
	return fmt.Sprintf("#%06X", r.Intn(0xFFFFFF+1))
}
