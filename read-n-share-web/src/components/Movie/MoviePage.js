import React, {useEffect, useState} from 'react';
import axios from 'axios';
import {useParams} from "react-router-dom";
import {FaRegStar, FaStar} from 'react-icons/fa';
import ReviewComponent from "../Review/ReviewComponent";

const MoviePage = () => {
    const {imdbId} = useParams();
    const [movie, setMovie] = useState(null);
    const [reviews, setReviews] = useState([]);
    const [loading, setLoading] = useState(true);
    const [reviewForm, setReviewForm] = useState({review: ''});
    const token = localStorage.getItem('token');

    const [userRating, setUserRating] = useState(0);
    const [itemRating, setItemRating] = useState(0);
    const [isLoading, setIsLoading] = useState(true);


    const fetchItemRating = async () => {
        try {
            const response = await axios.get(`http://localhost:8082/api/v1/ratings/${imdbId}`, {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });
            if (response.status !== 404) {
                setItemRating(response.data);
            }
            setIsLoading(false);
        } catch (error) {
            setIsLoading(false);
            console.log('Error fetching rating:', error);
        }
    };

    useEffect(() => {
        const fetchMovie = async () => {
            try {
                const response = await axios.get(
                    `http://localhost:8080/api/v1/movies/${imdbId}`,
                    {
                        headers: {Authorization: `Bearer ${token}`}
                    }
                );
                setMovie(response.data);
                console.log(response.data)
            } catch (error) {
                console.error('Error fetching movie:', error);
            } finally {
                setLoading(false);
            }
        };

        const fetchReviews = async () => {
            try {
                const response = await axios.get(
                    `http://localhost:8082/api/v1/reviews/${imdbId}`,
                    {
                        headers: {Authorization: `Bearer ${token}`}
                    }
                );
                setReviews(response.data);
            } catch (error) {
                console.error('Error fetching reviews:', error);
            }
        };

        const fetchUserRating = async () => {
            try {
                const response = await axios.get(`http://localhost:8082/api/v1/ratings/my/${imdbId}`, {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                });
                if (response.status !== 404) {
                    setUserRating(response.data.rating);
                }
                setIsLoading(false);
            } catch (error) {
                setIsLoading(false);
                console.log('Error fetching rating:', error);
            }
        };

        fetchMovie();
        fetchReviews();
        fetchUserRating();
        fetchItemRating();
    }, [imdbId, token]);

    const handleRatingChange = async (newRating) => {
        try {
            const response = await axios.post(
                'http://localhost:8082/api/v1/ratings',
                {
                    itemId: imdbId,
                    rating: newRating,
                    itemType: "MOVIE"
                },
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );
            if (response.status === 200) {
                setUserRating(newRating);
                await fetchItemRating();
            }

        } catch (error) {
            console.log('Error updating rating:', error);
        }
    };

    const renderStars = () => {
        const stars = [];
        for (let i = 1; i <= 10; i++) {
            stars.push(
                <span key={i} onClick={() => handleRatingChange(i)}>
          {i <= userRating ? <FaStar/> : <FaRegStar/>}
        </span>
            );
        }

        return (
            <>
                {stars}
                {userRating !== 0 && (
                    <button className="btn btn-sm btn-danger" onClick={handleUnrate}>
                        Unrate
                    </button>
                )}
            </>
        );
    };

    if (isLoading) {
        return <p>Loading...</p>;
    }

    const handleUnrate = async () => {
            await axios
                .delete(`http://localhost:8082/api/v1/ratings/${imdbId}`,
                    {
                        headers: {
                            Authorization: `Bearer ${token}`,
                        }
                    })
                .then(() => {
                    setUserRating(0);
                })
                .catch((error) => {
                    console.error(error);
                });
            await fetchItemRating();
        }
    ;


    const handleReviewChange = (event) => {
        setReviewForm((prevForm) => ({
            ...prevForm,
            [event.target.name]: event.target.value
        }));
    };

    const handleReviewSubmit = async (event) => {
        event.preventDefault();

        try {
            const response = await axios.post(
                `http://localhost:8082/api/v1/reviews/${imdbId}`,
                reviewForm,
                {
                    headers: {Authorization: `Bearer ${token}`}
                }
            );
            setReviews((prevReviews) => [...prevReviews, response.data]);
            setReviewForm({review: ''});
        } catch (error) {
            console.error('Error submitting review:', error);
        }
    };

    const handleRemoveReview = async (reviewId) => {
        try {
            await axios.delete(`http://localhost:8082/api/v1/reviews/${reviewId}`, {
                headers: {Authorization: `Bearer ${token}`}
            });
            setReviews((prevReviews) =>
                prevReviews.filter((review) => review.id !== reviewId)
            );
        } catch (error) {
            console.error('Error removing review:', error);
        }
    };


    const renderReviews = () => {
        if (reviews.length === 0) {
            return <p>No reviews available.</p>;
        }

        return (
            <ul className="list-group">
                {reviews.map((review) => (
                    <li key={review.id} className="list-group-item">
                        {review.review}
                        <button
                            type="button"
                            className="btn btn-danger btn-sm ms-2"
                            onClick={() => handleRemoveReview(review.id)}
                        >
                            Remove
                        </button>
                    </li>
                ))}
            </ul>
        );
    };

    const renderUserRatings = () => {
        return (
            <>
                <p>
                    Read-n-share rating: <span className="badge bg-warning text-dark">{itemRating.rating}</span> ●
                    Rating votes: {itemRating.votesCount}
                </p>
            </>
        );
    };

    if (loading) {
        return <p>Loading...</p>;
    }

    if (!movie) {
        return <p>Movie not found.</p>;
    }

    return (
        <div className="container">
            <div className="row">
                <div className="col-3">
                    <img src={movie.imageURL} alt={movie.title} className="img-fluid" width="100%" height="auto"/>
                </div>
                <div className="col">
                    <h1>{movie.title}</h1>
                    {movie.originalTitle && <h2 className="text-muted">movie.originalTitle</h2>}
                    <p>{movie.plot}</p>
                    {renderStars()}
                    <p>
                        IMDb Rating: <span className="badge bg-warning text-dark">{movie.imdbRating}</span> ●
                        Rating votes: {movie.imdbRatingVotes}
                    </p>
                    {renderUserRatings()}
                    {movie.genres && (
                        <>
                            <br/>
                            Genres: {movie.genres.map(genre => (<span className="badge bg-dark">genre</span>))}

                        </>
                    )}
                    {movie.directors && (
                        <>
                            <br/>
                            Directors: {movie.directors.join(' | ')}
                        </>
                    )}
                    {movie.writers && (
                        <>
                            <br/>
                            Directors: {movie.writers.join(' | ')}
                        </>
                    )}
                </div>
            </div>

            <ReviewComponent imdbId={imdbId}></ReviewComponent>


        </div>
    );
};

export default MoviePage;