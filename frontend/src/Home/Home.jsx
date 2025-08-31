import React, { useEffect, useState } from 'react';
import axios from 'axios';
import './Home.css';
import logo from '../assets/logo.png';
import profileIcon from '../assets/profile-icon.png';
import { Link, useNavigate } from 'react-router-dom';

export default function Home() {
  const [newsList, setNewsList] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    const accessToken = localStorage.getItem('accessToken');
    if (!accessToken) {
      navigate('/login');
      return;
    }

    axios
      .get('/api/news/top?limit=3&period=24h', {
        headers: { Authorization: `Bearer ${accessToken}` },
      })
      .then((res) => {
        if (res.data.success) {
          setNewsList(res.data.data); // data 배열 사용
        } else {
          setNewsList([]);
        }
      })
      .catch((err) => {
        console.error(err);
        setNewsList([]);
        if (err.response && (err.response.status === 401 || err.response.status === 403)) {
          handleLogout();
        }
      });
  }, [navigate]);

  const handleLogout = () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    navigate('/login');
  };

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
          {newsList.map((news) => (
            <div
              className="news-card"
              key={news.id}
              onClick={() => handleCardClick(news.id)}
              style={{ cursor: 'pointer' }}
              role="button"
              tabIndex={0}
              onKeyDown={(e) => {
                if (e.key === 'Enter') handleCardClick(news.id);
              }}
            >
              <img
                src={news.imageUrl || '/assets/default.jpg'} // imageUrl 사용
                alt={news.title}
                className="news-img"
              />
              <div className="news-overlay">
                <h3>{news.title}</h3>
                <p>{news.teaserText}</p> {/* teaserText 사용 */}
              </div>
            </div>
          ))}
        </div>
      </main>
    </div>
  );
}
