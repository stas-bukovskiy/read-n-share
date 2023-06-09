package httpcontroller

import (
	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
	"github.com/stas-bukovskiy/read-n-share/book-reader-api/internal/entity"
	"github.com/stas-bukovskiy/read-n-share/book-reader-api/internal/service"
	"github.com/stas-bukovskiy/read-n-share/book-reader-api/pkg/errs"
	"time"
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
		group.PUT("/:id", errorHandler(options, bookRoutes.updateBook))

	}

	shareLinkGroup := group.Group("/share-link")
	shareLinkGroup.Use(newAuthMiddleware(options))
	{
		shareLinkGroup.POST("/", errorHandler(options, bookRoutes.createShareLink))
		shareLinkGroup.DELETE("/:id", errorHandler(options, bookRoutes.deleteShareLink))
		shareLinkGroup.POST("/:id/share", errorHandler(options, bookRoutes.shareBook))
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

type updateBookRequestBody struct {
	Title       *string `json:"title"`
	Author      *string `json:"author"`
	Description *string `json:"description"`
}

type updateBookResponseBody struct {
	Book *entity.Book `json:"book"`
}

func (b *bookRoutes) updateBook(c *gin.Context) (interface{}, *httpErr) {
	logger := b.logger.Named("updateBook").WithContext(c.Request.Context())

	var body updateBookRequestBody
	if err := c.ShouldBindJSON(&body); err != nil {
		logger.Info("invalid request body")
		return nil, &httpErr{
			Type:    httpErrTypeClient,
			Message: "invalid request body",
			Details: err.Error(),
		}
	}

	_, err := uuid.Parse(c.Param("id"))
	if err != nil {
		logger.Info("invalid book id")
		return nil, &httpErr{
			Type:    httpErrTypeClient,
			Message: "invalid book id",
			Details: err.Error(),
		}
	}

	book, err := b.services.Book.UpdateBook(c.Request.Context(), &service.UpdateBookOptions{
		BookID:      c.Param("id"),
		UserID:      c.Value("userID").(string),
		Title:       body.Title,
		Author:      body.Author,
		Description: body.Description,
	})
	if err != nil {
		logger.Error("failed to update book", "err", err)
		return nil, &httpErr{
			Type:    httpErrTypeServer,
			Message: "failed to update book",
			Details: err.Error(),
		}
	}
	logger = logger.With("book", book)

	logger.Info("book updated")
	return &updateBookResponseBody{
		Book: book,
	}, nil
}

type createShareLinkRequestBody struct {
	BookID    string `json:"book_id"`
	ExpiresIn *int64 `json:"expires_in"`
}

type createShareLinkResponseBody struct {
	ShareLink *entity.BookShareLink `json:"share_link"`
}

func (b *bookRoutes) createShareLink(c *gin.Context) (interface{}, *httpErr) {
	logger := b.logger.Named("createShareLink").WithContext(c.Request.Context())

	var body createShareLinkRequestBody
	if err := c.ShouldBindJSON(&body); err != nil {
		logger.Info("invalid request body")
		return nil, &httpErr{
			Type:    httpErrTypeClient,
			Message: "invalid request body",
			Details: err.Error(),
		}
	}

	_, err := uuid.Parse(body.BookID)
	if err != nil {
		logger.Info("invalid book id")
		return nil, &httpErr{
			Type:    httpErrTypeClient,
			Message: "invalid book id",
			Details: err.Error(),
		}
	}

	expiresAt := time.Now().Add(time.Duration(*body.ExpiresIn) * time.Second)

	shareLink, err := b.services.Book.CreateShareLink(c.Request.Context(), service.CreateShareLinkOptions{
		BookID:    body.BookID,
		UserID:    c.Value("userID").(string),
		ExpiresAt: &expiresAt,
	})
	if err != nil {
		if errs.IsExpected(err) {
			logger.Info("failed to create share link", "err", err)
			return nil, &httpErr{
				Type:    httpErrTypeClient,
				Message: "failed to create share link",
				Details: err.Error(),
			}
		}
		logger.Error("failed to create share link", "err", err)
		return nil, &httpErr{
			Type:    httpErrTypeServer,
			Message: "failed to create share link",
			Details: err.Error(),
		}
	}
	logger = logger.With("share_link", shareLink)

	logger.Info("share link created")
	return createShareLinkResponseBody{
		ShareLink: shareLink,
	}, nil
}

type deleteShareLinkResponseBody struct{}

func (b *bookRoutes) deleteShareLink(c *gin.Context) (interface{}, *httpErr) {
	logger := b.logger.Named("deleteShareLink").WithContext(c.Request.Context())

	_, err := uuid.Parse(c.Param("id"))
	if err != nil {
		logger.Info("invalid share link id")
		return nil, &httpErr{
			Type:    httpErrTypeClient,
			Message: "invalid share link id",
			Details: err.Error(),
		}
	}

	err = b.services.Book.DeleteShareLink(c.Request.Context(), c.Param("id"), c.Value("userID").(string))
	if err != nil {
		if errs.IsExpected(err) {
			logger.Info("failed to delete share link", "err", err)
			return nil, &httpErr{
				Type:    httpErrTypeClient,
				Message: "failed to delete share link",
				Details: err.Error(),
			}
		}
		logger.Error("failed to delete share link", "err", err)
		return nil, &httpErr{
			Type:    httpErrTypeServer,
			Message: "failed to delete share link",
			Details: err.Error(),
		}
	}

	logger.Info("share link deleted")
	return deleteShareLinkResponseBody{}, nil
}

type listShareLinksRequestQuery struct {
	BookID *string `form:"book_id"`
	UserID *string `form:"user_id"`
}

type listShareLinksResponseBody struct {
	ShareLinks []*entity.BookShareLink `json:"share_links"`
}

func (b *bookRoutes) listShareLinks(c *gin.Context) (interface{}, *httpErr) {
	logger := b.logger.Named("listShareLinks").WithContext(c.Request.Context())

	var query listShareLinksRequestQuery
	if err := c.ShouldBindQuery(&query); err != nil {
		logger.Info("invalid request query")
		return nil, &httpErr{
			Type:    httpErrTypeClient,
			Message: "invalid request query",
			Details: err.Error(),
		}
	}

	shareLinks, err := b.services.Book.ListShareLinks(c.Request.Context(), c.Value("userID").(string), &service.ListShareLinksOptions{
		BookID: query.BookID,
		UserID: query.UserID,
	})
	if err != nil {
		if errs.IsExpected(err) {
			logger.Info("failed to list share links", "err", err)
			return nil, &httpErr{
				Type:    httpErrTypeClient,
				Message: "failed to list share links",
				Details: err.Error(),
			}
		}
		logger.Error("failed to list share links", "err", err)
		return nil, &httpErr{
			Type:    httpErrTypeServer,
			Message: "failed to list share links",
			Details: err.Error(),
		}
	}
	logger = logger.With("share_links", shareLinks)

	logger.Info("share links listed")
	return listShareLinksResponseBody{
		ShareLinks: shareLinks,
	}, nil
}

type getShareLinkResponseBody struct {
	Book *entity.Book `json:"book"`
}

func (b *bookRoutes) shareBook(c *gin.Context) (interface{}, *httpErr) {
	logger := b.logger.Named("shareBook").WithContext(c.Request.Context())

	_, err := uuid.Parse(c.Param("id"))
	if err != nil {
		logger.Info("invalid share link id")
		return nil, &httpErr{
			Type:    httpErrTypeClient,
			Message: "invalid share link id",
			Details: err.Error(),
		}
	}

	book, err := b.services.Book.ShareBook(c.Request.Context(), c.Param("id"), c.Value("userID").(string))
	if err != nil {
		if errs.IsExpected(err) {
			logger.Info("failed to share link", "err", err)
			return nil, &httpErr{
				Type:    httpErrTypeClient,
				Message: "failed to get share link",
				Details: err.Error(),
			}
		}
		logger.Error("failed to share link", "err", err)
		return nil, &httpErr{
			Type:    httpErrTypeServer,
			Message: "failed to share link",
			Details: err.Error(),
		}
	}
	logger = logger.With("book", book)

	logger.Info("book shared")
	return getShareLinkResponseBody{
		Book: book,
	}, nil
}
