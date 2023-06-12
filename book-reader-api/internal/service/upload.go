package service

import (
	"context"
	"fmt"
	"github.com/google/uuid"
	"github.com/kapmahc/epub"
	"github.com/stas-bukovskiy/read-n-share/book-reader-api/internal/entity"

	"io"
	"mime/multipart"
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
			apis:     options.APIs,
			cfg:      options.Config,
			logger:   options.Logger.Named("UploadService"),
			storages: options.Storages,
		},
	}
}

func (s *uploadService) UploadBook(ctx context.Context, options *UploadBookOptions) (*entity.Book, error) {
	logger := s.logger.
		Named("UploadBook.js").
		WithContext(ctx)

	bookId := uuid.NewString()
	book := &entity.Book{
		ID:          bookId,
		OwnerUserID: fmt.Sprintf("%s", ctx.Value("userID")),

		Title:       options.Title,
		Author:      options.Author,
		Description: options.Description,
	}
	logger = logger.With("bookId", bookId)

	bookReader, err := s.processBook(ctx, processBookOptions{
		BookReader: options.File,
		BookHeader: options.Header,
		Book:       book,
	})

	err = s.apis.File.UploadFile(ctx, bookId, bookReader)
	if err != nil {
		logger.Error("failed to upload file to storage", "err", err)
		return nil, fmt.Errorf("failed to upload file to storage: %w", err)
	}

	createdBook, err := s.storages.Book.Save(ctx, book)
	if err != nil {
		logger.Error("failed to save book", "err", err)
		return nil, fmt.Errorf("failed to save book: %w", err)
	}
	logger = logger.With("book", createdBook)

	var firstChapter string
	if len(createdBook.Chapters) > 0 {
		firstChapter = createdBook.Chapters[0]
	}
	bookUserSettings, err := s.storages.Book.SaveUserBookSettings(ctx, &entity.BookUserSettings{
		BookID:   bookId,
		UserID:   fmt.Sprintf("%s", ctx.Value("userID")),
		Location: "epubcfi(/6/2[cover]!/6)",
		Chapter:  firstChapter,
	})
	if err != nil {
		logger.Error("failed to save book user settings", "err", err)
		return nil, fmt.Errorf("failed to save book user settings: %w", err)
	}
	logger = logger.With("bookUserSettings", bookUserSettings)

	logger.Info("book uploaded")
	return book, nil
}

type processBookOptions struct {
	BookReader io.Reader
	BookHeader *multipart.FileHeader
	Book       *entity.Book
}

func (s *uploadService) processBook(ctx context.Context, options processBookOptions) (io.Reader, error) {
	logger := s.logger.
		Named("processBook").
		WithContext(ctx)

	bookFileFormat, err := getFileFormat(options.BookHeader.Filename)
	if err != nil {
		logger.Error("failed to get file format", "err", err)
		return nil, fmt.Errorf("failed to get file format: %w", err)
	}

	var bookReader = options.BookReader
	var epubBook *os.File

	// If file format is not epub, convert it to epub
	if bookFileFormat != "epub" {
		epubBook, err = s.convertToEpub(ctx, convertToEpubOptions{
			BookReader: options.BookReader,
			BookHeader: options.BookHeader,
		})
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
	} else {
		bookFileName, err := generateFileName(uuid.NewString(), options.BookHeader.Filename)
		if err != nil {
			logger.Error("failed to generate file name", "err", err)
			return nil, fmt.Errorf("failed to generate file name: %w", err)
		}
		bookFileName = "local/" + bookFileName

		epubBook, err = os.Create(bookFileName)
		if err != nil {
			logger.Error("failed to create file", "err", err)
			return nil, fmt.Errorf("failed to create file: %w", err)
		}
		defer func(out *os.File) {
			err := os.Remove(out.Name())
			if err != nil {
				logger.Error("failed to remove file", "err", err)
			}
		}(epubBook)

		_, err = io.Copy(epubBook, options.BookReader)
		if err != nil {
			logger.Error("failed to copy file", "err", err)
			return nil, fmt.Errorf("failed to copy file: %w", err)
		}
	}

	epubContent, err := epub.Open(epubBook.Name())
	if err != nil {
		logger.Error("failed to open epub file", "err", err)
		return nil, fmt.Errorf("failed to open epub file: %w", err)
	}

	chapters := make([]string, 0)
	extractChapters(epubContent.Ncx.Points, &chapters)
	options.Book.Chapters = chapters

	if options.Book.Title == "" && len(epubContent.Opf.Metadata.Title) > 0 {
		options.Book.Title = epubContent.Opf.Metadata.Title[0]
	}

	if options.Book.Author == "" && len(epubContent.Opf.Metadata.Creator) > 0 {
		for _, creator := range epubContent.Opf.Metadata.Creator {
			if creator.Role == "aut" {
				options.Book.Author = creator.Data
				break
			}
		}
	}

	if options.Book.Description == "" && len(epubContent.Opf.Metadata.Description) > 0 {
		options.Book.Description = epubContent.Opf.Metadata.Description[0]
	}

	return bookReader, nil
}

func extractChapters(npList []epub.NavPoint, chapters *[]string) {
	for _, np := range npList {
		*chapters = append(*chapters, np.Text)
		extractChapters(np.Points, chapters)
	}
}

type convertToEpubOptions struct {
	BookReader io.Reader
	BookHeader *multipart.FileHeader
}

func (s *uploadService) convertToEpub(ctx context.Context, options convertToEpubOptions) (*os.File, error) {
	logger := s.logger.
		Named("convertToEpub").
		WithContext(ctx)

	bookFileName, err := generateFileName(uuid.NewString(), options.BookHeader.Filename)
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
		err := os.Remove(out.Name())
		if err != nil {
			logger.Error("failed to remove file", "err", err)
		}
	}(bookFile)

	_, err = io.Copy(bookFile, options.BookReader)
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

	ep, err := epub.Open(epubBookFileName)
	if err != nil {
		logger.Error("failed to open epub file", "err", err)
		return nil, fmt.Errorf("failed to open epub file: %w", err)
	}

	logger = logger.With("chapters", ep.Ncx.Points)

	logger.Info("file converted")
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
