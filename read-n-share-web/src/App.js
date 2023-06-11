import React from 'react';
import {BrowserRouter as Router, Route, Routes} from 'react-router-dom';
import HomePage from './components/HomePage/HomePage';
import Login from './components/Login/Login';
import NavBar from "./components/NavBar/NavBar";
import UploadBook from "./components/BookUpload/UploadBook";
import BookDetail from './components/BookDetail/BookDetail';
import WishlistsPage from './components/MovieWishlistPage/WishlistsPage';
import CreateWishlistForm from './components/MovieWishlistPage/CreateWishlistForm';
import WishlistPage from "./components/MovieWishlistPage/WishlistPage";
import EditWishlist from "./components/MovieWishlistPage/EditWishlist";


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
                <Route path="/create-wishlist" element={<CreateWishlistForm/>}/>
                <Route path="/wishlists/:wishlistId" element={<WishlistPage/>}></Route>
                <Route path="/wishlists/edit/:wishlistId" element={<EditWishlist/>}></Route>
            </Routes>
        </Router>
    );
}

export default App;
