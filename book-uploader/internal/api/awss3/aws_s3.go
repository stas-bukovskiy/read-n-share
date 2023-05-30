package awss3

import (
	"context"
	"fmt"
	"github.com/aws/aws-sdk-go/aws/credentials"
	"io"
	"time"

	// third party
	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/aws/session"
	"github.com/aws/aws-sdk-go/service/s3"
	"github.com/aws/aws-sdk-go/service/s3/s3manager"

	// external
	"github.com/stas-bukovskiy/read-n-share/book-uploader/config"
	"github.com/stas-bukovskiy/read-n-share/book-uploader/pkg/logging"
)

type awsS3 struct {
	cfg     *config.Config
	session *session.Session
	s3      *s3.S3
	logger  logging.Logger
}

func New(cfg *config.Config, logger logging.Logger) *awsS3 {
	session := session.Must(session.NewSession(&aws.Config{
		Region: aws.String(cfg.AWS.Region),
		Credentials: credentials.NewCredentials(&credentials.StaticProvider{
			Value: credentials.Value{
				AccessKeyID:     cfg.AWS.IAMUserAccessToken,
				SecretAccessKey: cfg.AWS.IAMUserSecretKey,
			},
		}),
	}))
	s3 := s3.New(session)

	return &awsS3{
		cfg:     cfg,
		session: session,
		s3:      s3,
		logger:  logger.Named("awsS3API"),
	}
}

// UploadFile is used to upload file to AWS S3.
// https://pkg.go.dev/github.com/aws/aws-sdk-go/service/s3#hdr-Upload_Managers
func (a *awsS3) UploadFile(ctx context.Context, key string, reader io.Reader) error {
	logger := a.logger.
		Named("UploadFile").
		WithContext(ctx).
		With("key", key)

	uploader := s3manager.NewUploader(a.session)
	_, err := uploader.Upload(&s3manager.UploadInput{
		Bucket: aws.String(a.cfg.AWS.S3Bucket),
		Key:    aws.String(key),
		Body:   reader,
	})
	if err != nil {
		logger.Error("failed to upload file", "err", err)
		return fmt.Errorf("failed to upload file: %w", err)
	}

	logger.Info("successfully uploaded file")
	return nil
}

// DeleteFile is used to delete file from AWS S3.
// https://pkg.go.dev/github.com/aws/aws-sdk-go/service/s3#example-S3.DeleteObject-Shared00
func (a *awsS3) DeleteFile(ctx context.Context, key string) error {
	logger := a.logger.
		Named("DeleteFile").
		WithContext(ctx).
		With("key", key)

	_, err := a.s3.DeleteObject(&s3.DeleteObjectInput{
		Bucket: aws.String(a.cfg.AWS.S3Bucket),
		Key:    aws.String(key),
	})
	if err != nil {
		logger.Error("failed to delete file", "err", err)
		return fmt.Errorf("failed to delete file: %w", err)
	}

	logger.Info("successfully deleted file")
	return nil
}

// GetFileUrl is used to get file url from AWS S3.
// https://pkg.go.dev/github.com/aws/aws-sdk-go/service/s3#S3.GetObjectRequest
func (a *awsS3) GetFileUrl(ctx context.Context, key string) (string, error) {
	logger := a.logger.
		Named("GetFileUrl").
		WithContext(ctx).
		With("key", key)

	// get file url from S3
	req, _ := a.s3.GetObjectRequest(&s3.GetObjectInput{
		Bucket: aws.String(a.cfg.AWS.S3Bucket),
		Key:    aws.String(key),
	})

	// get presigned url
	fileUrl, err := req.Presign(time.Hour * 24 * 7) // 7 days
	if err != nil {
		logger.Error("failed to get file url", "err", err)
		return "", fmt.Errorf("failed to get file url: %w", err)
	}
	logger = logger.With("fileUrl", fileUrl)

	logger.Info("successfully got file url")
	return fileUrl, nil
}
