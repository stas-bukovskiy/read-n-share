import React, {useEffect, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import axios from 'axios';
import SearchItemComponent from './SearchItemComponent';
import MovieSearchResultComponent from "./MovieSearchResultComponent";
import BookSearchResultComponent from "./BookSearchResultComponent";

const CreateWishlistForm = () => {
    const navigate = useNavigate();


    const [step, setStep] = useState(1);
    const [title, setTitle] = useState('');
    const [description, setDescription] = useState('');
    const [wishlistType, setWishlistType] = useState('');
    let [itemType, setItemType] = useState('');
    const [itemIds, setItemIds] = useState([]);
    const [showModal, setShowModal] = useState(false);
    const [loading, setLoading] = useState(false);
    const [success, setSuccess] = useState(false);
    const [error, setError] = useState('');


    const handleAddItemButtonClick = (event) => {
        event.preventDefault();
        setShowModal(true);
    };
    // Inside BookSearchResultComponent
    const addItemToIds = (item) => {
        if (!itemIds.includes(item)) {
            setItemIds([...itemIds, item]); // Add unique itemId to itemIds
        }
    };

    function removeItemId(itemIdToRemove) {
        setItemIds((prevItemIds) => prevItemIds.filter((itemId) => itemId !== itemIdToRemove));
    }

    const handleStepOneNext = (event) => {
        event.preventDefault();
        setStep(2);
    };


    useEffect(() => {
        setItemIds([]); // Clear itemIds when itemType changes
    }, [itemType]);


    const handleSubmit = (e) => {
        e.preventDefault();
        // Create the request body
        const wishlistData = {
            title,
            description,
            itemType,
            wishlistType,
            itemIds,
        };

        // Send the POST request
        const token = localStorage.getItem('token');
        // Send the POST request
        axios
            .post('http://localhost:8081/api/v1/wishlists', wishlistData, {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            })
            .then((response) => {
                // Handle successful response here
                console.log(response.data);
                setSuccess(true);
                setLoading(false);
                // Redirect after successful submission
                if (itemType === 'MOVIE') {
                    navigate('/wishlist/movie'); // Redirect to movie wishlist page
                } else if (itemType === 'BOOK') {
                    navigate('/wishlist/book'); // Redirect to book wishlist page
                }
            })
            .catch((error) => {
                // Handle errors here
                console.error(error);
                setError('An error occurred while submitting the form.');
                setLoading(false);
            });
    };

    const isStepOneNextDisabled = title.trim() === '' || wishlistType === '';

    const renderStepOne = () => (
        <div className="container">
            <h2 className="my-4">Step 1: Wishlist Details</h2>
            <form onSubmit={handleSubmit}>
                <div className="mb-3">
                    <label htmlFor="title" className="form-label">
                        Title:
                    </label>
                    <input
                        type="text"
                        className="form-control"
                        id="title"
                        value={title}
                        onChange={(e) => setTitle(e.target.value)}
                        required
                    />
                </div>
                <div className="mb-3">
                    <label htmlFor="description" className="form-label">
                        Description:
                    </label>
                    <textarea
                        className="form-control"
                        id="description"
                        value={description}
                        onChange={(e) => setDescription(e.target.value)}
                    ></textarea>
                </div>
                <div className="mb-3">
                    <label>Wishlist Type:</label>
                    <div className="form-check">
                        <input
                            className="form-check-input"
                            type="radio"
                            value="PUBLIC"
                            checked={wishlistType === 'PUBLIC'}
                            onChange={(e) => setWishlistType(e.target.value)}
                            id="public"
                            required
                        />
                        <label className="form-check-label" htmlFor="public">
                            Public
                        </label>
                    </div>
                    <div className="form-check">
                        <input
                            className="form-check-input"
                            type="radio"
                            value="PRIVATE"
                            checked={wishlistType === 'PRIVATE'}
                            onChange={(e) => setWishlistType(e.target.value)}
                            id="private"
                            required
                        />
                        <label className="form-check-label" htmlFor="private">
                            Private
                        </label>
                    </div>
                </div>
                <button className="btn btn-primary" onClick={handleStepOneNext} disabled={isStepOneNextDisabled}>
                    Next
                </button>
            </form>
        </div>
    );

    const renderStepTwo = () => (
        <div className="container">
            <h2 className="my-4">Step 2: Select Item Type</h2>
            <div className="mb-3">
                <button
                    className="btn btn-primary me-3"
                    onClick={() => {
                        setItemType('MOVIE');
                        setStep(3);
                    }}
                >
                    Movies
                </button>
                <button
                    className="btn btn-primary"
                    onClick={() => {
                        setItemType('BOOK');
                        setStep(3);
                    }}
                >
                    Books
                </button>
            </div>
            <button className="btn btn-secondary" onClick={() => setStep(1)}>
                Previous
            </button>
        </div>

    );


    const renderStepThree = () => (
            <>
                <div>
                    {/* Button to open the modal */}
                    <button className="btn btn-primary" onClick={handleAddItemButtonClick}>
                        Add item
                    </button>

                    {/* Include the SearchItemComponent */}
                    <SearchItemComponent
                        searchUrl={
                            itemType === 'BOOK'
                                ? 'http://localhost:8080/api/v1/books/search'
                                : 'http://localhost:8080/api/v1/movies/search'
                        }
                        SearchResultComponent={
                            itemType === 'BOOK' ? BookSearchResultComponent : MovieSearchResultComponent
                        }
                        handleAddItem={addItemToIds}
                        showModal={showModal}
                        setShowModal={setShowModal}
                    />
                </div>
                <ul className="list-group mt-3">
                    {itemIds.map(itemId => (
                        <li key={itemId} className="list-group-item">
                            {itemId}
                            <button className="btn btn-danger" onClick={() => removeItemId(itemId)}>Remove</button>
                        </li>
                    ))}
                </ul>
                <div className="mt-3">
                    <button className="btn btn-secondary me-3" onClick={() => setStep(2)}>
                        Previous
                    </button>
                    <button className="btn btn-primary" type="submit" disabled={itemIds.length === 0 || loading}>
                        Submit
                    </button>
                    <div>
                        {loading && <p>Loading...</p>}
                        {success && <p>Form submitted successfully!</p>}
                        {error && <p>Error: {error}</p>}
                    </div>
                </div>
            </>
        )
    ;

    return (
        <form onSubmit={handleSubmit}>
            {step === 1 && renderStepOne()}
            {step === 2 && renderStepTwo()}
            {step === 3 && renderStepThree()}
        </form>
    );
};

export default CreateWishlistForm;