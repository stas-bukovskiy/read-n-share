package storage

import (
	"context"
	"github.com/google/uuid"
	"github.com/stas-bukovskiy/read-n-share/user-service/internal/entity"
	"github.com/stas-bukovskiy/read-n-share/user-service/internal/service"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
)

func (m *MongoDB) CreateUser(user *entity.User) (*entity.User, error) {
	user.ID = uuid.NewString()

	result, err := m.DB.Collection("user").InsertOne(context.TODO(), user)
	if err != nil {
		return nil, err
	}

	err = m.DB.Collection("user").
		FindOne(context.TODO(), bson.M{"_id": result.InsertedID}).
		Decode(user)
	if err != nil {
		return nil, err
	}
	if user == nil {
		return nil, nil
	}

	return user, nil
}

func (m *MongoDB) GetUser(filter *service.GetUserFilter) (*entity.User, error) {
	filterStmt := bson.M{}
	if filter.ID != nil && *filter.ID != "" {
		filterStmt = bson.M{"_id": *filter.ID}
	}
	if filter.Email != nil && *filter.Email != "" {
		filterStmt = bson.M{"email": *filter.Email}
	}
	if filter.Username != nil && *filter.Username != "" {
		filterStmt = bson.M{"username": *filter.Username}
	}

	user := &entity.User{}
	err := m.DB.Collection("user").FindOne(context.TODO(), filterStmt).Decode(user)
	if err != nil {
		if err == mongo.ErrNoDocuments {
			return nil, nil
		}
		return nil, err
	}

	return user, nil
}
