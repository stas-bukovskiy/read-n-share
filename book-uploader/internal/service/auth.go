package service

import (
	"context"
	pb "github.com/stas-bukovskiy/read-n-share/book-uploader/internal/controller/grpc"
	"github.com/stas-bukovskiy/read-n-share/book-uploader/pkg/errs"
)

type authService struct {
	serviceContext
	authClient pb.UserServiceClient
}

func NewAuthService(options *Options) *authService {
	return &authService{
		serviceContext: serviceContext{
			cfg:    options.Config,
			logger: options.Logger.Named("AuthService"),
		},
		authClient: options.AuthClient,
	}
}

func (s *authService) VerifyToken(ctx context.Context, token string) (string, error) {
	logger := s.logger.
		Named("VerifyToken").
		WithContext(ctx).
		With("token", token)

	output, err := s.authClient.VerifyUser(ctx, &pb.VerifyUserRequest{
		Token: token,
	})
	if err != nil {
		logger.Error("failed to verify token", "err", err)
		return "", err
	}

	if output.Error != nil {
		logger.Info("unable to verify token", "reason", output.Error)
		return "", &errs.Err{
			Code:    output.Error.Code,
			Message: output.Error.Message,
			Details: map[string]string{
				"details": output.Error.Details,
			},
		}
	}

	logger.Info("token verified")
	return output.User.Id, nil
}

func (s *authService) Login(ctx context.Context, email, password string) (string, error) {
	logger := s.logger.
		Named("Login").
		WithContext(ctx).
		With("email", email, "password", password)

	output, err := s.authClient.LoginUser(ctx, &pb.LoginUserRequest{
		Email:    email,
		Password: password,
	})
	if err != nil {
		logger.Error("failed to login", "err", err)
		return "", err
	}
	if output.Error != nil {
		logger.Info("unable to login", "reason", output.Error)
		return "", &errs.Err{
			Code:    output.Error.Code,
			Message: output.Error.Message,
			Details: map[string]string{
				"details": output.Error.Details,
			},
		}
	}

	logger.Info("user logged in")
	return output.Token, nil
}
