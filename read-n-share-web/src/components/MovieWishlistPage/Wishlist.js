import React from 'react';
import {Link} from "react-router-dom";

const WishlistComponent = ({wishlist}) => {
    return (
        <div className="container">
            <Link to={`/wishlists/${wishlist.id}`} className="text-decoration-none">
                <h2 className="my-4">{wishlist.title}</h2>
            </Link>
            <p className="mb-3">{wishlist.description}</p>
            <p className="mb-2"><strong>Item IDs:</strong> {wishlist.itemIds.join(', ')}</p>
            <p className="mb-2"><strong>Item Type:</strong> {wishlist.itemType}</p>
            <p className="mb-2"><strong>Wishlist Type:</strong> {wishlist.wishlistType}</p>
            {/* Additional fields can be displayed here */}
        </div>
    );
};

export default WishlistComponent;
