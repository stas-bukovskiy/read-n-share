// LinksListModal.js

import React, {useEffect, useState} from 'react';

function LinksListModal({closeModal, bookId}) {
  const [links, setLinks] = useState([]);
  const [error, setError] = useState(null);

  useEffect(() => {
    const token = localStorage.getItem('token');

    const fetchLinks = async () => {

      async function getShareLinks(bookId) {
        const bookDetailsResponse = await fetch(`http://localhost:8002/api/v1/book/share-link/list?book_id=${bookId}`, {
          headers: {
            'Authorization': `Bearer ${token}`
          },
        });

        if (!bookDetailsResponse.ok) {
          console.error('Failed to fetch book details');
          return;
        }

        const data = await bookDetailsResponse.json();

        if (bookDetailsResponse.status === 422) {
          setError(data.message);
          return;
        }
        setLinks(data.share_links);
      }


      // Call your backend function to get all share links for this book
      await getShareLinks(bookId);
    }

    fetchLinks();
  }, [bookId]);

  const handleDeleteLink = async (linkId) => {
    const response = await deleteShareLink(linkId);
    if (!response.ok) {
      console.error('Failed to delete share link');
      return;
    }
    setLinks(links.filter(link => link.id !== linkId));
  }

  async function deleteShareLink(linkId) {
    const token = localStorage.getItem('token');
    return await fetch(`http://localhost:8002/api/v1/book/share-link/${linkId}`, {
      headers: {
        'Authorization': `Bearer ${token}`
      },
      method: 'DELETE',
    });
  }

  const handleCopyClick = (id) => async () => {
    try {
      await navigator.clipboard.writeText(window.location.href + "/share-link/" + id);
    } catch (err) {
      console.error('Failed to copy text: ', err);
    }
  }

  return (
    <div className="modal">
      <div className="modal-content">
        {error ? (<p>{error}</p>
        ) : (
          <>
            <h2>List of share links</h2>
            <table>
              <thead>
              <tr>
                <th>ID</th>
                <th>Link</th>
                <th>Expires</th>
                <th>Action</th>
              </tr>
              </thead>
              <tbody>
              {links.map(link => (
                <tr key={link.id}>
                  <td>{link.id}</td>
                  <td>
                    <button style={{margin: "20px"}} onClick={handleCopyClick(link.id)}>Copy</button>
                  </td>
                  <td>{new Date(link.expires_at) < Date.now() ? "Expired" : new Date(link.expires_at).toLocaleDateString()}</td>
                  <td>
                    <button onClick={() => handleDeleteLink(link.id)}>Delete</button>
                  </td>
                </tr>
              ))}
              </tbody>
            </table>
          </>
        )}
        <button onClick={closeModal}>Close</button>
      </div>
    </div>
  );
}

export default LinksListModal;
