import React, {useEffect, useState} from 'react';
import {Link, Navigate} from 'react-router-dom';
import './BookList.css';

function BookList() {
  const token = localStorage.getItem('token');
  const [books, setBooks] = useState([]);

  useEffect(() => {
    if (token) {
      fetchBooks().then(r => console.log(r));
    }
  }, [token]);

  const fetchBooks = async () => {
    try {
      const response = await fetch('http://localhost:8002/api/v1/book/list', {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      if (response.ok) {
        const data = await response.json();
        setBooks(data.books);
      } else {
        console.error('Failed to fetch books');
      }
    } catch (error) {
      console.error('Failed to fetch books', error);
    }
  };

  if (!token) {
    return <Navigate to={'/login'}/>
  }

  return (
    <div>
      <h1>Your Books</h1>
      <table className="book-table">
        <thead>
        <tr>
          <th>Title</th>
          <th>Author</th>
          <th>Guests</th>
          <th>Progress</th>
          <th>Current Chapter</th>
        </tr>
        </thead>
        <tbody>
        {books.map(userBook => (
          !userBook.isGuest &&
          <tr key={userBook.book.id}>
            <td>
              <Link to={`/book/${userBook.book.id}`}>
                {userBook.book.title.length > 40
                  ? userBook.book.title.substring(0, 37) + "..."
                  : userBook.book.title}
              </Link>
            </td>
            <td>{userBook.book.author}</td>
            <td>{userBook.book.guests_ids ? userBook.book.guests_ids.length : 0}</td>
            <td>{userBook.settings ? userBook.settings.progress : 0} %</td>
            <td>{userBook.settings ? userBook.settings.chapter : 'N/A'}</td>
          </tr>
        ))}
        </tbody>
      </table>

      <h1>Invited Books</h1>
      <table className="book-table">
        <thead>
        <tr>
          <th>Title</th>
          <th>Author</th>
          <th>Progress</th>
          <th>Current Chapter</th>
        </tr>
        </thead>
        <tbody>
        {books.map(userBook => (
          userBook.isGuest &&
          <tr key={userBook.book.id}>
            <td>
              <Link to={`/book/${userBook.book.id}`}>
                {userBook.book.title.length > 40
                  ? userBook.book.title.substring(0, 37) + "..."
                  : userBook.book.title}
              </Link>
            </td>
            <td>{userBook.book.author}</td>
            <td>{userBook.settings ? userBook.settings.progress : 0} %</td>
            <td>{userBook.settings ? userBook.settings.chapter : 'N/A'}</td>
          </tr>
        ))}
        </tbody>
      </table>
    </div>
  );
}

export default BookList;
