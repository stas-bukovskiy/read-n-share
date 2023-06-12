import React from 'react';
import Card from 'react-bootstrap/Card';
import {Link} from 'react-router-dom';
import './BookCard.css'; // Import the CSS for this component

const BookCard = ({book}) => {
    return (
        <Card className="book-card">
            <Link to={`/books/${book.googleBookId}`}>
                <Card.Img variant="top" src={book.imageLinks.smallThumbnail}/>
                <Card.Body>
                    <Card.Title>{book.title}</Card.Title>
                    <Card.Text>{book.authors.join(', ')}</Card.Text>
                </Card.Body>
            </Link>
        </Card>
    );
};

export default BookCard;