package app

import (
	"github.com/stas-bukovskiy/read-n-share/user-service/config"
	httpcontroller "github.com/stas-bukovskiy/read-n-share/user-service/internal/controller/http"
	"github.com/stas-bukovskiy/read-n-share/user-service/internal/service"
	"github.com/stas-bukovskiy/read-n-share/user-service/internal/storage"
	"github.com/stas-bukovskiy/read-n-share/user-service/pkg/httpserver"
	"github.com/stas-bukovskiy/read-n-share/user-service/pkg/logging"
	"log"
	"os"
	"os/signal"
	"syscall"
	"time"
)

func Run(cfg *config.Config) {
	logger := logging.New("INFO")

	userStorage, err := storage.NewMongoDB(storage.MongoDBConfig{
		URI:      cfg.MongoDB.URI,
		Database: cfg.MongoDB.Database,
	})
	if err != nil {
		log.Fatalf("failed to connect to mongodb: %v", err)
	}
	logger.Info("Connected to MongoDB")

	storages := service.Storages{
		User: userStorage,
	}

	servicesOptions := service.Options{
		Storages: storages,
		Logger:   logger,
		Config:   cfg,
	}

	services := service.Services{
		Auth: service.NewAuthService(&servicesOptions),
		User: service.NewUserService(&servicesOptions),
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

	// Waiting signal
	interrupt := make(chan os.Signal, 1)
	signal.Notify(interrupt, os.Interrupt, syscall.SIGTERM)

	select {
	case s := <-interrupt:
		log.Println("app - Run - signal: " + s.String())
	case err = <-httpServer.Notify():
		logger.Error("app - Run - httpServer.Notify", "err", err)
	}

	// shutdown http server
	err = httpServer.Shutdown()
	if err != nil {
		logger.Error("app - Run - httpServer.Shutdown", "err", err)
	}
}
