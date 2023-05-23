package storage

import (
	"context"
	"github.com/google/uuid"
	pb "github.com/stas-bukovskiy/read-n-share/user-service/internal/controller/grpc"
	"github.com/stas-bukovskiy/read-n-share/user-service/internal/service"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
	"google.golang.org/protobuf/types/known/timestamppb"
	"time"
)

type userDTO struct {
	ID        string    `bson:"_id"`
	Email     string    `bson:"email"`
	Username  string    `bson:"username"`
	Password  string    `bson:"password"`
	Role      string    `bson:"role"`
	CreatedAt time.Time `bson:"created_at"`
	UpdatedAt time.Time `bson:"updated_at"`
}

func (u *userDTO) fromEntity(user *pb.User) {
	u.ID = user.Id
	u.Email = user.Email
	u.Username = user.Name
	u.Password = user.Password
	u.Role = user.Role
	if user.CreatedAt != nil {
		u.CreatedAt = user.CreatedAt.AsTime()
	}
	if user.UpdatedAt != nil {
		u.UpdatedAt = user.UpdatedAt.AsTime()
	}
}

func (u *userDTO) toEntity() *pb.User {
	return &pb.User{
		Id:        u.ID,
		Email:     u.Email,
		Name:      u.Username,
		Password:  u.Password,
		Role:      u.Role,
		CreatedAt: &timestamppb.Timestamp{Seconds: u.CreatedAt.Unix()},
		UpdatedAt: &timestamppb.Timestamp{Seconds: u.UpdatedAt.Unix()},
	}
}

func (m *MongoDB) CreateUser(user *pb.User) (*pb.User, error) {
	u := &userDTO{}
	u.fromEntity(user)
	u.ID = uuid.NewString()
	u.CreatedAt = time.Now()
	u.UpdatedAt = time.Now()

	result, err := m.DB.Collection("user").InsertOne(context.TODO(), u)
	if err != nil {
		return nil, err
	}

	createdUser := &userDTO{}
	err = m.DB.Collection("user").
		FindOne(context.TODO(), bson.M{"_id": result.InsertedID}).
		Decode(createdUser)
	if err != nil {
		return nil, err
	}
	if createdUser == nil {
		return nil, nil
	}

	return createdUser.toEntity(), nil
}

func (m *MongoDB) GetUser(filter *service.GetUserFilter) (*pb.User, error) {
	filterStmt := bson.M{}
	if filter.ID != "" {
		filterStmt = bson.M{"_id": filter.ID}
	}
	if filter.Email != "" {
		filterStmt = bson.M{"email": filter.Email}
	}
	if filter.Username != "" {
		filterStmt = bson.M{"username": filter.Username}
	}

	user := &userDTO{}
	err := m.DB.Collection("user").FindOne(context.TODO(), filterStmt).Decode(user)
	if err != nil {
		if err == mongo.ErrNoDocuments {
			return nil, nil
		}
		return nil, err
	}

	return user.toEntity(), nil
}
