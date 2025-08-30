import React from 'react';
import './Home.css';
import logo from '../assets/logo.png';
import img1 from '../assets/news1.jpg';
import img2 from '../assets/news2.jpg';
import img3 from '../assets/news3.jpg';
import profileIcon from '../assets/profile-icon.png'; 
import { Link, useNavigate } from 'react-router-dom';

const newsList = [
  {
    img: img1,
    title: '사망사고 건설사 명단공개 재추진',
    subtitle: '“안전 관리 유도” vs “실효성 낮아”',
  },
  {
    img: img2,
    title: '노란봉투법 국회 통과··',
    subtitle: '“역사적 순간” vs “불법파업 조장”',
  },
  {
    img: img3,
    title: 'AI 교과서 도입 찬반 논란',
    subtitle: '“맞춤형 학습 지원” vs “디지털 과의존”',
  },
];

export default function Home() {
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.removeItem('authToken');
    navigate('/');
  };

  // 카드 클릭시 Social 페이지로 이동
  const handleCardClick = () => {
    navigate('/society'); // Social 컴포넌트 경로에 맞게 조정
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
              key={idx}
              onClick={handleCardClick}
              style={{ cursor: 'pointer' }}
              role="button"
              tabIndex={0} // 키보드 접근성 추가
              onKeyDown={(e) => { if (e.key === 'Enter') handleCardClick(); }}
            >
              <img src={news.img} alt={news.title} className="news-img" />
              <div className="news-overlay">
                <h3>{news.title}</h3>
                <p>{news.subtitle}</p>
              </div>
            </div>
          ))}
        </div>
      </main>
    </div>
  );
}