import React, {useState} from 'react';
import {Link} from 'react-router-dom';
import {animated, useSpring} from 'react-spring';
import './WishlistCard.css';

const WishlistCard = ({wishlist}) => {
    const [hovered, setHovered] = useState(false);
    const [clicked, setClicked] = useState(false);

    const hoverAnimation = useSpring({
        transform: hovered ? 'scale(1.05)' : 'scale(1)',
    });

    const clickAnimation = useSpring({
        transform: clicked ? 'scale(0.95)' : 'scale(1)',
    });

    const formatOptions = {
        hour: '2-digit',
        minute: '2-digit',
        day: '2-digit',
        month: '2-digit',
        year: 'numeric'
    };

    const formattedCreatedAt = new Date(wishlist.createdAt).toLocaleString(undefined, formatOptions);
    const formattedUpdatedAt = new Date(wishlist.updatedAt).toLocaleString(undefined, formatOptions);

    return (
        <Link
            to={`/wishlists/${wishlist.id}`}
            className="card wishlist-card my-3"
            onMouseEnter={() => setHovered(true)}
            onMouseLeave={() => setHovered(false)}
            onClick={() => setClicked(!clicked)}
        >
            <div className="card-header">
                {wishlist.title}
            </div>
            <animated.div
                className="card-body"
                style={{...hoverAnimation, ...clickAnimation}}
            >
                <h5 className="card-title">{wishlist.description}</h5>
                <p className="card-text">
                    Item Type: {wishlist.itemType}<br/>
                    Type: {wishlist.wishlistType}<br/>
                    Item Count: {wishlist.itemIds.size}
                </p>
            </animated.div>
            <div className="card-footer text-muted text-center">
                Created At: <strong>{formattedCreatedAt}</strong> | Updated At: <strong>{formattedUpdatedAt}</strong>
            </div>
        </Link>
    );
};

export default WishlistCard;
