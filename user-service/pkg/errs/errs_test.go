package errs

import (
	"errors"
	"github.com/stretchr/testify/assert"
	"testing"
)

func TestErrs_GetDetails(t *testing.T) {
	t.Parallel()
	testCases := []struct {
		name       string
		inputErr   error
		want       map[string]string
		wantLength int
	}{
		{
			name: "GetDetails",
			inputErr: &Err{
				Message: "test",
				Details: map[string]string{"success": "yes"},
			},
			want:       map[string]string{"success": "yes"},
			wantLength: 1,
		},
		{
			name: "GetDetails with nil map",
			inputErr: &Err{
				Message: "test",
				Details: nil,
			},
			want:       nil,
			wantLength: 0,
		},
		{
			name: "GetDetails with empty map",
			inputErr: &Err{
				Message: "test",
				Details: map[string]string{},
			},
			want:       map[string]string{},
			wantLength: 0,
		},
		{
			name:       "GetDetails with not custom error",
			inputErr:   errors.New("not custom"),
			want:       nil,
			wantLength: 0,
		},
	}
	for _, tc := range testCases {
		tc := tc
		t.Run(tc.name, func(t *testing.T) {
			t.Parallel()
			got := GetDetails(tc.inputErr)
			assert.Equalf(t, tc.want, got, "details are not equal: got: %v want: %v", got, tc.want)
			assert.Equalf(t, tc.wantLength, len(got), "details length is not equal: got: %v want: %v", len(got), tc.wantLength)
		})
	}
}

func TestErrs_GetCode(t *testing.T) {
	t.Parallel()
	testCases := []struct {
		name     string
		inputErr error
		want     string
	}{
		{
			name: "GetCode",
			inputErr: &Err{
				Message: "test",
				Code:    "success",
				Details: map[string]string{"success": "yes"},
			},
			want: "success",
		},
		{
			name: "GetCode with no code",
			inputErr: &Err{
				Message: "test",
				Details: nil,
			},
			want: "",
		},
		{
			name: "GetCode with empty code",
			inputErr: &Err{
				Message: "test",
				Code:    "",
				Details: map[string]string{},
			},
			want: "",
		},
		{
			name:     "GetCode with not custom error",
			inputErr: errors.New("not custom"),
			want:     "",
		},
	}
	for _, tc := range testCases {
		tc := tc
		t.Run(tc.name, func(t *testing.T) {
			t.Parallel()
			got := GetCode(tc.inputErr)
			assert.Equalf(t, tc.want, got, "codes are not equal: got: %s want: %s", got, tc.want)
		})
	}
}

func TestErrs_HasAnyGivenMessage(t *testing.T) {
	t.Parallel()
	testCases := []struct {
		name               string
		inputErr           error
		inputFirstMessage  string
		inputOtherMessages []string
		want               bool
	}{
		{
			name: "HasAnyGivenMessage returned true with one message",
			inputErr: &Err{
				Message: "true",
			},
			inputFirstMessage: "true",
			want:              true,
		},
		{
			name: "HasAnyGivenMessage returned false with one message",
			inputErr: &Err{
				Message: "true",
			},
			inputFirstMessage: "false",
			want:              false,
		},
		{
			name: "HasAnyGivenMessage returned true with empty error message",
			inputErr: &Err{
				Message: "",
			},
			inputFirstMessage: "",
			want:              true,
		},
		{
			name: "HasAnyGivenMessage returned true on second message",
			inputErr: &Err{
				Message: "abc",
			},
			inputFirstMessage:  "bcd",
			inputOtherMessages: []string{"abc"},
			want:               true,
		},
		{
			name: "HasAnyGivenMessage returned true on third message",
			inputErr: &Err{
				Message: "abc",
			},
			inputFirstMessage:  "bcd",
			inputOtherMessages: []string{"test", "abc"},
			want:               true,
		},
		{
			name: "HasAnyGivenMessage returned false with several messages",
			inputErr: &Err{
				Message: "qwe",
			},
			inputFirstMessage:  "bcd",
			inputOtherMessages: []string{"test", "abc"},
			want:               false,
		},
	}
	for _, tc := range testCases {
		tc := tc
		t.Run(tc.name, func(t *testing.T) {
			t.Parallel()
			got := HasAnyGivenMessage(tc.inputErr, tc.inputFirstMessage, tc.inputOtherMessages...)
			assert.Equalf(t, tc.want, got, "bool values are not equal: got: %v want: %v", got, tc.want)
		})
	}
}

func TestErrs_DeleteFromDetails(t *testing.T) {
	t.Parallel()
	testCases := []struct {
		name        string
		inputErr    error
		inputKey    string
		wantDetails map[string]string
	}{
		{
			name: "DeleteFromDetails",
			inputErr: &Err{
				Message: "1",
				Details: map[string]string{"1": "1", "2": "2"},
			},
			inputKey:    "1",
			wantDetails: map[string]string{"2": "2"},
		},
		{
			name: "DeleteFromDetails with not existing key",
			inputErr: &Err{
				Message: "1",
				Details: map[string]string{"1": "1", "2": "2"},
			},
			inputKey:    "3",
			wantDetails: map[string]string{"1": "1", "2": "2"},
		},
		{
			name: "DeleteFromDetails with empty map",
			inputErr: &Err{
				Message: "1",
				Details: map[string]string{},
			},
			inputKey:    "3",
			wantDetails: map[string]string{},
		},
		{
			name: "DeleteFromDetails with nil map",
			inputErr: &Err{
				Message: "1",
				Details: nil,
			},
			inputKey:    "3",
			wantDetails: nil,
		},
	}
	for _, tc := range testCases {
		tc := tc
		t.Run(tc.name, func(t *testing.T) {
			t.Parallel()
			DeleteFromDetails(tc.inputErr, tc.inputKey)
			assert.Equalf(t, tc.wantDetails, GetDetails(tc.inputErr), "details are not equal: got: %v want: %v", GetDetails(tc.inputErr), tc.wantDetails)
		})
	}
}
