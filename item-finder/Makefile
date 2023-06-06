include .env

build: clean jar-gen

docker-build:
	docker buildx build -t item-finder .

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
		--env-file .env item-finder \
		--name item-finder \
		--add-host host.docker.internal:host-gateway