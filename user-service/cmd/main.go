package main

import (
	"github.com/stas-bukovskiy/read-n-share/user-service/config"
	"github.com/stas-bukovskiy/read-n-share/user-service/internal/app"
)

func main() {
	cfg := config.Get()
	app.Run(cfg)
}
