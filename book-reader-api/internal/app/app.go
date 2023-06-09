package app

import (
	"github.com/stas-bukovskiy/read-n-share/book-reader-api/config"
	"github.com/stas-bukovskiy/read-n-share/book-reader-api/internal/api/awss3"
	"github.com/stas-bukovskiy/read-n-share/book-reader-api/internal/api/user"
	httpcontroller "github.com/stas-bukovskiy/read-n-share/book-reader-api/internal/controller/http"
	"github.com/stas-bukovskiy/read-n-share/book-reader-api/internal/service"
	"github.com/stas-bukovskiy/read-n-share/book-reader-api/internal/storage"
	"github.com/stas-bukovskiy/read-n-share/book-reader-api/pkg/database"
	"github.com/stas-bukovskiy/read-n-share/book-reader-api/pkg/httpserver"
	"github.com/stas-bukovskiy/read-n-share/book-reader-api/pkg/logging"
	"os"
	"os/signal"
	"syscall"
	"time"
)

func Run(cfg *config.Config) {
	logger := logging.New("INFO")

	mongodb, err := database.NewMongoDB(database.MongoDBConfig{
		URI:      cfg.MongoDB.URI,
		Database: cfg.MongoDB.Database,
	})
	if err != nil {
		logger.Fatal("failed to init mongodb", "err", err)
	}

	apis := service.APIs{
		File: awss3.New(cfg, logger),
		User: user.New(cfg, logger),
	}

	storages := service.Storages{
		Book: storage.NewBookStorage(mongodb),
	}

	// init services
	servicesOptions := service.Options{
		APIs:     apis,
		Logger:   logger,
		Config:   cfg,
		Storages: storages,
	}

	services := service.Services{
		Auth:   service.NewAuthService(&servicesOptions),
		Upload: service.NewUploadService(&servicesOptions),
		Book:   service.NewBookService(&servicesOptions),
	}

	handler := httpcontroller.New(httpcontroller.Options{
		Config:   cfg,
		Logger:   logger,
		Services: services,
	})

	// init and run http server
	httpServer := httpserver.New(
		handler,
		httpserver.Port(cfg.App.Port),
		httpserver.ReadTimeout(time.Second*60),
		httpserver.WriteTimeout(time.Second*60),
		httpserver.ShutdownTimeout(time.Second*30),
	)

	// waiting signal
	interrupt := make(chan os.Signal, 1)
	signal.Notify(interrupt, os.Interrupt, syscall.SIGTERM)

	select {
	case s := <-interrupt:
		logger.Info("app - Run - signal: " + s.String())

	case err := <-httpServer.Notify():
		logger.Error("app - Run - httpServer.Notify", "err", err)
	}

	// shutdown http server
	err = httpServer.Shutdown()
	if err != nil {
		logger.Error("app - Run - httpServer.Shutdown", "err", err)
	}
}
