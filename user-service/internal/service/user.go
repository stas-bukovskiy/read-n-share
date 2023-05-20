package service

import (
	"context"
	"github.com/stas-bukovskiy/read-n-share/user-service/config"
	pb "github.com/stas-bukovskiy/read-n-share/user-service/internal/controller/grpc"
	"golang.org/x/crypto/bcrypt"
	"log"
)

type userService struct {
	pb.UnimplementedUserServiceServer
	serviceContext
}

type serviceContext struct {
	cfg     *config.Config
	storage UserStorage
}

type Options struct {
	Storage UserStorage
	Config  *config.Config
}

func NewUserService(opts *Options) *userService {
	return &userService{
		serviceContext: serviceContext{
			cfg:     opts.Config,
			storage: opts.Storage,
		},
	}
}

func (s userService) CreateUser(ctx context.Context, r *pb.CreateUserRequest) (*pb.CreateUserResponse, error) {
	hashedPassword, err := hashPassword(r.Password)
	if err != nil {
		return nil, err
	}

	user := &pb.User{
		Email:    r.Email,
		Name:     r.Name,
		Password: hashedPassword,
		Role:     r.Role,
	}

	user, err = s.storage.CreateUser(user)
	if err != nil {
		log.Println(err)
		return nil, err
	}

	return &pb.CreateUserResponse{
		User: user,
	}, nil
}

func (s userService) GetUser(ctx context.Context, r *pb.GetUserRequest) (*pb.GetUserResponse, error) {
	user, err := s.storage.GetUser(&GetUserFilter{ID: r.Id})
	if err != nil {
		return nil, err
	}

	return &pb.GetUserResponse{
		User: user,
	}, nil
}

func (s userService) LoginUser(ctx context.Context, r *pb.LoginUserRequest) (*pb.LoginUserResponse, error) {
	user, err := s.storage.GetUser(&GetUserFilter{Email: r.Email})
	if err != nil {
		return nil, err
	}

	err = bcrypt.CompareHashAndPassword([]byte(user.Password), []byte(r.Password))
	if err != nil {
		return nil, err
	}

	token, err := SignToken(&TokenClaims{
		UserID:   user.Id,
		UserRole: user.Role,
	}, s.cfg.HMACSecret)
	if err != nil {
		return nil, err
	}

	return &pb.LoginUserResponse{
		User:  user,
		Token: token,
	}, nil
}

func (s userService) VerifyUser(ctx context.Context, r *pb.VerifyUserRequest) (*pb.VerifyUserResponse, error) {
	claims, err := VerifyToken(r.Token, s.cfg.HMACSecret)
	if err != nil {
		return &pb.VerifyUserResponse{
			User: nil,
		}, nil
	}

	user, err := s.storage.GetUser(&GetUserFilter{ID: claims.UserID})
	if err != nil {
		return nil, err
	}

	return &pb.VerifyUserResponse{
		User: user,
	}, nil
}

func (s userService) mustEmbedUnimplementedUserServiceServer() {

}

func hashPassword(password string) (string, error) {
	hash, err := bcrypt.GenerateFromPassword([]byte(password), bcrypt.DefaultCost)
	return string(hash), err
}
