import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import './ProsConsDetail.css';

const ProsConsDetail = () => {
  const { prosconsId } = useParams();
  const navigate = useNavigate();
  const [proscons, setProscons] = useState(null);
  const [comments, setComments] = useState([]);
  const [voteSummary, setVoteSummary] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [newComment, setNewComment] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    fetchProsConsDetail();
    fetchComments();
    fetchVoteSummary();
  }, [prosconsId]);

  const fetchProsConsDetail = async () => {
    try {
      const response = await axios.get(`/api/proscons/${prosconsId}`);
      if (response.data.success) {
        setProscons(response.data.data);
      }
    } catch (err) {
      setError('찬반 정보를 불러오는데 실패했습니다.');
    }
  };

  const fetchComments = async () => {
    try {
      const response = await axios.get(`/api/proscons/${prosconsId}/comments?sort=latest&page=0&size=20`);
      setComments(response.data.content || []);
    } catch (err) {
      console.error('댓글 로딩 실패:', err);
    }
  };

  const fetchVoteSummary = async () => {
    try {
      const response = await axios.get(`/api/news/${proscons?.newsId}/votes/summary`);
      if (response.data.success) {
        setVoteSummary(response.data.data);
      }
    } catch (err) {
      console.error('투표 통계 로딩 실패:', err);
    } finally {
      setIsLoading(false);
    }
  };

  const handleVote = async (voteType) => {
    try {
      const token = localStorage.getItem('token');
      if (!token) {
        alert('로그인이 필요합니다.');
        return;
      }

      const response = await axios.put(`/api/news/${proscons.newsId}/votes`, {
        voteType: voteType
      }, {
        headers: { Authorization: `Bearer ${token}` }
      });

      if (response.data.success) {
        alert('투표가 완료되었습니다!');
        fetchVoteSummary();
      }
    } catch (err) {
      alert('투표 처리 중 오류가 발생했습니다.');
    }
  };

  const handleCommentSubmit = async (e) => {
    e.preventDefault();
    if (!newComment.trim()) return;

    try {
      const token = localStorage.getItem('token');
      if (!token) {
        alert('로그인이 필요합니다.');
        return;
      }

      setIsSubmitting(true);
      const response = await axios.post(`/api/proscons/${prosconsId}/comments`, {
        content: newComment.trim()
      }, {
        headers: { Authorization: `Bearer ${token}` }
      });

      if (response.data) {
        setNewComment('');
        fetchComments();
        alert('댓글이 등록되었습니다!');
      }
    } catch (err) {
      alert('댓글 등록 중 오류가 발생했습니다.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleCommentLike = async (commentId, action) => {
    try {
      const endpoint = action === 'like' ? 'like' : 'hate';
      await axios.post(`/api/comments/${commentId}/${endpoint}`);
      fetchComments();
    } catch (err) {
      console.error('댓글 반응 처리 실패:', err);
    }
  };

  if (isLoading) {
    return <div className="loading">로딩 중...</div>;
  }

  if (error || !proscons) {
    return <div className="error">찬반 정보를 불러올 수 없습니다.</div>;
  }

  return (
    <div className="proscons-detail">
      <div className="header">
        <button onClick={() => navigate(-1)} className="back-btn">
          ← 뒤로가기
        </button>
        <h1>찬반 토론</h1>
      </div>

      <div className="content-section">
        <div className="news-info">
          <h2>{proscons.title}</h2>
          <p className="summary">{proscons.summary}</p>
          <div className="meta">
            <span>생성일: {new Date(proscons.createdAt).toLocaleDateString()}</span>
            <span>편향도: {proscons.bias > 0.3 ? '찬성 편향' : 
                           proscons.bias < -0.3 ? '반대 편향' : '중립적'}</span>
          </div>
        </div>

        <div className="pros-cons-cards">
          <div className="pros-card">
            <h3>👍 찬성</h3>
            <p>{proscons.pros}</p>
          </div>
          <div className="neutral-card">
            <h3>🤔 중립</h3>
            <p>{proscons.neutral}</p>
          </div>
          <div className="cons-card">
            <h3>👎 반대</h3>
            <p>{proscons.cons}</p>
          </div>
        </div>

        {voteSummary && (
          <div className="vote-section">
            <h3>투표 현황</h3>
            <div className="vote-stats">
              <div className="vote-item">
                <span className="vote-label">찬성</span>
                <div className="vote-bar">
                  <div 
                    className="vote-fill pros-fill" 
                    style={{ width: `${(voteSummary.prosCount / voteSummary.totalCount) * 100}%` }}
                  ></div>
                </div>
                <span className="vote-count">{voteSummary.prosCount}</span>
              </div>
              <div className="vote-item">
                <span className="vote-label">반대</span>
                <div className="vote-bar">
                  <div 
                    className="vote-fill cons-fill" 
                    style={{ width: `${(voteSummary.consCount / voteSummary.totalCount) * 100}%` }}
                  ></div>
                </div>
                <span className="vote-count">{voteSummary.consCount}</span>
              </div>
            </div>
            <div className="vote-actions">
              <button onClick={() => handleVote('PROS')} className="vote-btn pros-btn">
                👍 찬성하기
              </button>
              <button onClick={() => handleVote('CONS')} className="vote-btn cons-btn">
                👎 반대하기
              </button>
            </div>
          </div>
        )}
      </div>

      <div className="comments-section">
        <h3>댓글 ({comments.length})</h3>
        
        <form onSubmit={handleCommentSubmit} className="comment-form">
          <textarea
            value={newComment}
            onChange={(e) => setNewComment(e.target.value)}
            placeholder="의견을 남겨보세요..."
            className="comment-input"
            rows="3"
          />
          <button 
            type="submit" 
            disabled={isSubmitting || !newComment.trim()}
            className="comment-submit-btn"
          >
            {isSubmitting ? '등록 중...' : '댓글 등록'}
          </button>
        </form>

        <div className="comments-list">
          {comments.map((comment) => (
            <div key={comment.id} className="comment-item">
              <div className="comment-header">
                <span className="comment-author">{comment.authorName}</span>
                <span className="comment-date">
                  {new Date(comment.createdAt).toLocaleDateString()}
                </span>
              </div>
              <p className="comment-content">{comment.content}</p>
              <div className="comment-actions">
                <button 
                  onClick={() => handleCommentLike(comment.id, 'like')}
                  className="action-btn like-btn"
                >
                  👍 {comment.likeCount || 0}
                </button>
                <button 
                  onClick={() => handleCommentLike(comment.id, 'hate')}
                  className="action-btn hate-btn"
                >
                  👎 {comment.hateCount || 0}
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default ProsConsDetail;
