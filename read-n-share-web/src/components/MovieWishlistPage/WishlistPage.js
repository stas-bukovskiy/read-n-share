import React, {useEffect, useState} from 'react';
import {Link, useNavigate, useParams} from "react-router-dom";
import AccessRightsComponent from "./AccessRightComponent";

const MovieInfo = ({itemId}) => {
    const [movie, setMovie] = useState(null);

    useEffect(() => {
        const token = localStorage.getItem("token");

        // Fetch movie data from the API
        fetch(`http://localhost:8080/api/v1/movies/${itemId}`, {
            headers: {
                Authorization: `Bearer ${token}`, // Include bearer token in the headers
            },
        })
            .then(response => response.json())
            .then(data => setMovie(data))
            .catch(error => console.log(error));
    }, [itemId]);

    if (!movie) {
        return <div>Loading movie...</div>;
    }

    return (
        <div>
            <Link to={`/movies/${itemId}`} className="text-decoration-none">
                <h2>{movie.title}</h2>
            </Link>
            <p>{movie.plot}</p>
            {/* Render other movie information */}
        </div>
    );
};

const BookInfo = ({itemId}) => {
    const [book, setBook] = useState(null);

    useEffect(() => {
        const token = localStorage.getItem("token");

        // Fetch book data from the API
        fetch(`http://localhost:8080/api/v1/books/${itemId}`, {
            headers: {
                Authorization: `Bearer ${token}`, // Include bearer token in the headers
            },
        })
            .then(response => response.json())
            .then(data => setBook(data))
            .catch(error => console.log(error));
    }, [itemId]);

    if (!book) {
        return <div>Loading book...</div>;
    }

    return (
        <div>
            <h2>{book.title}</h2>
            <p>{book.description}</p>
            {/* Render other book information */}
        </div>
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
        fetch(`http://localhost:8081/api/v1/wishlists/${wishlistId}`, {
            headers: {
                Authorization: `Bearer ${token}`, // Include bearer token in the headers
            },
        })
            .then(response => response.json())
            .then(data => setWishlist(data))
            .catch(error => console.log(error));
    }, [wishlistId]);

    if (!wishlist) {
        return <div>Loading wishlist...</div>;
    }

    const {permission, itemIds = [], itemType} = wishlist;

    const handleEdit = () => {
        navigate(`/wishlists/edit/${wishlistId}`);
    };

    const handleDelete = () => {
        // Handle delete functionality here
    };

    return (
        <div className="container">
            <h1 className="my-4">{wishlist.title}</h1>
            <p className="mb-3">{wishlist.description}</p>

            {itemIds.map((itemId) => {
                if (itemType === 'MOVIE') {
                    return <MovieInfo itemId={itemId} key={itemId}/>;
                } else if (itemType === 'BOOK') {
                    return <BookInfo itemId={itemId} key={itemId}/>;
                }
                return null; // Handle other item types if needed
            })}

            {permission !== 'READ' && (
                <button className="btn btn-primary" onClick={handleEdit}>
                    Edit
                </button>
            )}

            {permission === 'OWNER' && (
                <button className="btn btn-danger" onClick={handleDelete}>
                    Delete
                </button>
            )}

            {/* Additional fields can be displayed here */}
            {wishlist.permission === 'OWNER' && (
                <> <br/>
                    <div>
                        <AccessRightsComponent wishlistId={wishlist.id}/>
                    </div>
                </>
            )}
        </div>
    );
};

export default WishlistPage;