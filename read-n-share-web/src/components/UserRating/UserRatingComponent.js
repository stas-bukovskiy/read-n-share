import axios from "axios";
import {FaRegStar, FaStar} from "react-icons/fa";
import React, {useEffect, useState} from "react";
import ListGroup from "react-bootstrap/ListGroup";

const RatingsComponent = ({itemId, itemType}) => {

    const [userRating, setUserRating] = useState(0);
    const [itemRating, setItemRating] = useState(0);
    const [isLoading, setIsLoading] = useState(true);

    const token = localStorage.getItem('token');

    const fetchItemRating = async () => {
        try {
            const response = await axios.get(`http://3.85.229.215:8082/api/v1/ratings/${itemId}`, {
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
        const fetchUserRating = async () => {
            try {
                const response = await axios.get(`http://3.85.229.215:8082/api/v1/ratings/my/${itemId}`, {
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

        fetchUserRating().then(r => {
        });
        fetchItemRating().then(r => {
        });
    }, [])


    const handleRatingChange = async (newRating) => {
        try {
            const response = await axios.post(
                'http://3.85.229.215:8082/api/v1/ratings',
                {
                    itemId: itemId,
                    rating: newRating,
                    "itemType": itemType
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

    const handleUnrate = async () => {
            await axios
                .delete(`http://3.85.229.215:8082/api/v1/ratings/${itemId}`,
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

    return (
        <>
            <ListGroup.Item><strong>Read-n-Share Rating: </strong> <span
                className="badge bg-warning text-dark">{itemRating.rating}</span> ● <strong>Ratings
                Count: </strong>{itemRating.votesCount}</ListGroup.Item>
            <ListGroup.Item>{renderStars()}</ListGroup.Item>
        </>
    )
}

export default RatingsComponent;