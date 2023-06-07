include .env

build: clean jar-gen

docker-build:
	docker buildx build -t item-reviewer .

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
		--env-file .env item-reviewer \
		--name item-reviewer