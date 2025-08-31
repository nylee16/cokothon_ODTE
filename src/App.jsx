import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Login from './Login/Login';
import Signup from './SignUp/Signup';
import Home from './Home/Home';
import Social from './Social/Social';
import VoteDetail from "./detail/VoteDetail"; 
import NewsAnalyzer from './components/NewsAnalyzer';
import ProsConsDetail from './components/ProsConsDetail';
import UserProfile from './components/UserProfile';
import CategoryNews from './components/CategoryNews';
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
          <Route path="/analyzer" element={<NewsAnalyzer />} />
          <Route path="/proscons/:prosconsId" element={<ProsConsDetail />} />
          <Route path="/profile" element={<UserProfile />} />
          <Route path="/category/:category" element={<CategoryNews />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
