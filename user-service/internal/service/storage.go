package service

import (
	"github.com/stas-bukovskiy/read-n-share/user-service/internal/entity"
)

type Storages struct {
	User UserStorage
}

type UserStorage interface {
	CreateUser(user *entity.User) (*entity.User, error)
	GetUser(filter *GetUserFilter) (*entity.User, error)
}

type GetUserFilter struct {
	ID       *string
	Email    *string
	Username *string
}
