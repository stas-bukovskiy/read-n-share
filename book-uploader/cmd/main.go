package main

import (
	"github.com/stas-bukovskiy/read-n-share/book-uploader/config"
	"github.com/stas-bukovskiy/read-n-share/book-uploader/internal/app"
)

func main() {
	cfg := config.Get()
	app.Run(cfg)
}
