import React, { useEffect, useState } from 'react';
import axios from 'axios';
import './Home.css';
import logo from '../assets/logo.png';
import profileIcon from '../assets/profile-icon.png';
import { Link, useNavigate } from 'react-router-dom';
import news1 from '../assets/news1.jpg';
import news2 from '../assets/news2.jpg';
import news3 from '../assets/news3.jpg';

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
          setNewsList(res.data.data);
        } else {
          setNewsList([]);
        }
      })
      .catch((err) => {
        console.error(err);
        setNewsList([]);
        if (
          err.response &&
          (err.response.status === 401 || err.response.status === 403)
        ) {
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

  // 뉴스 제목에 따라 로컬 이미지 반환
  const getLocalImage = (news) => {
    if (news.title.includes('미래 직업')) return news1;
    if (news.title.includes('탄소 중립')) return news2;
    if (news.title.includes('우주 탐사')) return news3;
    return '/assets/default.jpg'; // 기본 이미지
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
          <Link to="/category/정치">정치</Link>
          <Link to="/category/경제">경제</Link>
          <Link to="/category/사회">사회</Link>
          <Link to="/category/기술">IT</Link>
          <Link to="/category/세계">세계</Link>
        </div>
        <div className="navbar-right">
          <Link to="/analyzer" className="navbar-analyzer">
            🔍 뉴스 분석
          </Link>
          <Link to="/profile" className="navbar-profile-link">
            <img src={profileIcon} alt="프로필" className="navbar-profile" />
          </Link>
          <button className="navbar-logout" onClick={handleLogout} type="button">
            로그아웃
          </button>
        </div>
      </nav>

      <main>
        <div className="welcome-section">
          <h1>🎯 Topicurator에 오신 것을 환영합니다!</h1>
          <p>AI가 분석한 뉴스의 찬반을 확인하고 토론에 참여해보세요.</p>
          <div className="action-buttons">
            <Link to="/analyzer" className="action-btn primary">
              🔍 뉴스 URL 분석하기
            </Link>
            <Link to="/category/사회" className="action-btn secondary">
              📰 최신 뉴스 보기
            </Link>
          </div>
        </div>

        <div className="news-section">
          <h2>🔥 인기 뉴스</h2>
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
                  src={getLocalImage(news)}
                  alt={news.title}
                  className="news-img"
                />
                <div className="news-overlay">
                  <h3>{news.title}</h3>
                  <p>{news.teaserText}</p>
                </div>
              </div>
            ))}
          </div>
        </div>

        <div className="features-section">
          <h2>✨ 주요 기능</h2>
          <div className="features-grid">
            <div className="feature-card">
              <div className="feature-icon">🤖</div>
              <h3>AI 뉴스 분석</h3>
              <p>뉴스 URL을 입력하면 AI가 자동으로 요약하고 찬반을 분석합니다.</p>
              <Link to="/analyzer" className="feature-link">바로가기 →</Link>
            </div>
            <div className="feature-card">
              <div className="feature-icon">🗳️</div>
              <h3>찬반 투표</h3>
              <p>뉴스에 대한 의견을 투표하고 다른 사람들의 생각을 확인하세요.</p>
              <Link to="/category/사회" className="feature-link">바로가기 →</Link>
            </div>
            <div className="feature-card">
              <div className="feature-icon">💬</div>
              <h3>토론 참여</h3>
              <p>댓글로 의견을 나누고 다양한 관점을 탐색해보세요.</p>
              <Link to="/category/정치" className="feature-link">바로가기 →</Link>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}