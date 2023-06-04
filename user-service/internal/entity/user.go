package entity

type User struct {
	ID       string `json:"id" bson:"_id,omitempty"`
	Email    string `json:"email" bson:"email"`
	Username string `json:"username" bson:"username"`
	Password string `json:"password,omitempty" bson:"password"`
	Role     string `json:"role" bson:"role"`
}
