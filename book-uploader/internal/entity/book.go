package entity

type Book struct {
	ID          string `json:"id"`
	OwnerUserID string `json:"owner_user_id"`
}
