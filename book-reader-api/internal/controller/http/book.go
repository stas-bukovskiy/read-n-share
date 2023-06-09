package httpcontroller

import (
	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
	"github.com/stas-bukovskiy/read-n-share/book-reader-api/internal/entity"
	"github.com/stas-bukovskiy/read-n-share/book-reader-api/internal/service"
	"net/http"
)

type bookRoutes struct {
	routerContext
}

func setupBookRoutes(options routerOptions) {
	bookRoutes := bookRoutes{
		routerContext: routerContext{
			services: options.services,
			cfg:      options.cfg,
			logger:   options.logger.Named("bookRoutes"),
		},
	}

	group := options.router.Group("/book")
	group.Use(newAuthMiddleware(options))
	{
		group.GET("/list", errorHandler(options, bookRoutes.listBooks))
		group.GET("/:id", errorHandler(options, bookRoutes.getBook))
		group.GET("/:id/url", errorHandler(options, bookRoutes.getBookURL))
		group.GET("/:id/content", errorHandler(options, bookRoutes.getBookContent))
	}
}

type listBooksResponseBody struct {
	Books []*entity.Book `json:"books"`
}

func (b *bookRoutes) listBooks(c *gin.Context) (interface{}, *httpErr) {
	logger := b.logger.Named("listBooks").WithContext(c.Request.Context())

	books, err := b.services.Book.ListBooks(c.Request.Context(), &service.BookServiceListOptions{
		UserID: c.Value("userID").(string),
	})
	if err != nil {
		logger.Error("failed to list books", "err", err)
		return nil, &httpErr{
			Type:    httpErrTypeServer,
			Message: "failed to list books",
			Details: err.Error(),
		}
	}
	logger = logger.With("books", books)

	logger.Info("books listed")
	return &listBooksResponseBody{
		Books: books,
	}, nil
}

type getBookResponseBody struct {
	Book *entity.Book `json:"book"`
}

func (b *bookRoutes) getBook(c *gin.Context) (interface{}, *httpErr) {
	logger := b.logger.Named("getBook").WithContext(c.Request.Context())

	_, err := uuid.Parse(c.Param("id"))
	if err != nil {
		logger.Info("invalid book id")
		return nil, &httpErr{
			Type:    httpErrTypeClient,
			Message: "invalid book id",
			Details: err.Error(),
		}
	}

	book, err := b.services.Book.GetBook(c.Request.Context(), c.Param("id"), c.Value("userID").(string))
	if err != nil {
		logger.Error("failed to get book", "err", err)
		return nil, &httpErr{
			Type:    httpErrTypeServer,
			Message: "failed to get book",
			Details: err.Error(),
		}
	}
	logger = logger.With("book", book)

	logger.Info("book retrieved")
	return &getBookResponseBody{
		Book: book,
	}, nil
}

type getBookURLResponseBody struct {
	URL string `json:"url"`
}

func (b *bookRoutes) getBookURL(c *gin.Context) (interface{}, *httpErr) {
	logger := b.logger.Named("getBookURL").WithContext(c.Request.Context())

	_, err := uuid.Parse(c.Param("id"))
	if err != nil {
		logger.Info("invalid book id")
		return nil, &httpErr{
			Type:    httpErrTypeClient,
			Message: "invalid book id",
			Details: err.Error(),
		}
	}

	url, err := b.services.Book.GetBookURL(c.Request.Context(), c.Param("id"), c.Value("userID").(string))
	if err != nil {
		logger.Error("failed to get book url", "err", err)
		return nil, &httpErr{
			Type:    httpErrTypeServer,
			Message: "failed to get book url",
			Details: err.Error(),
		}
	}
	logger = logger.With("url", url)

	logger.Info("book url retrieved")
	return &getBookURLResponseBody{
		URL: url,
	}, nil
}

func (b *bookRoutes) getBookContent(c *gin.Context) (interface{}, *httpErr) {
	logger := b.logger.Named("getBookContent").WithContext(c.Request.Context())

	_, err := uuid.Parse(c.Param("id"))
	if err != nil {
		logger.Info("invalid book id")
		return nil, &httpErr{
			Type:    httpErrTypeClient,
			Message: "invalid book id",
			Details: err.Error(),
		}
	}

	content, err := b.services.Book.GetBookContent(c.Request.Context(), c.Param("id"), c.Value("userID").(string))
	if err != nil {
		logger.Error("failed to get book content", "err", err)
		return nil, &httpErr{
			Type:    httpErrTypeServer,
			Message: "failed to get book content",
			Details: err.Error(),
		}
	}
	logger = logger.With("content", content)

	c.Data(http.StatusOK, "application/epub+zip", content)

	logger.Info("book content retrieved")
	return nil, nil
}
