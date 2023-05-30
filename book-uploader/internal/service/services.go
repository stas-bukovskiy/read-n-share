package service

import (
	"context"
	"github.com/stas-bukovskiy/read-n-share/book-uploader/config"
	pb "github.com/stas-bukovskiy/read-n-share/book-uploader/internal/controller/grpc"
	"github.com/stas-bukovskiy/read-n-share/book-uploader/internal/entity"
	"github.com/stas-bukovskiy/read-n-share/book-uploader/pkg/logging"
	"mime/multipart"
)

type Services struct {
	Auth   AuthService
	Upload UploadService
}

type Options struct {
	AuthClient pb.UserServiceClient
	APIs       APIs
	Config     *config.Config
	Logger     logging.Logger
}

type serviceContext struct {
	apis   APIs
	cfg    *config.Config
	logger logging.Logger
}

type UploadService interface {
	UploadBook(ctx context.Context, options *UploadBookOptions) (*entity.Book, error)
	//ConvertBook(ctx context.Context, book *pb.Book) (*pb.Book, error)
}

type UploadBookOptions struct {
	File   multipart.File
	Header *multipart.FileHeader
}

// AuthService is used to authenticate user.
type AuthService interface {
	// VerifyToken is used to verify token. It returns user id if token is valid.
	// Otherwise, it returns error.
	VerifyToken(ctx context.Context, token string) (string, error)
	// Login is used to login user. It returns token if user is authenticated.
	Login(ctx context.Context, email, password string) (string, error)
}
