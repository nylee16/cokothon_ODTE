import React, { useState, useEffect } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import axios from "axios";
import logo from "../assets/logo.png";
import profileIcon from "../assets/profile-icon.png";
import thumbsUp from "../assets/like_before.png";
import thumbsDown from "../assets/dislike_before.png";
import "./VoteDetail.css";

function VoteDetail() {
  const navigate = useNavigate();
  const { newsId, prosconsId } = useParams(); // 라우터에서 newsId, prosconsId 받아오기
  const token = localStorage.getItem("accessToken");

  const [summary, setSummary] = useState({ pro: 0, con: 0, neutral: 0 });
  const [ageData, setAgeData] = useState([]);
  const [jobData, setJobData] = useState([]);
  const [comments, setComments] = useState([]);
  const [commentText, setCommentText] = useState("");
  const [newsTitle, setNewsTitle] = useState("");

  useEffect(() => {
    if (!token) {
      navigate("/login");
      return;
    }

    window.scrollTo(0, 120);

    // 뉴스 제목 가져오기
    axios.get(`/api/news/${newsId}`, {
      headers: { Authorization: `Bearer ${token}` }
    }).then(res => {
      setNewsTitle(res.data.data.title);
    }).catch(err => console.error(err));

    // 투표 비율
    axios.get(`/api/news/${newsId}/votes/summary`, {
      headers: { Authorization: `Bearer ${token}` }
    }).then(res => setSummary(res.data.data || { pro: 0, con: 0, neutral: 0 }));

    // 나이 투표 분포
    axios.get(`/api/news/${newsId}/votes/breakdown?dimension=age`, {
      headers: { Authorization: `Bearer ${token}` }
    }).then(res => setAgeData(res.data.data || []));

    // 직업 투표 분포
    axios.get(`/api/news/${newsId}/votes/breakdown?dimension=job`, {
      headers: { Authorization: `Bearer ${token}` }
    }).then(res => setJobData(res.data.data || []));

    // 댓글 불러오기
    axios.get(`/api/proscons/${prosconsId}/comments?sort=latest&page=0&size=20`, {
      headers: { Authorization: `Bearer ${token}` }
    }).then(res => setComments(res.data.data.content || []));
  }, [navigate, token, newsId, prosconsId]);

  const handleLogout = () => {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    navigate("/login");
  };

  // 댓글 등록
  const handleCommentSubmit = () => {
    if (commentText.trim() === "") return;

    axios.post(`/api/proscons/${prosconsId}/comments`, {
      content: commentText,
      choice: "neutral" // 선택지 버튼 추가 시 동적으로 변경 가능
    }, {
      headers: { Authorization: `Bearer ${token}` }
    }).then(res => {
      const newComment = res.data.data;
      setComments([newComment, ...comments]);
      setCommentText("");
    }).catch(err => console.error(err));
  };

  // 댓글 공감/비공감
  const handleLike = (commentId) => {
    axios.post(`/api/comments/${commentId}/like`, {}, {
      headers: { Authorization: `Bearer ${token}` }
    }).then(() => refreshComments())
      .catch(err => console.error(err));
  };

  const handleDislike = (commentId) => {
    axios.post(`/api/comments/${commentId}/hate`, {}, {
      headers: { Authorization: `Bearer ${token}` }
    }).then(() => refreshComments())
      .catch(err => console.error(err));
  };

  const refreshComments = () => {
    axios.get(`/api/proscons/${prosconsId}/comments?sort=latest&page=0&size=20`, {
      headers: { Authorization: `Bearer ${token}` }
    }).then(res => setComments(res.data.data.content || []));
  };

  return (
    <div className="vote-detail-unique-container">
      <nav className="vote-navbar-unique">
        <div className="vote-navbar-left">
          <Link to="/home" className="vote-navbar-logo">
            <img src={logo} alt="logo" />
          </Link>
        </div>
        <div className="vote-navbar-menu">
          <Link to="/politics">정치</Link>
          <Link to="/economy">경제</Link>
          <Link to="/society">사회</Link>
          <Link to="/it">IT</Link>
          <Link to="/world">세계</Link>
        </div>
        <div className="vote-navbar-right">
          <button className="vote-navbar-logout" onClick={handleLogout}>로그아웃</button>
          <img src={profileIcon} alt="프로필 아이콘" className="vote-navbar-profile" />
        </div>
      </nav>

      <main className="vote-detail-unique-main">
        <h1 className="vote-detail-unique-title">투표 결과</h1>
        <div className="vote-detail-unique-card">
          <span className="vote-detail-unique-news-title">{newsTitle}</span>
          <div className="vote-detail-unique-summary-cards">
            <div className="vote-detail-unique-summary-box">찬성<br />: {summary.pro || 0}%</div>
            <div className="vote-detail-unique-summary-box">중도<br />: {summary.neutral || 0}%</div>
            <div className="vote-detail-unique-summary-box">반대<br />: {summary.con || 0}%</div>
          </div>
        </div>

        <hr className="vote-detail-unique-divider" />
        <h2 className="vote-detail-unique-age-title">나이대별 결과</h2>
        <div className="vote-detail-unique-age-bars">
          {(ageData.length ? ageData : [
            { label: "20대", percent: 0 },
            { label: "30대", percent: 0 },
            { label: "40대", percent: 0 },
            { label: "50대", percent: 0 },
            { label: "60대", percent: 0 }
          ]).map((age) => (
            <div key={age.label} className="vote-detail-unique-age-bar-row">
              <span className="vote-detail-unique-age-label">{age.label}</span>
              <div className="vote-detail-unique-age-bar-outer">
                <div className="vote-detail-unique-age-bar-inner" style={{ width: `${age.percent}%` }} />
              </div>
              <span className="vote-detail-unique-age-percent">{age.percent}%</span>
            </div>
          ))}
        </div>

        <hr className="vote-detail-unique-divider" />
        <h2 className="vote-detail-unique-job-title">직업별 결과</h2>
        <div className="vote-detail-unique-job-bars">
          {(jobData.length ? jobData : [
            { label: "대학생", percent: 0 },
            { label: "직장인", percent: 0 },
            { label: "전문가", percent: 0 },
            { label: "기타", percent: 0 }
          ]).map((job) => (
            <div key={job.label} className="vote-detail-unique-job-bar-row">
              <span className="vote-detail-unique-job-label">{job.label}</span>
              <div className="vote-detail-unique-job-bar-outer">
                <div className="vote-detail-unique-job-bar-inner" style={{ width: `${job.percent}%` }} />
              </div>
              <span className="vote-detail-unique-job-percent">{job.percent}%</span>
            </div>
          ))}
        </div>

        <hr className="vote-detail-unique-divider" />
        <h2 className="vote-detail-unique-comment-title">의견 공유하기</h2>
        <div className="vote-detail-unique-comment-box">
          <textarea
            className="vote-detail-unique-comment-input"
            placeholder="내용을 입력해 주세요"
            value={commentText}
            onChange={(e) => setCommentText(e.target.value)}
          />
          <button className="vote-detail-unique-comment-submit" onClick={handleCommentSubmit}>
            등록하기
          </button>
        </div>

        <div className="vote-detail-unique-comments">
          {comments.map((c) => (
            <div key={c.id} className="vote-detail-unique-comment-item">
              <div className="vote-detail-unique-comment-header">
                <span className={`vote-detail-unique-comment-username ${c.choice}`}>{c.username || c.user_id}</span>
                <span className="vote-detail-unique-comment-type">{c.choice}</span>
                <span className="vote-detail-unique-comment-date">{c.created_at}</span>
              </div>
              <div className="vote-detail-unique-comment-content">{c.content}</div>
              <div className="vote-detail-unique-comment-actions">
                <img src={thumbsUp} alt="좋아요" className="vote-detail-unique-comment-action-icon"
                  onClick={() => handleLike(c.id)} style={{ cursor: "pointer" }} />
                <span className="vote-detail-unique-comment-action-count">{c.like || 0}</span>
                <img src={thumbsDown} alt="싫어요" className="vote-detail-unique-comment-action-icon"
                  onClick={() => handleDislike(c.id)} style={{ cursor: "pointer" }} />
                <span className="vote-detail-unique-comment-action-count">{c.hate || 0}</span>
              </div>
            </div>
          ))}
        </div>
      </main>
    </div>
  );
}

export default VoteDetail;


