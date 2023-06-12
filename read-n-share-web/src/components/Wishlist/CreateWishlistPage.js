import React, {useEffect, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import axios from 'axios';
import SearchItemComponent from './SearchItemComponent';
import {BookSearchResultComponent, MovieSearchResultComponent} from "../ItemSearch/ItemSearchResult";
import "../Forms/Form.css";
import {BackButton, NextButton} from "../Buttons/Buttons"; // Import your custom styles here
import "./CreateWishlistPage.css";
import FloatingActionButton from "../Buttons/FloatingActionButton";
import {BookAddedItem, MovieAddedItem} from "../ItemSearch/AddedItem";


const StepProgressBar = ({currentStep}) => {
    const progressPercentage = (currentStep / 3) * 100;

    return (
        <div className="progress mb-4">
            <div
                className="progress-bar"
                role="progressbar"
                style={{width: `${progressPercentage}%`}}
                aria-valuenow={progressPercentage}
                aria-valuemin="0"
                aria-valuemax="100"
            ></div>
        </div>
    );
};

const CreateWishlistPage = () => {
    const navigate = useNavigate();


    const [step, setStep] = useState(1);
    const [title, setTitle] = useState('');
    const [description, setDescription] = useState('');
    const [wishlistType, setWishlistType] = useState('');
    let [itemType, setItemType] = useState('');
    const [itemIds, setItemIds] = useState([]);
    const [items, setItems] = useState([])
    const [showModal, setShowModal] = useState(false);
    const [loading, setLoading] = useState(false);
    const [success, setSuccess] = useState(false);
    const [error, setError] = useState('');


    const handleAddItemButtonClick = (event) => {
        event.preventDefault();
        setShowModal(true);
    };

    const addItem = (itemId, item) => {
        if (!itemIds.includes(itemId)) {
            setItemIds([...itemIds, itemId]); // Add unique itemId to itemIds
            setItems([...items, item]);
        }
        console.log(itemIds);
        console.log(items);
    }

    function removeItem(itemIdToRemove) {
        setItemIds((prevItemIds) => prevItemIds.filter((itemId) => {
            console.log("itemId: " + itemId + ": " + itemId !== itemIdToRemove);
            return itemId !== itemIdToRemove;
        }));
        setItems((prevItem) => prevItem.filter((item) => {
            console.log("item: " + item + ": " + item.itemId !== itemIdToRemove)
            return item.itemId !== itemIdToRemove;
        }));
        console.log(itemIdToRemove)
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
        <div className="container-fluid d-flex align-items-center justify-content-center vh-100">
            <div className="form-wrapper">
                <h2 className="my-4">Wishlist Details</h2>
                <StepProgressBar currentStep={step}/>
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
                    <div className="d-flex justify-content-end">
                        <NextButton onClick={handleStepOneNext} disabled={isStepOneNextDisabled}></NextButton>
                    </div>
                </form>
            </div>
        </div>);

    const renderStepTwo = () => (
        <div className="container-fluid d-flex align-items-center justify-content-center vh-100">
            <div className="form-wrapper">
                <h2 className="my-4">Item type</h2>
                <StepProgressBar currentStep={step}/>
                <div className="mb-3">
                    <div className="row">
                        <div className="col d-flex justify-content-center">
                            <button
                                className="big-square-button"
                                style={{backgroundColor: '#FFA500'}}
                                onClick={() => {
                                    setItemType('MOVIE');
                                    setStep(3);
                                }}
                            >
                                Movies
                            </button>
                        </div>
                        <div className="col d-flex justify-content-center">
                            <button
                                className="big-square-button" style={{backgroundColor: '#00BFFF'}}
                                onClick={() => {
                                    setItemType('BOOK');
                                    setStep(3);
                                }}
                            >
                                Books
                            </button>
                        </div>
                    </div>
                </div>
                <div className="d-flex justify-content-start">
                    <BackButton onClick={() => setStep(1)}></BackButton>
                </div>
            </div>
        </div>

    );


    const renderStepThree = () => (
        <div className="container-fluid d-flex align-items-center justify-content-center vh-100">
            <div className="form-wrapper">
                <h2 className="my-4">Add items</h2>
                <StepProgressBar currentStep={step}/>

                {/* Button to open the modal */}
                <FloatingActionButton onClick={handleAddItemButtonClick}/>

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
                    handleAddItem={addItem}
                    showModal={showModal}
                    setShowModal={setShowModal}
                />
                <div>
                    {itemType === "BOOK" ?
                        (<BookAddedItem results={items} handleRemoveItem={removeItem}></BookAddedItem>) :
                        (<MovieAddedItem results={items} handleRemoveItem={removeItem}></MovieAddedItem>)}
                </div>
                <div>
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
            </div>
        </div>
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

export default CreateWishlistPage;