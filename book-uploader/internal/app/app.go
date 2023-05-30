package app

import (
	"fmt"
	"github.com/stas-bukovskiy/read-n-share/book-uploader/config"
	"github.com/stas-bukovskiy/read-n-share/book-uploader/internal/api/awss3"
	pb "github.com/stas-bukovskiy/read-n-share/book-uploader/internal/controller/grpc"
	httpcontroller "github.com/stas-bukovskiy/read-n-share/book-uploader/internal/controller/http"
	"github.com/stas-bukovskiy/read-n-share/book-uploader/internal/service"
	"github.com/stas-bukovskiy/read-n-share/book-uploader/pkg/httpserver"
	"github.com/stas-bukovskiy/read-n-share/book-uploader/pkg/logging"
	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials/insecure"
	"os"
	"os/signal"
	"syscall"
	"time"
)

func Run(cfg *config.Config) {
	logger := logging.New("INFO")

	conn, err := grpc.Dial(fmt.Sprintf("%s:%s", cfg.Auth.Host, cfg.Auth.Port), grpc.WithTransportCredentials(insecure.NewCredentials()))
	if err != nil {
		logger.Fatal("failed to connect to auth service", "err", err)
	}
	defer func(conn *grpc.ClientConn) {
		err := conn.Close()
		if err != nil {
			logger.Error("failed to close connection", "err", err)
		}
	}(conn)

	authClient := pb.NewUserServiceClient(conn)

	apis := service.APIs{
		File: awss3.New(cfg, logger),
	}

	// init services
	servicesOptions := service.Options{
		AuthClient: authClient,
		APIs:       apis,
		Logger:     logger,
		Config:     cfg,
	}

	services := service.Services{
		Auth:   service.NewAuthService(&servicesOptions),
		Upload: service.NewUploadService(&servicesOptions),
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

	case err = <-httpServer.Notify():
		logger.Error("app - Run - httpServer.Notify", "err", err)
	}

	// shutdown http server
	err = httpServer.Shutdown()
	if err != nil {
		logger.Error("app - Run - httpServer.Shutdown", "err", err)
	}
}
