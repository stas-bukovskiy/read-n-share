package entity

type Book struct {
	ID          string   `json:"id" bson:"_id"`
	OwnerUserID string   `json:"owner_user_id" bson:"owner_user_id"`
	GuestsIDs   []string `json:"guests_ids" bson:"guests_ids"`

	Title       string `json:"title" bson:"title"`
	Author      string `json:"author" bson:"author"`
	Description string `json:"description" bson:"description"`

	Chapters []string `json:"chapters" bson:"chapters"`
}
