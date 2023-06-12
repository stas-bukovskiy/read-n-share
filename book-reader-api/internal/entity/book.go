package entity

import "time"

type Book struct {
	ID          string   `json:"id" bson:"_id"`
	OwnerUserID string   `json:"owner_user_id" bson:"owner_user_id"`
	GuestsIDs   []string `json:"guests_ids" bson:"guests_ids"`

	Title       string `json:"title" bson:"title"`
	Author      string `json:"author" bson:"author"`
	Description string `json:"description" bson:"description"`

	Chapters []string `json:"chapters" bson:"chapters"`
}

type UserBook struct {
	Book     *Book             `json:"book"`
	IsGuest  bool              `json:"is_guest"`
	Settings *BookUserSettings `json:"settings"`
}

type BookUserSettings struct {
	UserID   string `json:"user_id" bson:"user_id"`
	BookID   string `json:"bookID" bson:"book_id"`
	Location string `json:"location" bson:"location"`
	Chapter  string `json:"chapter" bson:"chapter"`
	Progress int    `json:"progress" bson:"progress"`
	Colour   string `json:"colour" bson:"colour"`
	Username string `json:"username" bson:"username"`

	Selections []*BookUserSelection `json:"selections" bson:"selections"`
}

type BookSync struct {
	BookID string `json:"book_id" bson:"book_id"`

	Ticker    *time.Ticker              `json:"-" bson:"-"`
	IsTicking bool                      `json:"-" bson:"-"`
	Users     []*BookSyncConnectedUsers `json:"connected_users" bson:"connected_users"`
}

type BookSyncConnectedUsers struct {
	UserSettings   *BookUserSettings `json:"user_settings" bson:"user_settings"`
	UpdatesChannel chan *BookSync    `json:"-" bson:"-"`
}

type BookUserSelection struct {
	UserID   string `json:"user_id" bson:"user_id"`
	BookID   string `json:"book_id" bson:"book_id"`
	CFIRange string `json:"cfi_range" bson:"cfi_range"`
	Text     string `json:"text" bson:"text"`
	UserText string `json:"user_text" bson:"user_text"`
}

type BookShareLink struct {
	ID          string     `json:"id" bson:"_id"`
	BookID      string     `json:"book_id" bson:"book_id"`
	OwnerUserID string     `json:"owner_user_id" bson:"owner_user_id"`
	ExpiresAt   *time.Time `json:"expires_at" bson:"expire_at"`
}
