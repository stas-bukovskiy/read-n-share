package config

import (
	"github.com/ilyakaznacheev/cleanenv"
	"log"
	"sync"
)

type (
	// Config - represent top level application configuration object.
	Config struct {
		App
		Auth
		AWS
		MongoDB
	}

	App struct {
		Host string `env:"USER_SERVICE_HOST" env-default:"localhost"`
		Port string `env:"USER_SERVICE_PORT" env-default:"8002"`
	}

	Auth struct {
		Host string `env:"AUTH_SERVICE_HOST" env-default:"http://localhost:8001/api/v1"`
	}

	AWS struct {
		S3Bucket           string `env:"BOOKS_AWS_S3_BUCKET" env-default:"read-n-share-books-bucket"`
		Region             string `env:"BOOKS_AWS_REGION" env-default:"us-east-1"`
		IAMUserAccessToken string `env:"BOOKS_AWS_IAM_USER_ACCESS_TOKEN" env-default:""`
		IAMUserSecretKey   string `env:"BOOKS_AWS_IAM_USER_SECRET_KEY" env-default:""`
	}

	MongoDB struct {
		URI      string `env:"MONGODB_URI" env-default:"mongodb+srv://vdpolishchuk:Lxv2eJbM0cSydOJI@read-n-share-db.wlxut51.mongodb.net/?retryWrites=true&w=majority"`
		Database string `env:"MONGODB_DATABASE" env-default:"user"`
	}
)

var (
	config Config
	once   sync.Once
)

// Get returns config.
func Get() *Config {
	once.Do(func() {
		err := cleanenv.ReadEnv(&config)
		if err != nil {
			log.Fatal("failed to read env", err)
		}
	})

	return &config
}
