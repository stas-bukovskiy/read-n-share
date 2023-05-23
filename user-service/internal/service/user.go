package service

import (
	"context"
	"github.com/stas-bukovskiy/read-n-share/user-service/config"
	pb "github.com/stas-bukovskiy/read-n-share/user-service/internal/controller/grpc"
	"golang.org/x/crypto/bcrypt"
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

const (
	internalServerErrorCode = "internal server error"

	userAlreadyExistsErrorCode  = "user_already_exists"
	invalidCredentialsErrorCode = "invalid_credentials"
	invalidTokenErrorCode       = "invalid_token"
)

const (
	errTypeClient = "client"
	errTypeServer = "server"
)

func (s userService) CreateUser(ctx context.Context, r *pb.CreateUserRequest) (*pb.CreateUserResponse, error) {
	response := pb.CreateUserResponse{}

	hashedPassword, err := hashPassword(r.Password)
	if err != nil {
		response.Error = &pb.Error{
			Message: "failed to create user",
			Code:    internalServerErrorCode,
			Type:    errTypeServer,
			Details: err.Error(),
		}
		return &response, nil
	}

	userWithEmail, err := s.storage.GetUser(&GetUserFilter{Email: r.Email})
	if err != nil {
		response.Error = &pb.Error{
			Message: "failed to create user",
			Code:    internalServerErrorCode,
			Type:    errTypeServer,
			Details: err.Error(),
		}
		return &response, nil
	}
	if userWithEmail != nil {
		response.Error = &pb.Error{
			Message: "user with this email already exists",
			Code:    userAlreadyExistsErrorCode,
			Type:    errTypeClient,
		}
		return &response, nil
	}

	user := &pb.User{
		Email:    r.Email,
		Name:     r.Name,
		Password: hashedPassword,
		Role:     r.Role,
	}

	user, err = s.storage.CreateUser(user)
	if err != nil {
		response.Error = &pb.Error{
			Message: "failed to create user",
			Code:    internalServerErrorCode,
			Type:    errTypeServer,
			Details: err.Error(),
		}
		return &response, nil
	}

	response.User = user

	return &response, nil
}

func (s userService) GetUser(ctx context.Context, r *pb.GetUserRequest) (*pb.GetUserResponse, error) {
	response := pb.GetUserResponse{}

	user, err := s.storage.GetUser(&GetUserFilter{ID: r.Id})
	if err != nil {
		response.Error = &pb.Error{
			Message: "failed to get user",
			Code:    internalServerErrorCode,
			Type:    errTypeServer,
			Details: err.Error(),
		}
		return &response, nil
	}

	response.User = user

	return &response, nil
}

func (s userService) LoginUser(ctx context.Context, r *pb.LoginUserRequest) (*pb.LoginUserResponse, error) {
	response := pb.LoginUserResponse{}

	user, err := s.storage.GetUser(&GetUserFilter{Email: r.Email})
	if err != nil {
		response.Error = &pb.Error{
			Message: "failed to login user",
			Code:    internalServerErrorCode,
			Type:    errTypeServer,
			Details: err.Error(),
		}
		return &response, nil
	}

	err = bcrypt.CompareHashAndPassword([]byte(user.Password), []byte(r.Password))
	if err != nil {
		response.Error = &pb.Error{
			Message: "unable to login user",
			Code:    invalidCredentialsErrorCode,
			Type:    errTypeClient,
			Details: "invalid password for user",
		}
		return &response, nil
	}

	token, err := SignToken(&TokenClaims{
		UserID:   user.Id,
		UserRole: user.Role,
	}, s.cfg.HMACSecret)
	if err != nil {
		response.Error = &pb.Error{
			Message: "failed to login user",
			Code:    internalServerErrorCode,
			Type:    errTypeServer,
			Details: err.Error(),
		}
		return &response, nil
	}

	response.User = user
	response.Token = token

	return &response, nil
}

func (s userService) VerifyUser(ctx context.Context, r *pb.VerifyUserRequest) (*pb.VerifyUserResponse, error) {
	response := pb.VerifyUserResponse{}

	claims, err := VerifyToken(r.Token, s.cfg.HMACSecret)
	if err != nil {
		response.Error = &pb.Error{
			Message: "unable to verify user",
			Code:    invalidTokenErrorCode,
			Type:    errTypeClient,
		}
		return &response, nil
	}

	user, err := s.storage.GetUser(&GetUserFilter{ID: claims.UserID})
	if err != nil {
		response.Error = &pb.Error{
			Message: "failed to verify user",
			Code:    internalServerErrorCode,
			Type:    errTypeServer,
			Details: err.Error(),
		}
		return &response, nil
	}

	response.User = user

	return &response, nil
}

func (s userService) mustEmbedUnimplementedUserServiceServer() {

}

func hashPassword(password string) (string, error) {
	hash, err := bcrypt.GenerateFromPassword([]byte(password), bcrypt.DefaultCost)
	return string(hash), err
}
