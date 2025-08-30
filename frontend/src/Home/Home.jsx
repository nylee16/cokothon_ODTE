import React, { useEffect, useState } from 'react';
import axios from 'axios';
import './Home.css';
import logo from '../assets/logo.png';
import profileIcon from '../assets/profile-icon.png';
import { Link, useNavigate } from 'react-router-dom';

export default function Home() {
  const [newsList, setNewsList] = useState([]);
  const navigate = useNavigate();

  // 뉴스 목록 API 호출
  useEffect(() => {
    axios
      .get('/api/news?page=0&size=10&sort=createdAt,desc', {
        headers: { Authorization: `Bearer ${localStorage.getItem('authToken')}` },
      })
      .then((res) => setNewsList(res.data.content || []))
      .catch(() => setNewsList([]));
  }, []);

  const handleLogout = () => {
    localStorage.removeItem('authToken');
    navigate('/');
  };

  // 카드 클릭 시 상세 페이지 이동
  const handleCardClick = (newsId) => {
    navigate(`/news/${newsId}`);
  };

  return (
    <div className="home-container">
      <nav className="navbar">
        <div className="navbar-left">
          <Link to="/home" className="navbar-logo">
            <img src={logo} alt="logo" />
          </Link>
        </div>
        <div className="navbar-menu">
          <Link to="/politics">정치</Link>
          <Link to="/economy">경제</Link>
          <Link to="/society">사회</Link>
          <Link to="/it">IT</Link>
          <Link to="/world">세계</Link>
        </div>
        <div className="navbar-right">
          <button className="navbar-logout" onClick={handleLogout} type="button">
            로그아웃
          </button>
          <img src={profileIcon} alt="프로필 아이콘" className="navbar-profile" />
        </div>
      </nav>
      <main>
        <div className="news-list">
          {newsList.map((news, idx) => (
            <div
              className="news-card"
              key={news.id}
              onClick={() => handleCardClick(news.id)}
              style={{ cursor: 'pointer' }}
              role="button"
              tabIndex={0}
              onKeyDown={(e) => { if (e.key === 'Enter') handleCardClick(news.id); }}
            >
              {/* image_url 컬럼 값이 있을 때 require로 읽어서 이미지를 로컬에서 출력 */}
              <img
                src={news.image_url ? require(`../assets/${news.image_url}`) : require('../assets/default.jpg')}
                alt={news.title}
                className="news-img"
              />
              <div className="news-overlay">
                <h3>{news.title}</h3>
                <p>{news.teaser_text}</p>
              </div>
            </div>
          ))}
        </div>
      </main>
    </div>
  );
}