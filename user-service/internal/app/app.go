package app

import (
	"fmt"
	"github.com/stas-bukovskiy/read-n-share/user-service/config"
	pb "github.com/stas-bukovskiy/read-n-share/user-service/internal/api/grpc"
	"github.com/stas-bukovskiy/read-n-share/user-service/internal/service"
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

	var opts []grpc.ServerOption
	grpcServer := grpc.NewServer(opts...)
	pb.RegisterUserServiceServer(grpcServer, service.NewUserService())

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
