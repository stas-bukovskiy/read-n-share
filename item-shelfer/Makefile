include .env

build: clean jar-gen

docker-build:
	docker buildx build -t item-shelfer .

run: docker-run

local:
	@echo Starting local docker compose
	docker-compose -f docker-compose.local.yaml up -d --build

clean:
	./gradlew clean

jar-gen:
	./gradlew bootjar

docker-run:
	docker run -p ${SERVER_PORT}:${SERVER_PORT} \
		--env-file .env item-shelfer \
		--name item-shelfer \
		--add-host host.docker.internal:host-gateway