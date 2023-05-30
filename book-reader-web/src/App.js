import React from 'react';
import {BrowserRouter as Router, Route, Routes} from 'react-router-dom';
import HomePage from './components/HomePage/HomePage';
import Login from './components/Login/Login';
import NavBar from "./components/NavBar/NavBar";
import UploadBook from "./components/BookUpload/UploadBook";

function App() {
  return (
    <Router>
      <NavBar/>
      <Routes>
        <Route path="/" element={<HomePage/>}/>
        <Route path="/login" element={<Login/>}/>
        <Route path="/upload" element={<UploadBook/>}/>
        {/* You will add more routes here for the other pages */}
      </Routes>
    </Router>
  );
}

export default App;
