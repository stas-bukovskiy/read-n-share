// HomePage.js
import React from 'react';
import { Link } from 'react-router-dom';

function HomePage() {
  return (
    <div>
      <h1>Welcome to My App</h1>
      <nav>
        <ul>
          <li>
            <Link to="/login">Login</Link>
          </li>
          <li>
            <Link to="/register">Register</Link>
          </li>
          <li>
            <Link to="/upload">Upload a Book</Link>
          </li>
          <li>
            <Link to="/reader">Go to Reader</Link>
          </li>
        </ul>
      </nav>
      {/* You might fetch and display the user's books here */}
    </div>
  );
}

export default HomePage;
