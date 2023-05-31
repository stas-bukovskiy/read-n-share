include .env

build: clean proto-gen jar-gen

docker-build:
	docker buildx build -t item-finder .

run: docker-run

local:
	@echo Starting local docker compose
	docker-compose -f docker-compose.local.yaml up -d --build

clean:
	./gradlew clean

proto-gen:
	./gradlew generateProto

jar-gen:
	./gradlew bootjar

docker-run:
	docker run -p ${GRPC_SERVER_PORT}:${GRPC_SERVER_PORT} \
		-p ${REST_SERVER_PORT}:${REST_SERVER_PORT} \
		--env-file .env item-finder \
		--name item-finder \
		--add-host host.docker.internal:host-gateway