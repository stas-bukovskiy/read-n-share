package service

import (
	"context"
	"fmt"
	"github.com/google/uuid"
	"github.com/stas-bukovskiy/read-n-share/book-uploader/internal/entity"
	"io"
	"os"
	"os/exec"
	"strings"
)

type uploadService struct {
	serviceContext
}

func NewUploadService(options *Options) *uploadService {
	return &uploadService{
		serviceContext: serviceContext{
			apis:   options.APIs,
			cfg:    options.Config,
			logger: options.Logger.Named("UploadService"),
		},
	}
}

func (s *uploadService) UploadBook(ctx context.Context, options *UploadBookOptions) (*entity.Book, error) {
	logger := s.logger.
		Named("UploadBook.js").
		WithContext(ctx)

	bookId := uuid.NewString()
	logger = logger.With("bookId", bookId)

	bookFileFormat, err := getFileFormat(options.Header.Filename)
	if err != nil {
		logger.Error("failed to get file format", "err", err)
		return nil, fmt.Errorf("failed to get file format: %w", err)
	}

	var bookReader io.Reader
	bookReader = options.File

	// If file format is not epub, convert it to epub
	if bookFileFormat != "epub" {
		epubBook, err := s.convertToEpub(ctx, options)
		if err != nil {
			logger.Error("failed to convert file", "err", err)
			return nil, fmt.Errorf("failed to convert file: %w", err)
		}
		defer func() {
			err := os.Remove(epubBook.Name())
			if err != nil {
				logger.Error("failed to remove file", "err", err)
			}
		}()
		bookReader = epubBook
	}

	err = s.apis.File.UploadFile(ctx, bookId, bookReader)
	if err != nil {
		logger.Error("failed to upload file to storage", "err", err)
		return nil, fmt.Errorf("failed to upload file to storage: %w", err)
	}

	book := &entity.Book{
		ID:          bookId,
		OwnerUserID: fmt.Sprintf("%s", ctx.Value("userId")),
	}

	logger.Info("book uploaded")
	return book, nil
}

func (s *uploadService) convertToEpub(ctx context.Context, options *UploadBookOptions) (*os.File, error) {
	logger := s.logger.
		Named("convertToEpub").
		WithContext(ctx)

	bookFileName, err := generateFileName(uuid.NewString(), options.Header.Filename)
	if err != nil {
		logger.Error("failed to generate file name", "err", err)
		return nil, fmt.Errorf("failed to generate file name: %w", err)
	}
	bookFileName = "local/" + bookFileName
	logger = logger.With("bookFileName", bookFileName)

	bookFile, err := os.Create(bookFileName)
	if err != nil {
		logger.Error("failed to create file", "err", err)
		return nil, fmt.Errorf("failed to create file: %w", err)
	}
	defer func(out *os.File) {
		err := out.Close()
		if err != nil {
			if err == os.ErrClosed || err == os.ErrNotExist {
				return
			}
			logger.Error("failed to close file", "err", err)
		}
	}(bookFile)

	_, err = io.Copy(bookFile, options.File)
	if err != nil {
		logger.Error("failed to copy file", "err", err)
		return nil, fmt.Errorf("failed to copy file: %w", err)
	}

	epubBookFileName, err := generateEpubFileName(bookFileName)
	if err != nil {
		logger.Error("failed to generate file name", "err", err)
		return nil, fmt.Errorf("failed to generate file name: %w", err)
	}
	logger = logger.With("epubBookFileName", epubBookFileName)

	cmd := exec.Command("ebook-convert", bookFileName, epubBookFileName)
	err = cmd.Run()
	if err != nil {
		logger.Error("failed to convert file", "err", err)
		return nil, fmt.Errorf("failed to convert file: %w", err)
	}
	logger.Info("file converted", "epubBookFileName", epubBookFileName)

	epubBook, err := os.Open(epubBookFileName)
	if err != nil {
		logger.Error("failed to open file", "err", err)
		return nil, fmt.Errorf("failed to open file: %w", err)
	}

	return epubBook, nil
}

func generateFileName(id, file string) (string, error) {
	fileNameParts := strings.Split(file, ".")
	if len(fileNameParts) < 2 {
		return "", fmt.Errorf("invalid file name: %s", file)
	}
	fileFormat := fileNameParts[len(fileNameParts)-1]
	fileName := strings.Join(fileNameParts[:len(fileNameParts)-1], "-")

	return fmt.Sprintf("%s-%s.%s", fileName, id, fileFormat), nil
}

func getFileFormat(filename string) (string, error) {
	fileNameParts := strings.Split(filename, ".")
	if len(fileNameParts) < 2 {
		return "", fmt.Errorf("invalid file name: %s", filename)
	}
	return fileNameParts[len(fileNameParts)-1], nil
}

func generateEpubFileName(filename string) (string, error) {
	fileNameParts := strings.Split(filename, ".")
	if len(fileNameParts) < 2 {
		return "", fmt.Errorf("invalid file name: %s", filename)
	}
	fileNameParts[len(fileNameParts)-1] = "epub"
	return strings.Join(fileNameParts, "."), nil
}
