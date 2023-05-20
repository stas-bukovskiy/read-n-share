build:
	go build -o ./.bin/user-service cmd/main.go

run: build
	./.bin/user-service

proto-gen:
	protoc --go_out=. --go_opt=paths=source_relative --go-grpc_out=. --go-grpc_opt=paths=source_relative internal/api/grpc/user.proto