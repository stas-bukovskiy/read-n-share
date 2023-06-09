import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';

function HomePage() {
  const token = localStorage.getItem('token');
  const [books, setBooks] = useState([]);

  useEffect(() => {
    if (token) {
      fetchBooks().then(r => console.log(r));
    }
  }, [token]);

  const fetchBooks = async () => {
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
  };

  return (
    <div>
      <h1>Welcome to My App</h1>

      {!token && (
        <>
          <p>Welcome, please <Link to="/login">Login</Link></p>
          <nav>
            <ul>
              <li>
                <Link to="/login">Login</Link>
              </li>
              <li>
                <Link to="/register">Register</Link>
              </li>
            </ul>
          </nav>
        </>
      )}

      {token && (
        <nav>
          <ul>
            <li>
              <Link to="/upload">Upload a Book</Link>
            </li>
          </ul>
        </nav>
      )}

      {books.map(book => (
        <div key={book.id}>
          <Link to={`/book/${book.id}`}>{book.title}</Link>
        </div>
      ))}
    </div>
  );
}

export default HomePage;
