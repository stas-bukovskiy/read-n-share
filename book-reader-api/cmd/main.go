package main

import (
	"github.com/stas-bukovskiy/read-n-share/book-reader-api/config"
	"github.com/stas-bukovskiy/read-n-share/book-reader-api/internal/app"
)

func main() {
	cfg := config.Get()
	app.Run(cfg)
}
