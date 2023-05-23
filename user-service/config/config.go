package config

import (
	"github.com/ilyakaznacheev/cleanenv"
	"log"
	"sync"
)

type (
	// Config - represent top level application configuration object.
	Config struct {
		AWS
		App
		MongoDB
	}

	App struct {
		Host       string `env:"USER_SERVICE_HOST" env-default:"localhost"`
		Port       string `env:"USER_SERVICE_PORT" env-default:"8001"`
		HMACSecret string `env:"USER_SERVICE_SECRET" env-default:"bh43yGXui32he7i3iFgq3linjK"`
	}

	AWS struct {
		S3Bucket string `env:"GD_AWS_S3_BUCKET" env-default:"files-controller-staging-godrive-evokia-com"`
		Region   string `env:"GD_AWS_REGION" env-default:"us-east-2"`
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
