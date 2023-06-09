import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { ReactReader } from 'react-reader';

export default BookDetail;

function BookDetail() {
  const { id } = useParams();
  const [book, setBook] = useState(null);
  const [url, setUrl] = useState(null);

  useEffect(() => {
    const fetchData = async () => {
      const bookDetailsResponse = await fetch(`http://localhost:8002/api/v1/book/${id}`, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });

      if (!bookDetailsResponse.ok) {
        console.error('Failed to fetch book details');
        return;
      }

      const bookData = await bookDetailsResponse.json();
      setBook(bookData.book);

      const bookUrlResponse = await fetch(`http://localhost:8002/api/v1/book/${id}/url`, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });

      if (!bookUrlResponse.ok) {
        console.error('Failed to fetch book URL');
        return;
      }

      const urlData = await bookUrlResponse.json();
      setUrl(urlData.url);
    };

    fetchData();
  }, [id]);

  if (!book || !url) {
    return <p>Loading...</p>;
  }

  return (
    <div>
      <h1>{book.title}</h1>
      <p>{book.description}</p>
      <p>Author: {book.author}</p>
      <div style={{ height: '600px', position: 'relative' }}>
        <ReactReader
          url={url}
          title={book.title}
          location={"epubcfi(/6/2[cover]!/6)"}
          locationChanged={(epubcifi) => console.log(epubcifi)}
        />
      </div>
    </div>
  );
}
