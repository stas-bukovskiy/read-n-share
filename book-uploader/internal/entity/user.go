package entity

type User struct {
	ID       string `json:"id" bson:"_id,omitempty"`
	Email    string `json:"email" bson:"email"`
	Username string `json:"username" bson:"username"`
	Role     string `json:"role" bson:"role"`
}
