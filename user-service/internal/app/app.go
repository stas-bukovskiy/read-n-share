package app

import (
	"fmt"
	"github.com/stas-bukovskiy/read-n-share/user-service/config"
	pb1 "github.com/stas-bukovskiy/read-n-share/user-service/internal/controller/grpc"
	"github.com/stas-bukovskiy/read-n-share/user-service/internal/service"
	"github.com/stas-bukovskiy/read-n-share/user-service/internal/storage"
	"google.golang.org/grpc"
	"log"
	"net"
	"os"
	"os/signal"
	"syscall"
)

func Run(cfg *config.Config) {
	log.Printf("Listening on %s:%s", cfg.Host, cfg.Port)
	lis, err := net.Listen("tcp", fmt.Sprintf("%s:%s", cfg.Host, cfg.Port))
	if err != nil {
		log.Fatalf("failed to listen: %v", err)
	}

	userStorage, err := storage.NewMongoDB(storage.MongoDBConfig{
		URI:      cfg.MongoDB.URI,
		Database: cfg.MongoDB.Database,
	})
	if err != nil {
		log.Fatalf("failed to connect to mongodb: %v", err)
	}

	var opts []grpc.ServerOption
	grpcServer := grpc.NewServer(opts...)
	pb1.RegisterUserServiceServer(grpcServer,
		service.NewUserService(&service.Options{
			Config:  cfg,
			Storage: userStorage,
		},
		))

	go func() {
		log.Println("Starting gRPC server")
		err = grpcServer.Serve(lis)
		if err != nil {
			return
		}
	}()

	// Waiting signal
	interrupt := make(chan os.Signal, 1)
	signal.Notify(interrupt, os.Interrupt, syscall.SIGTERM)

	select {
	case s := <-interrupt:
		log.Println("app - Run - signal: " + s.String())
	}

	grpcServer.GracefulStop()
}
