package storage

import (
	"context"
	"fmt"
	"github.com/stas-bukovskiy/read-n-share/user-service/internal/service"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
	"time"
)

type MongoDBConfig struct {
	URI      string
	Database string
}

type MongoDB struct {
	DB *mongo.Database
}

func NewMongoDB(cfg MongoDBConfig) (service.UserStorage, error) {
	nosql := &MongoDB{}

	client, err := mongo.Connect(context.Background(), options.Client().ApplyURI(cfg.URI))
	if err != nil {
		return nil, fmt.Errorf("failed to connect to mongodb: %w", err)
	}

	nosql.DB = client.Database(cfg.Database)

	return nosql, nil
}

func (m *MongoDB) Ping(ctx context.Context) error {
	ctx, cancel := context.WithTimeout(ctx, 1*time.Second)
	defer cancel()

	err := m.DB.Client().Ping(ctx, nil)
	if err != nil {
		return fmt.Errorf("failed to connect")
	}
	return nil
}

func (m *MongoDB) Close() error {
	if m.DB != nil {
		err := m.DB.Client().Disconnect(context.Background())
		if err != nil {
			return fmt.Errorf("failed to close mongodb connection: %w", err)
		}
		return nil
	}
	return nil
}
