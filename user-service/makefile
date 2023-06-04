build:
	go build -o ./.bin/user-service cmd/main.go

run: build
	./.bin/user-service

docker-build:
	docker build -t read-n-share-user-service .

docker-run:
	docker run -p 8001:8001 read-n-share-user-service