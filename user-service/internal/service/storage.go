package service

import (
	pb "github.com/stas-bukovskiy/read-n-share/user-service/internal/controller/grpc"
)

type UserStorage interface {
	CreateUser(user *pb.User) (*pb.User, error)
	GetUser(filter *GetUserFilter) (*pb.User, error)
}

type GetUserFilter struct {
	ID       string
	Email    string
	Username string
}
