import React, {useEffect, useState} from 'react';
import {Button, Modal, Spinner, Table} from 'react-bootstrap';

const AccessRightsComponent = ({wishlistId}) => {
    const [accessRights, setAccessRights] = useState([]);
    const [showModal, setShowModal] = useState(false);
    const [loading, setLoading] = useState(false);
    const [username, setUsername] = useState('');
    const [permission, setPermission] = useState('READ');
    const [selectedUserId, setSelectedUserId] = useState(null);

    const token = localStorage.getItem('token'); // Assuming the token is stored as 'token' in localStorage

    // Fetch access rights for the given wishlistId
    useEffect(() => {
        const fetchAccessRights = async () => {
            setLoading(true);
            try {
                const response = await fetch(`http://localhost:8081/api/v1/wishlists/${wishlistId}/access-rights`, {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                });
                if (response.ok) {
                    const data = await response.json();
                    // Fetch the username for each access right
                    const accessRightsWithUsername = await Promise.all(data.map(async (accessRight) => {
                        const username = await fetchUsername(accessRight.userId);
                        return {
                            ...accessRight,
                            username: username || 'User not found',
                        };
                    }));
                    setAccessRights(accessRightsWithUsername);
                }
            } catch (error) {
                console.error('Error fetching access rights:', error);
            } finally {
                setLoading(false);
            }
        };

        fetchAccessRights().then(r => {
        });
    }, [wishlistId, token]);


    const fetchUsername = async (userId) => {
        try {
            const response = await fetch(`http://localhost:8001/api/v1/users?id=${userId}`, {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });
            if (response.ok) {
                const data = await response.json();
                return data.user.username;
            } else {
                console.error('Error fetching username:', response.status);
            }
        } catch (error) {
            console.error('Error fetching username:', error);
        }
        return '';
    };

    const handleShowModal = () => {
        setUsername('');
        setPermission('READ');
        setSelectedUserId(null);
        setShowModal(true);
    };

    const handleCloseModal = () => {
        setShowModal(false);
    };

    const handleAddAccessRight = async () => {
        setLoading(true);
        try {
            const response = await fetch(`http://localhost:8001/api/v1/users?username=${username}`, {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });
            if (response.ok) {
                const data = await response.json();
                const {id: userId} = data.user;

                // Add the access right using POST request
                const addAccessRightResponse = await fetch(`http://localhost:8081/api/v1/wishlists/${wishlistId}/access-rights`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        Authorization: `Bearer ${token}`,
                    },
                    body: JSON.stringify({userId, permission}),
                });

                if (addAccessRightResponse.ok) {
                    // Refresh the access rights list
                    const updatedAccessRights = await addAccessRightResponse.json();
                    setAccessRights(updatedAccessRights.map(accessRight => {
                        return {
                            ...accessRight,
                            username: username,
                        };
                    }));
                } else {
                    console.error('Error adding access right:', addAccessRightResponse.status);
                }
            } else {
                console.error('User not found');
            }
        } catch (error) {
            console.error('Error adding access right:', error);
        } finally {
            setLoading(false);
            setShowModal(false);
        }
    };

    const handleEditAccessRight = async (accessRight) => {
        setLoading(true);
        try {
            setUsername(accessRight.username);
            setPermission(accessRight.permission);
            setSelectedUserId(accessRight.userId);
            setShowModal(true);
        } catch (error) {
            console.error('Error editing access right:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleDeleteAccessRight = async (accessRight) => {
        setLoading(true);
        try {
            const deleteAccessRightResponse = await fetch(
                `http://localhost:8081/api/v1/wishlists/${wishlistId}/access-rights/${accessRight.userId}`,
                {
                    method: 'DELETE',
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );

            if (deleteAccessRightResponse.ok) {
                const updatedAccessRights = accessRights.filter(
                    (right) => right.userId !== accessRight.userId
                );
                setAccessRights(updatedAccessRights);
            } else {
                console.error('Error deleting access right:', deleteAccessRightResponse.status);
            }
        } catch (error) {
            console.error('Error deleting access right:', error);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div>
            <Button variant="primary" onClick={handleShowModal}>Add new</Button>

            <Table striped bordered hover>
                <thead>
                <tr>
                    <th>Username</th>
                    <th>Permission</th>
                    <th>Actions</th>
                </tr>
                </thead>
                <tbody>
                {accessRights.map((accessRight) => (
                    <tr key={accessRight.userId}>
                        <td>{accessRight.username}</td>
                        <td>{accessRight.permission}</td>
                        <td>
                            <Button variant="primary" onClick={() => handleEditAccessRight(accessRight)}>
                                Edit
                            </Button>{' '}
                            <Button variant="danger"
                                    onClick={() => handleDeleteAccessRight(accessRight)}>Delete</Button>
                        </td>
                    </tr>
                ))}
                </tbody>
            </Table>

            {/* Modal component */}
            <Modal show={showModal} onHide={handleCloseModal}>
                <Modal.Header closeButton>
                    <Modal.Title>{selectedUserId ? 'Edit' : 'Add'} Access Right</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    {/* Username input */}
                    <input
                        type="text"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        disabled={selectedUserId !== null}
                    />

                    {/* Permission select */}
                    <select
                        value={permission}
                        onChange={(e) => setPermission(e.target.value)}
                    >
                        <option value="READ">Read</option>
                        <option value="MODIFY">Modify</option>
                    </select>
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={handleCloseModal}>Cancel</Button>
                    <Button variant="primary" onClick={handleAddAccessRight}>
                        {selectedUserId ? 'Save' : 'Add'}
                    </Button>
                </Modal.Footer>
            </Modal>

            {loading && <Spinner animation="border"/>}
        </div>
    );
};

export default AccessRightsComponent;
