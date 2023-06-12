// NavBar.js
import React from 'react';
import {Link, useNavigate} from 'react-router-dom';
import logo from '../../assets/logo.png';
import './NavBar.css';

function NavBar() {
  const navigation = useNavigate();
  const token = localStorage.getItem('token');

  const handleLogout = () => {
    localStorage.removeItem('token');
    navigation('/');
  }
  return (
    <nav className="navbar">
      <img src={logo} alt="Logo" className="navbar-logo" />
        <ul className="navbar-links">
            <li>
                <Link to="/">Home</Link>
            </li>
            <li>
                <Link to="/upload">Upload a Book</Link>
            </li>
            <li>
                <Link to="/reader">Go to Reader</Link>
            </li>
            <li>
                <Link to="/wishlist/movie">Movie wishlists</Link>
            </li>
            <li>
                <Link to="/wishlist/book">Book wishlists</Link>
            </li>
            <li>
                <Link to="/wishlist/shared-with-me">Shared with me wishlists</Link>
            </li>
            {!token ? (
                <>
                    <li>
                        <Link to="/login">Login</Link>
                    </li>
                    <li>
                        <Link to="/register">Register</Link>
                    </li>
                </>
            ) : (
          <li>
            <button className="logout-button" onClick={handleLogout}>Logout</button>
          </li>
        )}
      </ul>
    </nav>
  );
}

export default NavBar;
