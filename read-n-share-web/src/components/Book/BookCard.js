import React from 'react';
import {Link} from 'react-router-dom';
import "../../css/styles.css"

const BookCard = ({book}) => {
    return (
        <div key={book.googleBookId} className="col-md-4 mb-4  animated-card">
            <Link to={`/books/${book.googleBookId}`} className="text-decoration-none">
                <div className="card">
                    <img src={book.imageLinks.thumbnail || book.smallThumbnail} alt={book.title}
                         className="card-img-top"/>
                    <div className="card-body">
                        <h4 className="card-title">{book.title}</h4>
                        <p className="card-text">{book.subtitle}</p>
                    </div>
                </div>
            </Link>
        </div>

    )
};

export default BookCard;