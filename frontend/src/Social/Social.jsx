import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import logo from "../assets/logo.png"; // 실제 경로 맞게 수정
import profileIcon from "../assets/profile-icon.png"; // 실제 경로 맞게 수정
import "./Social.css";

const mockNews = {
  id: 1,
  title: "1. '노란봉투법' 국회 통과…",
  summary:
    "필리버스터가 끝난 후, 노란봉투법이 국회 본회의를 통과했다. 이 법은 하청 노동자들이 원청과 직접 교섭할 수 있도록 허용하고, 파업으로 인한 손해배상 책임을 제한하는 내용을 담고 있다. 국민의힘은 이에 반발하며 퇴장했고, 민주당과 노동계는 역사적인 법안 통과라며 환영했다.",
  pollingPercent: 0,
  impression: 1000,
};

const mockProsCons = {
  pros: "노동자의 정당한 권리 보장, 원청-하청 간 교섭권 강화 주장",
  cons: "법의 지나친 노조 권한 강화로 인한 기업 경영 부담 우려",
};

function Social() {
  const [selected, setSelected] = useState(null);
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.removeItem("authToken");
    navigate("/");
  };

  const handleDetailClick = () => {
    navigate(`/society/detail/${mockNews.id}`);
  };

  const handleResultClick = () => {
    navigate("/vote_detail"); // 원하는 경로로 변경
  };

  return (
    <div className="social-container">
      {/* 네비게이션 바 직접 포함 */}
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
        <div className="social-news-card-detail">
          <span className="social-news-title">{mockNews.title}</span>
          <span className="social-news-underline" onClick={handleDetailClick}>
            {" "}기사 자세히 보기{" "}
          </span>
          <p>{mockNews.summary}</p>
          <p>
            기사 찬반율: {mockNews.pollingPercent}% / 조회수: {mockNews.impression}
          </p>
        </div>
        <div className="social-pros-cons-section">
          <div className="social-side-buttons">
            <button
              className={`social-pros-cons-btn${selected === "찬성" ? " selected" : ""}`}
              onClick={() => setSelected("찬성")}
            >
              <span className="social-pros-cons-title">찬성</span><br />{mockProsCons.pros}
            </button>
            <span className="social-pros-cons-vs">VS</span>
            <button
              className={`social-pros-cons-btn${selected === "반대" ? " selected" : ""}`}
              onClick={() => setSelected("반대")}
            >
              <span className="social-pros-cons-title">반대</span><br />{mockProsCons.cons}
            </button>
          </div>
          <div className="social-middle-button-container">
            <button
              className={`social-pros-cons-btn${selected === "중도" ? " selected" : ""}`}
              onClick={() => setSelected("중도")}
            >
              <span className="social-pros-cons-title">중도</span>
            </button>
          </div>
        </div>
        <button className="social-result-btn" onClick={handleResultClick}>
          투표 결과 살펴보기
        </button>
      </main>
    </div>
  );
}

export default Social;
