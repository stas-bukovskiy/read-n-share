package database

import (
	"context"
	"errors"
)

type Database interface {
	// Close closes the connection to storage.
	Close() error
	// Ping - checks if storage is available.
	Ping(ctx context.Context) error
}

var (
	ErrDatabaseEmptyOutput = errors.New("empty query output")
)
