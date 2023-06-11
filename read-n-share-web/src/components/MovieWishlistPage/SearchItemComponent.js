import {useState} from "react";
import axios from 'axios';
import {Button, Modal} from 'react-bootstrap';

const SearchItemComponent = ({searchUrl, SearchResultComponent, handleAddItem, showModal, setShowModal}) => {
    const [expression, setExpression] = useState('');
    const [searchResults, setSearchResults] = useState([]);
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const handleCloseModal = () => {
        setShowModal(false);
    };

    const handleSearch = (event) => {
        event.preventDefault();
        if (expression.trim() === '') {
            setError('Please enter a search expression');
            setSearchResults([]);
            return;
        }

        setLoading(true);

        const token = localStorage.getItem('token');
        axios
            .get(`${searchUrl}/${expression}`, {
                headers: {
                    Authorization: `Bearer ${token}`
                },
            })
            .then((response) => {
                setSearchResults(response.data.results);
                setError('');
            })
            .catch((error) => {
                console.error(error);
                setError('An error occurred during the search.');
                setSearchResults([]);
            })
            .finally(() => {
                setLoading(false);
            });
    };

    return (

        <Modal show={showModal} onHide={handleCloseModal} className=" modal-lg">
            <Modal.Header closeButton>
                <Modal.Title>Search Item</Modal.Title>
            </Modal.Header>
            <Modal.Body>
                <div>
                    <input
                        type="text"
                        value={expression}
                        onChange={(e) => setExpression(e.target.value)}
                    />
                    <button className="btn btn-primary" onClick={handleSearch}>
                        Search
                    </button>
                </div>
                {loading && <p>Loading...</p>}
                {error && <p>{error}</p>}
                {searchResults.length > 0 && (
                    <div>
                        <h3>Search Results:</h3>
                        <SearchResultComponent results={searchResults} handleAddItem={handleAddItem}/>
                    </div>
                )}
            </Modal.Body>
            <Modal.Footer>
                <Button variant="secondary" onClick={handleCloseModal}>
                    Close
                </Button>
            </Modal.Footer>
        </Modal>
    );
};

export default SearchItemComponent;