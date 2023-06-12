import React, {useEffect, useState} from 'react';
import {useParams} from "react-router-dom";
import SearchItemComponent from "./SearchItemComponent";
import {BookSearchResultComponent, MovieSearchResultComponent} from "../ItemSearch/ItemSearchResult";


const EditWishlistPage = () => {
    const {wishlistId} = useParams();
    const [wishlist, setWishlist] = useState(null);
    const [title, setTitle] = useState('');
    const [description, setDescription] = useState('');
    const [items, setItems] = useState([]);
    const [itemType, setItemType] = useState('');
    const [wishlistType, setWishlistType] = useState('');
    const [showModal, setShowModal] = useState(false);

    const [submitting, setSubmitting] = useState(false);
    const [submitError, setSubmitError] = useState(null);
    const [submitSuccess, setSubmitSuccess] = useState(false);

    useEffect(() => {
        const token = localStorage.getItem("token");
        // Fetch wishlist data from the API
        fetch(`http://localhost:8081/api/v1/wishlists/${wishlistId}`, {
            headers: {
                Authorization: 'Bearer ' + token
            }
        })
            .then(response => response.json())
            .then(data => {
                setWishlist(data);
                setTitle(data.title);
                setDescription(data.description);
                setItems(data.itemIds);
                setItemType(data.itemType);
                setWishlistType(data.wishlistType);
            })
            .catch(error => console.log(error));
    }, [wishlistId]);

    const handleTitleChange = e => {
        setTitle(e.target.value);
    };

    const handleDescriptionChange = e => {
        setDescription(e.target.value);
    };

    const addItemToIds = (item) => {
        if (!items.includes(item)) {
            setItems([...items, item]); // Add unique itemId to itemIds
        }
    };

    const handleItemDelete = index => {
        setItems(prevItems => prevItems.filter((_, i) => i !== index));
    };

    const handleWishlistTypeChange = e => {
        setWishlistType(e.target.value);
    };

    const handleSubmit = e => {
        e.preventDefault();

        // Prepare the updated wishlist data
        const updatedWishlist = {
            ...wishlist,
            title,
            description,
            itemIds: items,
            wishlistType
        };

        const token = localStorage.getItem("token");
        // Send the updated wishlist data to the API
        fetch(`http://localhost:8081/api/v1/wishlists/${wishlistId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                Authorization: 'Bearer ' + token
            },
            body: JSON.stringify(updatedWishlist)
        })
            .then(response => {
                if (response.ok) {
                    console.log('Wishlist updated successfully!');
                    setSubmitSuccess(true);
                } else {
                    console.log('Failed to update wishlist.');
                    setSubmitError('Failed to update wishlist.');
                }
                setSubmitting(false);
            })
            .catch(error => {
                console.log(error);
                setSubmitError('An error occurred while updating wishlist.');
                setSubmitting(false);
            });
    };

    if (!wishlist) {
        return <div>Loading wishlist...</div>;
    }

    const handleAddItemButtonClick = (event) => {
        event.preventDefault();
        setShowModal(true);
    };

    return (
        <div className="container">
            <h1>Edit Wishlist</h1>
            <form onSubmit={handleSubmit}>
                <div className="mb-3">
                    <label className="form-label">Title:</label>
                    <input type="text" className="form-control" value={title} onChange={handleTitleChange}/>
                </div>
                <div className="mb-3">
                    <label className="form-label">Description:</label>
                    <textarea className="form-control" value={description} onChange={handleDescriptionChange}/>
                </div>
                <div className="mb-3">
                    <label className="form-label">Items:</label>
                    {items.map((item, index) => (
                        <div key={index} className="d-flex align-items-center mb-2">
                            <input
                                type="text"
                                className="form-control me-2"
                                value={item}
                                disabled
                            />
                            <button type="button" className="btn btn-danger" onClick={() => handleItemDelete(index)}>
                                Delete
                            </button>
                        </div>
                    ))}
                    <button type="button" className="btn btn-primary" onClick={handleAddItemButtonClick}>
                        Add Item
                    </button>
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
                <div className="mb-3">
                    <label className="form-label">Wishlist Type:</label>
                    <select className="form-select" value={wishlistType} onChange={handleWishlistTypeChange}>
                        <option value="PRIVATE">Private</option>
                        <option value="PUBLIC">Public</option>
                    </select>
                </div>
                <button type="submit" className="btn btn-primary" disabled={submitting}>Save Changes</button>
            </form>
            {submitting && <p>Saving changes...</p>}
            {submitSuccess && <p>Wishlist updated successfully!</p>}
            {submitError && <p>Error: {submitError}</p>}
        </div>
    );

};

export default EditWishlistPage;
