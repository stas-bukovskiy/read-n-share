# Item-Finder Service

Service for searching movies and books in a database or third-party API such as IMDb and Google Books API

## Prerequirements

1. Installed [git](https://git-scm.com/downloads)
2. [IMDb API key](https://imdb-api.com/)
3. Create `.env` file in item-finder directory:

```env
IMDB_API_TOKEN=<your_imdb_api_key>
GRPC_SERVER_PORT=8000
REST_SERVER_PORT=8080
USER_SERVICE_ADDRESS=host.docker.internal:8001
MONGODB_URI=<mongodb-uri_with_database_name>
```

## Installation and usage

1. Install item-finder with   `git clone`

```bash
  git clone https://github.com/stas-bukovskiy/read-n-share.git
```

2. Inside item-finder folder run `make build` and ` make docker-build` command in order to generate jar file and docker
   image:

```bash
cd item-finder
make build
make docker-build
```

If you see such error after executing previous command:

```bash
./gradlew clean
make: ./gradlew: Permission denied
make: *** [Makefile:8: clean] Error 127
```

Try to set the execution flag on your gradlew file:

```bash
chmod +x gradlew
```

3. After docker container generating you can run application with `make run` command

```bash
make run
```
