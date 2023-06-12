import React from 'react';
import {BrowserRouter as Router, Route, Routes} from 'react-router-dom';
import HomePage from './components/HomePage/HomePage';
import Login from './components/Login/Login';
import NavBar from "./components/NavBar/NavBar";
import UploadBook from "./components/BookUpload/UploadBook";
import BookDetail from './components/BookDetail/BookDetail';
import WishlistsPage from './components/Wishlist/WishlistsPage';
import CreateWishlistPage from './components/Wishlist/CreateWishlistPage';
import WishlistPage from "./components/Wishlist/WishlistPage";
import EditWishlist from "./components/Wishlist/EditWishlist";
import MoviePage from "./components/Movie/MoviePage";
import BookPage from "./components/Book/BookPage";


function App() {
    return (
        <Router>
            <NavBar/>
            <Routes>
                <Route path="/" element={<HomePage/>}/>
                <Route path="/login" element={<Login/>}/>
                <Route path="/upload" element={<UploadBook/>}/>
                <Route path="/book/:id" element={<BookDetail/>}/>
                <Route path="/wishlist/movie" element={<WishlistsPage wishlistUri="?itemType=MOVIE"/>}/>
                <Route path="/wishlist/book" element={<WishlistsPage wishlistUri="?itemType=BOOK"/>}/>
                <Route path="/wishlist/shared-with-me" element={<WishlistsPage wishlistUri="/shared-with-me"/>}/>
                <Route path="/create-wishlist" element={<CreateWishlistPage/>}/>
                <Route path="/wishlists/:wishlistId" element={<WishlistPage/>}></Route>
                <Route path="/wishlists/edit/:wishlistId" element={<EditWishlist/>}></Route>

                <Route path="/movies/:imdbId" element={<MoviePage/>}></Route>
                <Route path="/books/:googleBooksId" element={<BookPage/>}></Route>
            </Routes>
        </Router>
    );
}

export default App;
