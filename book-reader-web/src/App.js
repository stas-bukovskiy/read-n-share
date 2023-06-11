import React from 'react';
import {BrowserRouter as Router, Route, Routes} from 'react-router-dom';
import HomePage from './components/HomePage/HomePage';
import Login from './components/Login/Login';
import NavBar from "./components/NavBar/NavBar";
import UploadBook from "./components/BookUpload/UploadBook";
import BookDetail from './components/BookDetail/BookDetail';
import BookList from "./components/BookList/BookList";
import ShareLinkHandler from "./components/ShareLinkHandler/ShareLinkHandler";

function App() {
  return (
    <Router>
      <NavBar/>
      <Routes>
        <Route path="/" element={<HomePage/>}/>
        <Route path="/login" element={<Login/>}/>
        <Route path="/upload" element={<UploadBook/>}/>
        <Route path="/book/:id" element={<BookDetail/>}/>
        <Route path="/book/:id/share-link/:linkID" element={<ShareLinkHandler/>}/>
        <Route path="/books" element={<BookList/>}/>
      </Routes>
    </Router>
  );
}

export default App;
