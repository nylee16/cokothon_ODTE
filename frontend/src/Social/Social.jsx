import React, { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import axios from "axios";
import logo from "../assets/logo.png";
import profileIcon from "../assets/profile-icon.png";
import "./Social.css";

function Social() {
  const [news, setNews] = useState(null);
  const [prosCons, setProsCons] = useState(null);
  const [voteSummary, setVoteSummary] = useState(null);
  const [selected, setSelected] = useState(null);
  const navigate = useNavigate();

  // 예시: 첫 번째 뉴스(사회 분야) 가져오기
  useEffect(() => {
    axios.get('/api/news?category=사회&page=0&size=1&sort=createdAt,desc')
      .then(res => {
        if (res.data && res.data.length > 0) {
          setNews(res.data);
          // proscons 정보 가져오기
          axios.get(`/api/news/${res.data.id}/proscons`)
            .then(pcRes => setProsCons(pcRes.data));
          // 투표 비율 가져오기
          axios.get(`/api/news/${res.data.id}/votes/summary`)
            .then(voteRes => setVoteSummary(voteRes.data));
        }
      });
  }, []);

  const handleLogout = () => {
    localStorage.removeItem("authToken");
    navigate("/");
  };

  const handleDetailClick = () => {
    if (news) navigate(`/society/detail/${news.id}`);
  };

  const handleResultClick = () => {
    navigate("/vote_detail");
  };

  // 찬성/반대/중도 투표 API 연동
  const handleVote = (choice) => {
    if (!news) return;
    axios.put(`/api/news/${news.id}/votes`, { choice })
      .then(() => {
        setSelected(choice);
        // 투표 비율 갱신
        axios.get(`/api/news/${news.id}/votes/summary`)
          .then(res => setVoteSummary(res.data));
      });
  };

  return (
    <div className="social-container">
      {/* 네비게이션 바 */}
      <nav className="navbar-social">
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

      <main className="social-main">
        <h1 className="social-title">사회 분야</h1>
        {news && (
          <div className="social-news-card-detail">
            <span className="social-news-title">{news.title}</span>
            <span className="social-news-underline" onClick={handleDetailClick}>
              {" "}기사 자세히 보기{" "}
            </span>
            <p>{news.description}</p>
            <p>
              기사 찬반율: {voteSummary ? voteSummary.pro : 0}% / 조회수: {news.views}
            </p>
          </div>
        )}

        {prosCons && (
          <div className="social-pros-cons-section">
            <div className="social-side-buttons">
              <button
                className={`social-pros-cons-btn${selected === "pro" ? " selected" : ""}`}
                onClick={() => handleVote("pro")}
              >
                <span className="social-pros-cons-title">찬성</span><br />{prosCons.pros}
              </button>
              <span className="social-pros-cons-vs">VS</span>
              <button
                className={`social-pros-cons-btn${selected === "con" ? " selected" : ""}`}
                onClick={() => handleVote("con")}
              >
                <span className="social-pros-cons-title">반대</span><br />{prosCons.cons}
              </button>
            </div>
            <div className="social-middle-button-container">
              <button
                className={`social-pros-cons-btn${selected === "neutral" ? " selected" : ""}`}
                onClick={() => handleVote("neutral")}
              >
                <span className="social-pros-cons-title">중도</span>
              </button>
            </div>
          </div>
        )}
        <button className="social-result-btn" onClick={handleResultClick}>
          투표 결과 살펴보기
        </button>
      </main>
    </div>
  );
}

export default Social;