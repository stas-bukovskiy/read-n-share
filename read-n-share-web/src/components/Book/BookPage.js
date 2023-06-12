import React, {useEffect, useState} from 'react';
import Spinner from 'react-bootstrap/Spinner';
import Container from 'react-bootstrap/Container';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import ListGroup from 'react-bootstrap/ListGroup';
import {useParams} from "react-router-dom";
import RatingsComponent from "../UserRating/UserRatingComponent";

const BookPage = () => {
    const {googleBooksId} = useParams()
    const token = localStorage.getItem('token');

    const [book, setBook] = useState(null);
    const [rating, setRating] = useState(null);
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetch(`http://localhost:8080/api/v1/books/${googleBooksId}`, {
            headers: {
                Authorization: `Bearer ${token}`
            }
        })
            .then(response => response.json())
            .then(data => {
                setBook(data);
                fetch(`http://localhost:8082/api/v1/ratings/${data.id}`, {
                    headers: {
                        Authorization: `Bearer ${token}`
                    }
                })
                    .then(response => response.json())
                    .then(ratingData => {
                        setRating(ratingData);
                        fetch(`http://localhost:8001/api/v1/users?id=${ratingData.itemId}`, {
                            headers: {
                                Authorization: `Bearer ${token}`
                            }
                        })
                            .then(response => response.json())
                            .then(userData => {
                                setUser(userData.user);
                                setLoading(false);
                            });
                    });
            });
    }, [googleBooksId, token]);


    if (loading) {
        return <Spinner animation="border"/>;
    }

    return (
        <Container>
            <Row>
                <Col md={4}>
                    <img src={book.imageLinks.smallThumbnail} alt={book.title} style={{width: '100%'}}/>
                </Col>
                <Col md={8}>
                    <h2>{book.title}</h2>
                    <h4>{book.subtitle}</h4>
                    <ListGroup variant="flush">
                        <ListGroup.Item><strong>Authors:</strong> {book.authors.join(', ')}</ListGroup.Item>
                        <ListGroup.Item><strong>Published Date:</strong> {book.publishedDate}</ListGroup.Item>
                        <ListGroup.Item><strong>Categories:</strong> {book.categories.join(', ')}</ListGroup.Item>
                        <ListGroup.Item><strong>Description:</strong> {book.description}</ListGroup.Item>
                        <ListGroup.Item><strong>Page Count:</strong> {book.pageCount}</ListGroup.Item>
                        <ListGroup.Item><strong>Language:</strong> {book.language}</ListGroup.Item>
                        <RatingsComponent itemId={googleBooksId} itemType="BOOK"></RatingsComponent>
                    </ListGroup>
                </Col>
            </Row>
        </Container>
    );
};

export default BookPage;