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
	}

	App struct {
		Host string `env:"USER_SERVICE_HOST" env-default:"localhost"`
		Port string `env:"USER_SERVICE_PORT" env-default:"8001"`
	}

	AWS struct {
		S3Bucket string `env:"GD_AWS_S3_BUCKET" env-default:"files-api-staging-godrive-evokia-com"`
		Region   string `env:"GD_AWS_REGION" env-default:"us-east-2"`
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
