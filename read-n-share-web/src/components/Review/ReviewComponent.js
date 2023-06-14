import React, {useEffect, useState} from 'react';
import axios from 'axios';
import {Button, Form} from 'react-bootstrap';

const ReviewComponent = ({imdbId}) => {
    const [reviews, setReviews] = useState([]);
    const [isLoadingReviews, setIsLoadingReviews] = useState(true);
    const [isLoadingSubmit, setIsLoadingSubmit] = useState(false);
    const [isLoadingEdit, setIsLoadingEdit] = useState(false);
    const [reviewText, setReviewText] = useState('');
    const [usernames, setUsernames] = useState({});
    const [currentUserId, setCurrentUserId] = useState(null);

    const [editingReviewId, setEditingReviewId] = useState('');
    const [editingReviewText, setEditingReviewText] = useState('');

    const token = localStorage.getItem("token");

    useEffect(() => {
        const fetchCurrentUserId = async () => {
            const response = await axios.get(`http://3.85.229.215:8001/api/v1/auth/verify?token=${token}`);
            if (response.status.valueOf() === 200) {
                setCurrentUserId(response.data.user.id);
            }
        }

        fetchCurrentUserId();
        fetchReviews();
    }, []);

    const fetchReviews = async () => {
        try {
            setIsLoadingReviews(true);
            const response = await axios.get(`http://3.85.229.215:8082/api/v1/reviews/my/${imdbId}`, {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });
            setReviews(response.data);
            fetchUsernames(response.data);
        } catch (error) {
            console.log('Error fetching reviews:', error);
        } finally {
            setIsLoadingReviews(false);
        }
    };

    const fetchUsernames = async (reviews) => {
        const userIds = reviews.map((review) => review.userId);
        const uniqueUserIds = Array.from(new Set(userIds));
        const usernamePromises = uniqueUserIds.map((userId) => getUsername(userId));
        try {
            const usernameResponses = await Promise.all(usernamePromises);
            const usernameMap = {};
            uniqueUserIds.forEach((userId, index) => {
                usernameMap[userId] = usernameResponses[index];
            });
            setUsernames(usernameMap);
        } catch (error) {
            console.log('Error fetching usernames:', error);
        }
    };

    const getUsername = async (userId) => {
        try {
            const response = await axios.get(`http://3.85.229.215:8001/api/v1/users?id=${userId}`);
            return response.data.user.username;
        } catch (error) {
            console.log('Error fetching username:', error);
            return '';
        }
    };

    const handleEditReview = (id, reviewText) => {
        setEditingReviewId(id);
        setEditingReviewText(reviewText);
    };

    const handleCancelEdit = () => {
        setEditingReviewId('');
        setEditingReviewText('');
    };

    const handleSaveChanges = async () => {
        setIsLoadingEdit(true);
        try {
            const response = await axios.put(
                `http://3.85.229.215:8082/api/v1/reviews/${editingReviewId}`,
                {
                    review: editingReviewText,
                },
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );
            if (response.status === 200) {
                setEditingReviewId('');
                setEditingReviewText('');
                fetchReviews();
            }
        } catch (error) {
            console.log('Error saving changes:', error);
        } finally {
            setIsLoadingEdit(false);
        }
    };

    const handleDeleteReview = async (id) => {
        try {
            const response = await axios.delete(`http://3.85.229.215:8082/api/v1/reviews/${id}`, {
                headers: {
                    Authorization: `Bearer ${token}`
                }
            })
            if (response.status.valueOf() === 200) {
                fetchReviews();
            }
        } catch (error) {
            console.log('Error deleting review:', error);
        }
    };

    const handleReviewSubmit = async (event) => {
        event.preventDefault();
        setIsLoadingSubmit(true);
        try {
            const response = await axios.post(
                'http://3.85.229.215:8082/api/v1/reviews',
                {
                    itemId: imdbId,
                    review: reviewText,
                    itemType: 'MOVIE', // Update with the appropriate itemType value
                },
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );
            if (response.status === 200) {
                setReviewText('');
                fetchReviews();
            }
        } catch (error) {
            console.log('Error submitting review:', error);
        } finally {
            setIsLoadingSubmit(false);
        }
    };

    return (
        <div>
            <h2>Reviews</h2>
            {isLoadingReviews ? (
                <p>Loading reviews...</p>
            ) : reviews.length === 0 ? (
                <p>No reviews found.</p>
            ) : (
                <ul className="list-group">
                    {reviews.map((review) => (
                        <li key={review.id} className="list-group-item">
                            <strong>Username: </strong>
                            {usernames[review.userId]}
                            <br/>
                            {editingReviewId === review.id ? (
                                <div>
                                    <Form.Control
                                        as="textarea"
                                        rows={3}
                                        value={editingReviewText}
                                        onChange={(e) => setEditingReviewText(e.target.value)}
                                    />
                                    <Button variant="primary" disabled={isLoadingEdit} onClick={handleSaveChanges}>
                                        {isLoadingEdit ? 'Saving...' : 'Save Changes'}
                                    </Button>{' '}
                                    <Button variant="secondary" disabled={isLoadingEdit} onClick={handleCancelEdit}>
                                        Cancel
                                    </Button>
                                </div>
                            ) : (
                                <div>
                                    <strong>Review: </strong>
                                    {review.review}
                                    {review.userId === currentUserId && (
                                        <div>
                                            <Button variant="secondary" disabled={isLoadingEdit}
                                                    onClick={() => handleEditReview(review.id, review.review)}>
                                                {isLoadingEdit ? 'Editing...' : 'Edit'}
                                            </Button>{' '}
                                            <Button variant="danger" disabled={isLoadingEdit}
                                                    onClick={() => handleDeleteReview(review.id)}>
                                                Delete
                                            </Button>
                                        </div>
                                    )}
                                </div>
                            )}
                        </li>
                    ))}
                </ul>
            )}

            <h2>Submit Review</h2>
            <Form onSubmit={handleReviewSubmit}>
                <Form.Group controlId="reviewText">
                    <Form.Label>Review</Form.Label>
                    <Form.Control
                        as="textarea"
                        rows={3}
                        value={reviewText}
                        onChange={(e) => setReviewText(e.target.value)}
                    />
                </Form.Group>
                <Button variant="primary" type="submit" disabled={isLoadingSubmit}>
                    {isLoadingSubmit ? 'Submitting...' : 'Submit'}
                </Button>
            </Form>
        </div>
    );
};

export default ReviewComponent;


//
// const [currentUserId, setCurrentUserId] = useState(null);
//
// const token = localStorage.getItem("token");
//
// useEffect(() => {
//     const fetchCurrentUserId = async () => {
//         const response = await axios.get(`http://3.85.229.215:8001/api/v1/auth/verify?token=${token}`);
//         if (response.status.valueOf() === 200) {
//             setCurrentUserId(response.data.user.id);
//         }
//     }
//
//     fetchCurrentUserId();
//     fetchReviews();
// }, []);
