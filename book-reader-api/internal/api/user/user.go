package user

import (
	"context"
	"encoding/json"
	"fmt"
	"github.com/stas-bukovskiy/read-n-share/book-reader-api/internal/entity"
	"github.com/stas-bukovskiy/read-n-share/book-reader-api/internal/service"
	"github.com/stas-bukovskiy/read-n-share/book-reader-api/pkg/errs"
)

type getUserResponseBody struct {
	User *entity.User `json:"user"`
}

func (u *userAPI) GetUser(ctx context.Context, options *service.GetUserOptions) (*entity.User, error) {
	logger := u.logger.Named("GetUser").
		WithContext(ctx).
		With("options", options)

	var respBody getUserResponseBody
	resp, err := u.resty.R().
		SetQueryParams(map[string]string{
			"id":       *options.ID,
			"email":    *options.Email,
			"username": *options.Username,
		}).
		SetResult(&respBody).
		Get("/users")
	if err != nil {
		logger.Error("failed to get user", "err", err)
		return nil, fmt.Errorf("failed to get user: %w", err)
	}
	if resp.IsError() || resp.StatusCode() != 200 {
		if resp.StatusCode() == 422 {
			var apiErr apiError
			if err := json.Unmarshal(resp.Body(), &apiErr); err != nil {
				return nil, fmt.Errorf("failed to send SMS, status code: %d", resp.StatusCode())
			}
			logger = logger.With("apiErr", apiErr)

			logger.Info("unable to get user", "reason", "unauthorized")
			return nil, &errs.Err{
				Code:    apiErr.Code,
				Message: apiErr.Message,
			}
		}
		logger.Info("unable to get user", "reason", resp.Status())
		return nil, fmt.Errorf("unable to get user: %s, body: %s", resp.Status(), resp.Body())
	}

	logger.Info("token verified")
	return respBody.User, nil

}
