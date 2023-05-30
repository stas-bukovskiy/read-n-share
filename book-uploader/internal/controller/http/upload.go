package httpcontroller

import (
	"github.com/gin-gonic/gin"
	"github.com/stas-bukovskiy/read-n-share/book-uploader/internal/entity"
	"github.com/stas-bukovskiy/read-n-share/book-uploader/internal/service"
	"mime/multipart"
)

type uploadRoutes struct {
	routerContext
}

func setupUploadRoutes(options routerOptions) {
	uploadRoutes := uploadRoutes{
		routerContext: routerContext{
			services: options.services,
			cfg:      options.cfg,
			logger:   options.logger.Named("uploadRoutes"),
		},
	}

	group := options.router.Group("/upload")
	//group.Use(newAuthMiddleware(options))
	{
		group.POST("/book", errorHandler(options, uploadRoutes.uploadBook))
	}
}

type uploadBookResponseBody struct {
	CreatedBook *entity.Book `json:"createdBook"`
}

func (u *uploadRoutes) uploadBook(c *gin.Context) (interface{}, *httpErr) {
	logger := u.logger.Named("uploadBook").WithContext(c.Request.Context())

	file, header, err := c.Request.FormFile("file")
	if err != nil {
		logger.Info("failed to get file from request")
		return nil, &httpErr{
			Type:    httpErrTypeClient,
			Message: "invalid file",
			Details: err.Error(),
		}
	}
	defer func(file multipart.File) {
		err := file.Close()
		if err != nil {
			logger.Error("failed to close file", "err", err)
		}
	}(file)

	book, err := u.services.Upload.UploadBook(c.Request.Context(), &service.UploadBookOptions{
		File:   file,
		Header: header,
	})
	if err != nil {
		logger.Error("failed to upload book", "err", err)
		return nil, &httpErr{
			Type:    httpErrTypeServer,
			Message: "failed to upload book",
		}
	}
	logger = logger.With("book", book)

	logger.Info("book uploaded")
	return uploadBookResponseBody{
		CreatedBook: book,
	}, nil
}
