// ShareLinkModal.js

import React, {useState} from 'react';

function ShareLinkModal({closeModal, bookId}) {
  const [days, setDays] = useState('');
  const [link, setLink] = useState('');
  const [isCopied, setIsCopied] = useState(false);

  const handleSubmit = async (e) => {
    const token = localStorage.getItem('token');
    e.preventDefault();

    async function createShareLink(bookId, days) {
      const createLinkResponse = await fetch(`http://3.85.229.215:8002/api/v1/book/share-link/`, {
          headers: {
              'Authorization': `Bearer ${token}`
          },
          method: 'POST',
          body: JSON.stringify({
              book_id: bookId,
              expires_in: days * 24 * 60 * 60,
          }),
      });

      if (!createLinkResponse.ok) {
        console.error('Failed to fetch book details');
        return;
      }

      const data = await createLinkResponse.json();
      setLink(window.location.href + "/share-link/" + data.share_link.id);
    }

    // call your backend function to create a share link
    await createShareLink(bookId, days);
    //closeModal();
  }

  const handleCopyClick = async () => {
    try {
      await navigator.clipboard.writeText(link);
      setIsCopied(true);
    } catch (err) {
      console.error('Failed to copy text: ', err);
    }
  }

  return (
    <div className="modal">
      <div className="modal-content">
        {!link ? (
          <>
            <h2>Create a new share link</h2>
            <form onSubmit={handleSubmit}>
              <div className="modal-form">
                <label htmlFor="expiration-days">Expiration (days):</label>
                <input id="expiration-days" type="number" value={days} onChange={(e) => setDays(e.target.value)}/>
                <button type="submit">Create</button>
              </div>
            </form>
          </>
        ) : (
          <>
            <h2>Share link created</h2>
            <button style={{margin: "20px"}} onClick={handleCopyClick}>
              {isCopied ? 'Copied!' : 'Copy to Clipboard'}
            </button>
          </>
        )}
        <button onClick={closeModal}>Close</button>
      </div>
    </div>
  );
}

export default ShareLinkModal;
