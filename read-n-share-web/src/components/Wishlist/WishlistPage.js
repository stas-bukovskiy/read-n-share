import React, {useEffect, useState} from 'react';
import {useNavigate, useParams} from "react-router-dom";
import AccessRightsComponent from "./AccessRightComponent";
import {Spinner} from "react-bootstrap";
import "../../css/styles.css";
import {MovieCard} from "../Movie/MovieCard";
import BookCard from "../Book/BookCard";
import FloatingCopyButton from "../Buttons/FloatingCopyButton";


const MovieInfo = ({itemId}) => {
    const [movie, setMovie] = useState(null);

    useEffect(() => {
        const token = localStorage.getItem("token");

        // Fetch movie data from the API
        fetch(`http://3.85.229.215:8080/api/v1/movies/${itemId}`, {
            headers: {
                Authorization: `Bearer ${token}`, // Include bearer token in the headers
            },
        })
            .then(response => response.json())
            .then(data => setMovie(data))
            .catch(error => console.log(error));
    }, [itemId]);

    if (!movie) {
        return (
            <div className="loading-spinner">
                <Spinner animation="border" variant="primary"/>
            </div>
        );
    }

    return (
        <MovieCard movieData={movie}></MovieCard>
    );
};

const BookInfo = ({itemId}) => {
    const [book, setBook] = useState(null);

    useEffect(() => {
        const token = localStorage.getItem("token");

        // Fetch book data from the API
        fetch(`http://3.85.229.215:8080/api/v1/books/${itemId}`, {
            headers: {
                Authorization: `Bearer ${token}`, // Include bearer token in the headers
            },
        })
            .then(response => response.json())
            .then(data => setBook(data))
            .catch(error => console.log(error));
    }, [itemId]);

    if (!book) {
        return (
            <div className="loading-spinner">
                <Spinner animation="border" variant="primary"/>
            </div>
        );
    }

    return (
        <BookCard book={book}></BookCard>
    );
};

const WishlistPage = () => {
    const {wishlistId} = useParams();
    const [wishlist, setWishlist] = useState(null);
    const navigate = useNavigate();


    console.log(wishlistId)
    useEffect(() => {
        const token = localStorage.getItem("token");
        // Fetch wishlist data from the API
        fetch(`http://3.85.229.215:8081/api/v1/wishlists/${wishlistId}`, {
            headers: {
                Authorization: `Bearer ${token}`, // Include bearer token in the headers
            },
        })
            .then(response => response.json())
            .then(data => setWishlist(data))
            .catch(error => console.log(error));
    }, [wishlistId]);

    if (!wishlist) {
        return (
            <div className="loading-spinner">
                <Spinner animation="border" variant="primary"/>
            </div>
        );
    }

    const {permission, itemIds = [], itemType} = wishlist;

    const handleEdit = () => {
        navigate(`/wishlists/edit/${wishlistId}`);
    };

    const handleDelete = () => {
        // Handle delete functionality here
    };

    return (
        <>
            <div className="container">
                <div className="card my-5">
                    <div className="card-header text-center">
                        <h2 className="my-1">{wishlist.title}</h2>
                    </div>
                    <div className="card-body">
                        <h3 className="mb-3 text-muted">{wishlist.description}</h3>
                        <hr/>

                        <div className="row">
                            {itemIds.map((itemId) => {
                                if (itemType === 'MOVIE') {
                                    return <MovieInfo itemId={itemId} key={itemId}/>;
                                } else if (itemType === 'BOOK') {
                                    return <BookInfo itemId={itemId} key={itemId}/>;
                                }
                                return null; // Handle other item types if needed
                            })}
                        </div>

                        <div className="d-flex justify-content-center my-3">
                            {permission !== 'READ' && (
                                <button className="btn btn-primary btn-lg me-3" onClick={handleEdit}>
                                    Edit
                                </button>
                            )}

                            {permission === 'OWNER' && (
                                <button className="btn btn-danger btn-lg" onClick={handleDelete}>
                                    Delete
                                </button>
                            )}
                        </div>
                    </div>

                    {wishlist.permission === 'OWNER' && (
                        <div className="card-footer">
                            <AccessRightsComponent wishlistId={wishlist.id}/>
                        </div>
                    )}
                </div>
            </div>
            <FloatingCopyButton dataToCopy={`http://localhost:3000/wishlists/${wishlistId}`}></FloatingCopyButton>
        </>
    );
};

export default WishlistPage;