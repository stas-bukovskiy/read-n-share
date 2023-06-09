package storage

import (
	"context"
	"github.com/stas-bukovskiy/read-n-share/book-reader-api/internal/entity"
	"github.com/stas-bukovskiy/read-n-share/book-reader-api/internal/service"
	"github.com/stas-bukovskiy/read-n-share/book-reader-api/pkg/database"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
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

func (b *bookStorage) List(ctx context.Context, filter *service.BookStorageListFilter) ([]*entity.Book, error) {
	filterStmt := bson.M{}

	if filter != nil {
		if filter.UserID != nil && *filter.UserID != "" {
			filterStmt = bson.M{"owner_user_id": *filter.UserID}
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
