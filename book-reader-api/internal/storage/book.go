package storage

import (
	"context"
	"github.com/stas-bukovskiy/read-n-share/book-reader-api/internal/entity"
	"github.com/stas-bukovskiy/read-n-share/book-reader-api/internal/service"
	"github.com/stas-bukovskiy/read-n-share/book-reader-api/pkg/database"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
)

type bookStorage struct {
	*database.MongoDB
}

func NewBookStorage(mongodb *database.MongoDB) *bookStorage {
	return &bookStorage{mongodb}
}

var _ service.BookStorage = (*bookStorage)(nil)

func (b *bookStorage) Save(ctx context.Context, book *entity.Book) (*entity.Book, error) {
	result, err := b.DB.Collection("user-books").InsertOne(ctx, book)
	if err != nil {
		return nil, err
	}

	var createdBook *entity.Book
	err = b.DB.Collection("user-books").
		FindOne(ctx, bson.M{"_id": result.InsertedID}).
		Decode(&createdBook)
	if err != nil {
		return nil, err
	}

	return createdBook, nil
}

func (b *bookStorage) Update(ctx context.Context, book *entity.Book) (*entity.Book, error) {
	_, err := b.DB.Collection("user-books").UpdateByID(ctx, book.ID, bson.M{"$set": book})
	if err != nil {
		return nil, err
	}

	var updatedBook *entity.Book
	err = b.DB.Collection("user-books").
		FindOne(ctx, bson.M{"_id": book.ID}).
		Decode(&updatedBook)
	if err != nil {
		return nil, err
	}

	return updatedBook, nil
}

func (b *bookStorage) List(ctx context.Context, filter *service.BookStorageListFilter) ([]*entity.Book, error) {
	filterStmt := bson.M{}

	if filter != nil {
		if filter.UserID != nil && *filter.UserID != "" {
			filterStmt = bson.M{"owner_user_id": *filter.UserID}
		}
		if filter.GuestUserID != nil && *filter.GuestUserID != "" {
			filterStmt = bson.M{"guests_ids": *filter.GuestUserID}
		}
	}

	cursor, err := b.DB.Collection("user-books").Find(ctx, filterStmt)
	if err != nil {
		return nil, err
	}

	var books []*entity.Book
	err = cursor.All(ctx, &books)
	if err != nil {
		return nil, err
	}

	return books, nil
}

func (b *bookStorage) Get(ctx context.Context, bookID string) (*entity.Book, error) {
	book := &entity.Book{}
	err := b.DB.Collection("user-books").FindOne(ctx, bson.M{"_id": bookID}).Decode(book)
	if err != nil {
		if err == mongo.ErrNoDocuments {
			return nil, nil
		}
		return nil, err
	}

	return book, nil
}

func (b *bookStorage) CreateShareLink(ctx context.Context, link *entity.BookShareLink) (*entity.BookShareLink, error) {
	result, err := b.DB.Collection("user-books-share-links").InsertOne(ctx, link)
	if err != nil {
		return nil, err
	}

	var createdLink *entity.BookShareLink
	err = b.DB.Collection("user-books-share-links").
		FindOne(ctx, bson.M{"_id": result.InsertedID}).
		Decode(&createdLink)
	if err != nil {
		return nil, err
	}

	return createdLink, nil
}

func (b *bookStorage) GetShareLink(ctx context.Context, linkID string) (*entity.BookShareLink, error) {
	link := &entity.BookShareLink{}
	err := b.DB.Collection("user-books-share-links").FindOne(ctx, bson.M{"_id": linkID}).Decode(link)
	if err != nil {
		if err == mongo.ErrNoDocuments {
			return nil, nil
		}
		return nil, err
	}

	return link, nil
}

func (b *bookStorage) ListShareLinks(ctx context.Context, filter *service.BookShareLinkStorageListFilter) ([]*entity.BookShareLink, error) {
	filterStmt := bson.M{}

	if filter != nil {
		if filter.BookID != nil && *filter.BookID != "" {
			filterStmt = bson.M{"book_id": *filter.BookID}
		}
		if filter.UserID != nil && *filter.UserID != "" {
			filterStmt = bson.M{"owner_user_id": *filter.UserID}
		}
	}

	cursor, err := b.DB.Collection("user-books-share-links").Find(ctx, filterStmt)
	if err != nil {
		return nil, err
	}

	var links []*entity.BookShareLink
	err = cursor.All(ctx, &links)
	if err != nil {
		return nil, err
	}

	return links, nil
}

func (b *bookStorage) DeleteShareLink(ctx context.Context, linkID string) error {
	_, err := b.DB.Collection("user-books-share-links").DeleteOne(ctx, bson.M{"_id": linkID})
	if err != nil {
		return err
	}

	return nil
}

func (b *bookStorage) SaveUserBookSettings(ctx context.Context, userBookSettings *entity.BookUserSettings) (*entity.BookUserSettings, error) {
	filter := bson.D{{"user_id", userBookSettings.UserID}, {"book_id", userBookSettings.BookID}}
	opts := options.Update().SetUpsert(true)
	update := bson.M{
		"$set": userBookSettings,
	}

	_, err := b.DB.Collection("user-books-settings").UpdateOne(ctx, filter, update, opts)
	if err != nil {
		return nil, err
	}

	var createdUserBookSettings *entity.BookUserSettings
	err = b.DB.Collection("user-books-settings").
		FindOne(ctx, filter).
		Decode(&createdUserBookSettings)
	if err != nil {
		return nil, err
	}

	return createdUserBookSettings, nil
}

func (b *bookStorage) GetUserBookSettings(ctx context.Context, bookID, userID string) (*entity.BookUserSettings, error) {
	filter := bson.D{{"user_id", userID}, {"book_id", bookID}}
	settings := &entity.BookUserSettings{}
	err := b.DB.Collection("user-books-settings").FindOne(ctx, filter).Decode(settings)
	if err != nil {
		if err == mongo.ErrNoDocuments {
			return nil, nil
		}
		return nil, err
	}

	return settings, nil
}
