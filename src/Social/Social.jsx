import React, { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import axios from "axios";
import logo from "../assets/logo.png";
import profileIcon from "../assets/profile-icon.png";
import "./Social.css";

function Social() {
  const [news, setNews] = useState(null); // 단일 뉴스
  const [voteSummary, setVoteSummary] = useState({ proPercentage: 0, conPercentage: 0, neutralPercentage: 0 });
  const [selected, setSelected] = useState(null);
  const navigate = useNavigate();
  const token = localStorage.getItem("accessToken");

  useEffect(() => {
    if (!token) {
      navigate("/login");
      return;
    }

    const fetchLatestNews = async () => {
      try {
        const newsRes = await axios.get(
          "/api/news?category=사회&page=0&size=1&sort=createdAt,desc",
          { headers: { Authorization: `Bearer ${token}` } }
        );

        const latestNews = newsRes.data.data.content[0];
        if (!latestNews) return;

        // DTO에 prosconsId만 있으므로 바로 상태에 저장
        setNews(latestNews);

        // prosconsId가 있으면 투표 요약 호출
        if (latestNews.prosconsId) {
          const voteRes = await axios.get(
            `/api/proscons/${latestNews.prosconsId}/votes/summary`,
            { headers: { Authorization: `Bearer ${token}` } }
          );
          setVoteSummary(voteRes.data.data || { proPercentage: 0, conPercentage: 0, neutralPercentage: 0 });
        }
      } catch (err) {
        console.error("뉴스 가져오기 실패:", err);
        if (err.response && (err.response.status === 401 || err.response.status === 403)) {
          handleLogout();
        }
      }
    };

    fetchLatestNews();
  }, [navigate, token]);

  const handleLogout = () => {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    navigate("/login");
  };

  const handleDetailClick = (newsId) => navigate(`/society/detail/${newsId}`);
  const handleResultClick = () => navigate(`/vote_detail`);

  const handleVote = (choice) => {
    const prosconsId = news?.prosconsId;
    if (!prosconsId) return console.error("prosCons ID가 존재하지 않습니다.");

    axios
      .put(
        `/api/proscons/${prosconsId}/votes`,
        { choice },
        { headers: { Authorization: `Bearer ${token}` } }
      )
      .then(() => {
        setSelected(choice);
        return axios.get(`/api/proscons/${prosconsId}/votes/summary`, { headers: { Authorization: `Bearer ${token}` } });
      })
      .then((res) => setVoteSummary(res.data.data))
      .catch((err) => console.error("투표 요청 또는 요약 가져오기 실패:", err));
  };

  if (!news) return <div>로딩 중...</div>;

  return (
    <div className="social-container">
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
          <button className="navbar-logout" onClick={handleLogout}>로그아웃</button>
          <img src={profileIcon} alt="프로필 아이콘" className="navbar-profile" />
        </div>
      </nav>

      <main className="social-main">
        <h1 className="social-title">사회 분야 최신 뉴스</h1>

        <div className="social-news-card-detail">
          <h2 className="social-news-title">{news.title}</h2>
          <span className="social-news-underline" onClick={() => handleDetailClick(news.id)}>기사 자세히 보기</span>
          <p>{news.description}</p>
          <p>
            기사 찬반율: 찬성 {voteSummary?.proPercentage || 0}% / 반대 {voteSummary?.conPercentage || 0}% / 중립 {voteSummary?.neutralPercentage || 0}%
            <br />
            조회수: {news.views}
          </p>

          <div className="social-pros-cons-section">
            <div className="social-side-buttons">
              <button
                className={`social-pros-cons-btn${selected === "pro" ? " selected" : ""}`}
                onClick={() => handleVote("pro")}
              >
                <span className="social-pros-cons-title">찬성</span>
              </button>
              <span className="social-pros-cons-vs">VS</span>
              <button
                className={`social-pros-cons-btn${selected === "con" ? " selected" : ""}`}
                onClick={() => handleVote("con")}
              >
                <span className="social-pros-cons-title">반대</span>
              </button>
            </div>
            <div className="social-middle-button-container">
              <button
                className={`social-pros-cons-btn${selected === "neutral" ? " selected" : ""}`}
                onClick={() => handleVote("neutral")}
              >
                <span className="social-pros-cons-title">중립</span>
              </button>
            </div>
          </div>

          <button className="social-result-btn" onClick={handleResultClick}>투표 결과 살펴보기</button>
        </div>
      </main>
    </div>
  );
}

export default Social;
