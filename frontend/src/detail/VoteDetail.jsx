import React, { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import logo from "../assets/logo.png";
import profileIcon from "../assets/profile-icon.png";
import thumbsUp from "../assets/like_before.png";
import thumbsDown from "../assets/dislike_before.png";
import "./VoteDetail.css";

function VoteDetail() {
  const navigate = useNavigate();

  // 페이지 렌더 시 스크롤 맨 위로 이동
  useEffect(() => {
  window.scrollTo(0, 120); // 네비 높이만큼 스크롤 아래로 이동
}, []);

  const handleLogout = () => {
    localStorage.removeItem("authToken");
    navigate("/");
  };

  const jobData = [
    { label: "대학생", percent: 25 },
    { label: "직장인", percent: 25 },
    { label: "전문가", percent: 25 },
    { label: "기타", percent: 25 },
  ];

  const [comments, setComments] = useState([
    {
      username: "사용자1",
      type: "찬성",
      date: "2025.08.30",
      text: "찬성합니다. (내용)",
      likes: 10,
      dislikes: 5,
      likedByUser: false,
      dislikedByUser: false,
    },
    {
      username: "사용자2",
      type: "반대",
      date: "2025.08.30",
      text: "반대합니다. (내용)",
      likes: 10,
      dislikes: 5,
      likedByUser: false,
      dislikedByUser: false,
    },
  ]);

  const [commentText, setCommentText] = useState("");

  const handleCommentSubmit = () => {
    if (commentText.trim() === "") return;
    setComments([
      ...comments,
      {
        username: `사용자${comments.length + 1}`,
        type: "중도",
        date: "2025.08.31",
        text: commentText,
        likes: 0,
        dislikes: 0,
        likedByUser: false,
        dislikedByUser: false,
      },
    ]);
    setCommentText("");
  };

  const handleLike = (idx) => {
    setComments((comments) => {
      const newComments = [...comments];
      const comment = newComments[idx];
      if (!comment.likedByUser) {
        comment.likes += 1;
        comment.likedByUser = true;
        if (comment.dislikedByUser) {
          comment.dislikes -= 1;
          comment.dislikedByUser = false;
        }
      }
      return newComments;
    });
  };

  const handleDislike = (idx) => {
    setComments((comments) => {
      const newComments = [...comments];
      const comment = newComments[idx];
      if (!comment.dislikedByUser) {
        comment.dislikes += 1;
        comment.dislikedByUser = true;
        if (comment.likedByUser) {
          comment.likes -= 1;
          comment.likedByUser = false;
        }
      }
      return newComments;
    });
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
          <button
            className="vote-navbar-logout"
            onClick={handleLogout}
            type="button"
          >
            로그아웃
          </button>
          <img
            src={profileIcon}
            alt="프로필 아이콘"
            className="vote-navbar-profile"
          />
        </div>
      </nav>

      <main className="vote-detail-unique-main">
        <h1 className="vote-detail-unique-title">투표 결과</h1>
        <div className="vote-detail-unique-card">
          <span className="vote-detail-unique-news-title">
            1. '노란봉투법' 국회 통과...
          </span>
          <div className="vote-detail-unique-summary-cards">
            <div className="vote-detail-unique-summary-box">
              찬성
              <br />
              : 30%
            </div>
            <div className="vote-detail-unique-summary-box">
              중도
              <br />
              : 40%
            </div>
            <div className="vote-detail-unique-summary-box">
              반대
              <br />
              : 30%
            </div>
          </div>
        </div>
        <hr className="vote-detail-unique-divider" />
        <h2 className="vote-detail-unique-age-title">나이대별 결과</h2>
        <div className="vote-detail-unique-age-bars">
          {["20대", "30대", "40대", "50대", "60대"].map((age) => (
            <div key={age} className="vote-detail-unique-age-bar-row">
              <span className="vote-detail-unique-age-label">{age}</span>
              <div className="vote-detail-unique-age-bar-outer">
                <div
                  className="vote-detail-unique-age-bar-inner"
                  style={{ width: "20%" }}
                />
              </div>
              <span className="vote-detail-unique-age-percent">20%</span>
            </div>
          ))}
        </div>
        <hr className="vote-detail-unique-divider" />
        <h2 className="vote-detail-unique-job-title">직업별 결과</h2>
        <div className="vote-detail-unique-job-bars">
          {jobData.map((job) => (
            <div key={job.label} className="vote-detail-unique-job-bar-row">
              <span className="vote-detail-unique-job-label">{job.label}</span>
              <div className="vote-detail-unique-job-bar-outer">
                <div
                  className="vote-detail-unique-job-bar-inner"
                  style={{ width: `${job.percent}%` }}
                />
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
          <button
            className="vote-detail-unique-comment-submit"
            onClick={handleCommentSubmit}
          >
            등록하기
          </button>
        </div>
        <div className="vote-detail-unique-comments">
          {comments.map((c, i) => (
            <div key={i} className="vote-detail-unique-comment-item">
              <div className="vote-detail-unique-comment-header">
                <span className={`vote-detail-unique-comment-username ${c.type}`}>
                  {c.username}
                </span>
                <span className="vote-detail-unique-comment-type">{c.type}</span>
                <span className="vote-detail-unique-comment-date">{c.date}</span>
              </div>
              <div className="vote-detail-unique-comment-content">{c.text}</div>
              <div className="vote-detail-unique-comment-actions">
                <img
                  src={thumbsUp}
                  alt="좋아요"
                  className="vote-detail-unique-comment-action-icon"
                  onClick={() => handleLike(i)}
                  style={{
                    cursor: c.likedByUser ? "default" : "pointer",
                    opacity: c.likedByUser ? 0.5 : 1,
                  }}
                />
                <span className="vote-detail-unique-comment-action-count">{c.likes}</span>
                <img
                  src={thumbsDown}
                  alt="싫어요"
                  className="vote-detail-unique-comment-action-icon"
                  onClick={() => handleDislike(i)}
                  style={{
                    cursor: c.dislikedByUser ? "default" : "pointer",
                    opacity: c.dislikedByUser ? 0.5 : 1,
                  }}
                />
                <span className="vote-detail-unique-comment-action-count">{c.dislikes}</span>
              </div>
            </div>
          ))}
        </div>
      </main>
    </div>
  );
}

export default VoteDetail;

