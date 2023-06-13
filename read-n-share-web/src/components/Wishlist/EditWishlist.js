import React, {useEffect, useState} from 'react';
import {useParams} from "react-router-dom";
import SearchItemComponent from "./SearchItemComponent";
import {BookSearchResultComponent, MovieSearchResultComponent} from "../ItemSearch/ItemSearchResult";
import {BookAddedItem, MovieAddedItem} from "../ItemSearch/AddedItem";
import {Spinner} from "react-bootstrap";
import FloatingActionButton from "../Buttons/FloatingActionButton";


const EditWishlistPage = () => {
    const {wishlistId} = useParams();
    const [wishlist, setWishlist] = useState(null);
    const [title, setTitle] = useState('');
    const [description, setDescription] = useState('');
    const [itemIds, setItemIds] = useState([]);
    const [items, setItems] = useState([]);
    const [itemType, setItemType] = useState('');
    const [wishlistType, setWishlistType] = useState('');
    const [showModal, setShowModal] = useState(false);

    const [submitting, setSubmitting] = useState(false);
    const [submitError, setSubmitError] = useState(null);
    const [submitSuccess, setSubmitSuccess] = useState(false);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
            const token = localStorage.getItem("token");
            // Fetch wishlist data from the API
            fetch(`http://3.85.229.215:8081/api/v1/wishlists/${wishlistId}`, {
                headers: {
                    Authorization: 'Bearer ' + token
                }
            })
                .then(response => response.json())
                .then(data => {
                    setWishlist(data);
                    setTitle(data.title);
                    setDescription(data.description);
                    setItemIds(data.itemIds);
                    setItemType(data.itemType);
                    setWishlistType(data.wishlistType);
                    return data;
                })
                .then(r => fetchData())
                .catch(error => console.log(error));

            const fetchData = async () => {
                    try {
                        const responses = await Promise.all(
                            itemIds.map(async itemId => {
                                const response = await fetch(`http://3.85.229.215:8080/api/v1/${itemType === 'MOVIE' ? 'movies' : 'books'}/${itemId}`,
                                    {
                                        headers: {
                                            Authorization: 'Bearer ' + token
                                        }
                                    });
                                return await response.json();
                            })
                        );

                        setItems(responses);
                    } catch
                        (error) {
                        console.error('Error fetching item data:', error);
                    }
                }
            ;

        }, []
    )
    ;

    const handleTitleChange = e => {
        setTitle(e.target.value);
    };

    const handleDescriptionChange = e => {
        setDescription(e.target.value);
    };

    const addItem = (itemId, item) => {
        if (!itemIds.includes(itemId)) {
            setItemIds([...itemIds, itemId]); // Add unique itemId to itemIds
            setItems([...items, item]);
        }
        console.log(itemIds);
        console.log(items);
    }

    const removeItem = (itemIdToRemove) => {
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
        fetch(`http://3.85.229.215:8081/api/v1/wishlists/${wishlistId}`, {
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
            <div className="card mt-5">
                <div className="card-header text-center">
                    <h3>Edit Wishlist</h3>
                </div>
                <div className="card-body">
                    <form onSubmit={handleSubmit}>
                        <div className="mb-3">
                            <label className="form-label">Title:</label>
                            <input type="text" className="form-control" value={title} onChange={handleTitleChange}/>
                        </div>
                        <div className="mb-3">
                            <label className="form-label">Description:</label>
                            <textarea className="form-control" value={description}
                                      onChange={handleDescriptionChange}/>
                        </div>
                        <hr/>
                        <div className="mb-3">
                            <label className="form-label">Items:</label>
                            <div>
                                {isLoading && (
                                    < div className="loading-spinner">
                                        <Spinner animation="border" variant="primary"/>
                                    </div>
                                )}
                                {itemType === "BOOK" ?
                                    (<BookAddedItem results={items}
                                                    handleRemoveItem={removeItem}></BookAddedItem>) :
                                    (<MovieAddedItem results={items}
                                                     handleRemoveItem={removeItem}></MovieAddedItem>)}
                            </div>
                            <FloatingActionButton onClick={handleAddItemButtonClick}></FloatingActionButton>
                            <SearchItemComponent
                                searchUrl={
                                    itemType === 'BOOK'
                                        ? 'http://3.85.229.215:8080/api/v1/books/search'
                                        : 'http://3.85.229.215:8080/api/v1/movies/search'
                                }
                                SearchResultComponent={
                                    itemType === 'BOOK' ? BookSearchResultComponent : MovieSearchResultComponent
                                }
                                handleAddItem={addItem}
                                showModal={showModal}
                                setShowModal={setShowModal}
                            />
                        </div>
                        <div className="mb-3">
                            <label className="form-label">Wishlist Type:</label>
                            <select className="form-select" value={wishlistType}
                                    onChange={handleWishlistTypeChange}>
                                <option value="PRIVATE">Private</option>
                                <option value="PUBLIC">Public</option>
                            </select>
                        </div>
                        <div className="d-flex justify-content-center">
                            <button type="submit" className="btn btn-success btn-lg" disabled={submitting}>Save
                                Changes
                            </button>
                        </div>
                    </form>
                    {submitting && <p>Saving changes...</p>}
                    {submitSuccess && <p>Wishlist updated successfully!</p>}
                    {submitError && <p>Error: {submitError}</p>}
                </div>
            </div>
        </div>
    );

};

export default EditWishlistPage;
