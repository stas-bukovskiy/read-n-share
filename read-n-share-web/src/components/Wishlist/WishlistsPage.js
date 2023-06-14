import React, {useEffect, useState} from 'react';
import axios from 'axios';
import {useNavigate} from 'react-router-dom';
import WishlistCard from './WishlistCard';
import 'bootstrap/dist/css/bootstrap.css';
import './WishlistCard.css';
import FloatingActionButton from "../Buttons/FloatingActionButton";
import "../../css/styles.css";
import {Spinner} from "react-bootstrap";

export default function WishlistsPage({wishlistUri}) {
    localStorage.getItem('token');
    const [responseData, setResponseData] = useState(null);
    const navigate = useNavigate();

    const handleCreateWishlist = () => {
        navigate('/create-wishlist');
    };

    const fetchData = async () => {
        try {
            const token = localStorage.getItem('token');
            console.log(wishlistUri);
            const response = await axios.get(`http://3.85.229.215:8081/api/v1/wishlists` + wishlistUri, {
                headers: {
                    Authorization: `Bearer ${token}`, // Include bearer token in the headers
                },
            });
            setResponseData(response.data); // Store response data in state
        } catch (error) {
            console.error(error); // Handle the error
        }
    };

    useEffect(() => {
        fetchData();
    }, [wishlistUri]); // Add wishlistUri as a dependency

    return (
        <>
            <div className="container">
                <div className="wishlist-grid">
                    {responseData ? (
                        responseData.map((wishlist) => <WishlistCard key={wishlist.id} wishlist={wishlist}/>)
                    ) : (
                        <div className="loading-spinner">
                            <Spinner animation="border" variant="primary"/>
                        </div>
                    )}
                </div>
            </div>
            <FloatingActionButton onClick={handleCreateWishlist}/>
        </>
    );
}
