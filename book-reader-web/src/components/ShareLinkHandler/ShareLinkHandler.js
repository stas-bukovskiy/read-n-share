import React, { useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';

function ShareLinkHandler() {
  const { id, linkID } = useParams();
  const navigate = useNavigate();

  useEffect(() => {
    const fetchLink = async () => {
      const response = await fetch(`http://localhost:8002/api/v1/book/share-link/${linkID}/share`, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        },
        method: 'POST',
      });

      if (!response.ok) {
        console.error('Failed to validate share link');
        return;
      }

      const data = await response.json();

      if (response.status === 422) {
        alert('Failed to validate share link: ' + data.message);
      } else {
        navigate(`/book/${id}`);
      }
    };

    fetchLink();
  }, [id, linkID, navigate]);

  return <p>Processing...</p>;
}

export default ShareLinkHandler;
