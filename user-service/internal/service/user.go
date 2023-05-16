package service

import (
	"context"
	pb "github.com/stas-bukovskiy/read-n-share/user-service/internal/api/grpc"
)

type userService struct {
	pb.UnimplementedUserServiceServer
}

func NewUserService() *userService {
	return &userService{}
}

func (s userService) CreateUser(context.Context, *pb.CreateUserRequest) (*pb.CreateUserResponse, error) {
	return &pb.CreateUserResponse{
		User: &pb.User{
			Name: "ds",
		},
	}, nil
}

func (s userService) GetUser(context.Context, *pb.GetUserRequest) (*pb.GetUserResponse, error) {
	return &pb.GetUserResponse{
		User: &pb.User{
			Name: "ds",
		},
	}, nil
}

func (s userService) LoginUser(context.Context, *pb.LoginUserRequest) (*pb.LoginUserResponse, error) {
	return nil, nil
}

func (s userService) VerifyUser(context.Context, *pb.VerifyUserRequest) (*pb.VerifyUserResponse, error) {
	return nil, nil
}

func (s userService) mustEmbedUnimplementedUserServiceServer() {

}
