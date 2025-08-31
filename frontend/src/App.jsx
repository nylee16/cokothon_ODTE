import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Login from './Login/Login';
import Signup from './SignUp/Signup';
import Home from './Home/Home';
import Social from './Social/Social';
import VoteDetail from "./detail/VoteDetail"; 
import ScrollToTop from "./ScrollToTop";
import './App.css';


function App() {
  return (
    <Router>
      <div className="app">
        <ScrollToTop />
        <Routes>
          <Route path="/" element={<Login />} />
          <Route path="/signup" element={<Signup />} />
          <Route path="/login" element={<Login />} />
          <Route path="/home" element={<Home />} />
          <Route path="/society" element={<Social />} />
          <Route path="/vote_detail" element={<VoteDetail />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
