import React, { useState } from 'react';
import axios from 'axios';
import { Link, useNavigate } from 'react-router-dom';
import logo from '../assets/logo.png';
import profileIcon from '../assets/profile-icon.png';
import './NewsAnalyzer.css';

const NewsAnalyzer = () => {
  const [url, setUrl] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');
  const [voteCounts, setVoteCounts] = useState({ pros: 0, neutral: 0, cons: 0 });
  const [userVote, setUserVote] = useState(null); // 사용자의 투표 상태
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    navigate('/login');
  };

  // 투표 처리 함수
  const handleVote = async (voteType) => {
    if (!result || userVote === voteType) return; // 이미 같은 투표를 한 경우

    try {
      const token = localStorage.getItem('accessToken');
      if (!token) {
        setError('로그인이 필요합니다.');
        return;
      }

      // newsId가 없으면 임시 투표 (로컬 상태만 업데이트)
      if (!result.newsId) {
        // 임시 투표 - 로컬 상태만 업데이트
        setVoteCounts(prev => ({
          ...prev,
          [voteType]: prev[voteType] + 1,
          ...(userVote && { [userVote]: prev[userVote] - 1 }) // 이전 투표 취소
        }));
        setUserVote(voteType);
        setError(''); // 성공 시 에러 메시지 제거
        
        // 사용자에게 저장 안내
        setError('임시 투표가 완료되었습니다. 영구 저장을 위해 "분석 결과 저장"을 클릭해주세요.');
        return;
      }

      // newsId가 있으면 실제 투표 API 호출
      const response = await axios.put(`/api/news/${result.newsId}/votes`, {
        choice: voteType
      }, {
        headers: { Authorization: `Bearer ${token}` }
      });

      if (response.data.success) {
        // 투표 수 업데이트
        setVoteCounts(prev => ({
          ...prev,
          [voteType]: prev[voteType] + 1,
          ...(userVote && { [userVote]: prev[userVote] - 1 }) // 이전 투표 취소
        }));
        setUserVote(voteType);
        setError(''); // 성공 시 에러 메시지 제거
      }
    } catch (err) {
      console.error('Vote error:', err);

      // 구체적인 에러 메시지 표시
      if (err.response) {
        if (err.response.status === 404) {
          setError('뉴스 ID를 찾을 수 없습니다. 먼저 분석 결과를 저장해주세요.');
        } else if (err.response.status === 400) {
          setError('잘못된 투표 요청입니다.');
        } else if (err.response.status === 401) {
          setError('로그인이 필요합니다.');
        } else {
          setError(`투표 처리 중 오류가 발생했습니다. (${err.response.status})`);
        }
      } else if (err.request) {
        setError('서버에 연결할 수 없습니다. 네트워크를 확인해주세요.');
      } else {
        setError('투표 처리 중 오류가 발생했습니다.');
      }
    }
  };

  const handleAnalyze = async () => {
    if (!url.trim()) {
      setError('URL을 입력해주세요.');
      return;
    }

    setIsLoading(true);
    setError('');
    setResult(null);
    setVoteCounts({ pros: 0, neutral: 0, cons: 0 });
    setUserVote(null);

    try {
      const token = localStorage.getItem('accessToken');
      if (!token) {
        setError('로그인이 필요합니다.');
        return;
      }

      // URL 정리 및 검증
      let cleanUrl = url.trim();

      // URL이 http:// 또는 https://로 시작하지 않으면 https:// 추가
      if (!cleanUrl.match(/^https?:\/\//)) {
        cleanUrl = 'https://' + cleanUrl;
      }

      // URL 형식 검증
      try {
        new URL(cleanUrl);
      } catch (e) {
        setError('올바른 URL 형식이 아닙니다. http:// 또는 https://로 시작하는 URL을 입력해주세요.');
        setIsLoading(false);
        return;
      }

      const response = await axios.post('/api/llm/summarize', {
        url: cleanUrl,
        language: 'korean'
      }, {
        headers: { Authorization: `Bearer ${token}` }
      });

      if (response.data.success) {
        setResult(response.data.data);
        // 초기 투표 수 설정 (실제로는 백엔드에서 가져와야 함)
        setVoteCounts({ pros: 0, neutral: 0, cons: 0 });
      } else {
        setError('분석 중 오류가 발생했습니다.');
      }
    } catch (err) {
      setError('서버 연결에 실패했습니다.');
      console.error('Analysis error:', err);
    } finally {
      setIsLoading(false);
    }
  };

  const handleSave = async () => {
    if (!result || !url.trim()) return;

    try {
      const token = localStorage.getItem('accessToken');
      if (!token) {
        setError('로그인이 필요합니다.');
        return;
      }

      // URL 정리 및 검증
      let cleanUrl = url.trim();

      // URL이 http:// 또는 https://로 시작하지 않으면 https:// 추가
      if (!cleanUrl.match(/^https?:\/\//)) {
        cleanUrl = 'https://' + cleanUrl;
      }

      // URL 형식 검증
      try {
        new URL(cleanUrl);
      } catch (e) {
        setError('올바른 URL 형식이 아닙니다. http:// 또는 https://로 시작하는 URL을 입력해주세요.');
        return;
      }

      // 저장 API 호출 - 정리된 URL 사용
      const response = await axios.post('/api/proscons/summarize', {
        url: cleanUrl,
        save: true,  // 명시적으로 boolean true 전송
        newsId: null  // 새로 생성할 뉴스이므로 null
      }, {
        headers: { Authorization: `Bearer ${token}` }
      });

      if (response.data.success) {
        // 저장 성공 시 newsId를 받아서 result에 저장
        const savedData = response.data.data;
        if (savedData.id) {
          setResult(prev => ({
            ...prev,
            newsId: savedData.id
          }));
          
          // 임시 투표가 있었다면 실제 투표로 전송
          if (userVote) {
            try {
              const voteResponse = await axios.put(`/api/news/${savedData.id}/votes`, {
                choice: userVote
              }, {
                headers: { Authorization: `Bearer ${token}` }
              });
              
              if (voteResponse.data.success) {
                alert('찬반 분석과 투표가 모두 저장되었습니다!');
              } else {
                alert('찬반 분석은 저장되었지만 투표 저장에 실패했습니다.');
              }
            } catch (voteErr) {
              console.error('Vote save error:', voteErr);
              alert('찬반 분석은 저장되었지만 투표 저장에 실패했습니다.');
            }
          } else {
            alert('찬반 분석이 저장되었습니다!');
          }
          
          setError(''); // 에러 메시지 제거
        } else {
          alert('저장은 성공했지만 뉴스 ID를 받지 못했습니다.');
        }
      } else {
        setError('저장 중 오류가 발생했습니다.');
      }
    } catch (err) {
      console.error('Save error:', err);

      // 구체적인 에러 메시지 표시
      if (err.response) {
        console.log('Error response:', err.response);
        console.log('Error data:', err.response.data);

        if (err.response.status === 400) {
          let errorMessage = '잘못된 요청입니다.';

          // 백엔드에서 전송한 구체적인 에러 메시지 추출
          if (err.response.data?.message) {
            errorMessage = err.response.data.message;
          } else if (err.response.data?.errors) {
            // 검증 오류가 있는 경우
            const validationErrors = err.response.data.errors;
            errorMessage = `검증 오류: ${Object.values(validationErrors).join(', ')}`;
          }

          setError(`요청 오류: ${errorMessage}`);
        } else if (err.response.status === 401) {
          setError('로그인이 필요합니다.');
        } else if (err.response.status === 500) {
          setError('서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
        } else {
          setError(`저장 중 오류가 발생했습니다. (${err.response.status})`);
        }
      } else if (err.request) {
        setError('서버에 연결할 수 없습니다. 네트워크를 확인해주세요.');
      } else {
        setError('저장 중 오류가 발생했습니다.');
      }
    }
  };

  return (
      <div className="news-analyzer-container">
        {/* 네비게이션 바 */}
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
            <Link to="/category/국제">세계</Link>
          </div>
          <div className="navbar-right">
            <Link to="/analyzer" className="navbar-analyzer active">
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

        {/* 메인 컨텐츠 */}
        <div className="news-analyzer">
          <h2>뉴스 분석기</h2>
          <p>뉴스 URL을 입력하면 AI가 자동으로 요약하고 찬반을 분석합니다.</p>

          <div className="url-input-section">
            <input
                type="url"
                value={url}
                onChange={(e) => setUrl(e.target.value)}
                placeholder="뉴스 URL을 입력하세요 (예: https://news.naver.com/...)"
                className="url-input"
            />
            <button
                onClick={handleAnalyze}
                disabled={isLoading}
                className="analyze-btn"
            >
              {isLoading ? '분석 중...' : '분석하기'}
            </button>
          </div>

          {error && <div className="error-message">{error}</div>}

          {result && (
              <div className="analysis-result">
                <h3>분석 결과</h3>

                <div className="summary-section">
                  <h4>📰 요약</h4>
                  <p>{result.summary}</p>
                </div>

                <div className="pros-cons-section">
                  <div className="pros">
                    <h4>👍 찬성</h4>
                    {result.pros ? (
                        <p>{result.pros}</p>
                    ) : (
                        <div className="empty-content">
                          <p className="empty-message">AI가 찬성 근거를 분석하는 중입니다...</p>
                          <p className="empty-hint">잠시 후 다시 시도해보세요</p>
                        </div>
                    )}
                    <div className="vote-section">
                      <button 
                        className={`vote-btn ${userVote === 'pros' ? 'voted' : ''}`}
                        onClick={() => handleVote('pros')}
                        title="투표하기"
                      >
                        👍 찬성
                      </button>
                      <span className="vote-count">{voteCounts.pros}</span>
                      {!result.newsId && (
                        <p className="vote-hint">임시 투표 가능 (저장 후 영구 저장)</p>
                      )}
                    </div>
                  </div>
                  <div className="neutral">
                    <h4>🤔 중립</h4>
                    {result.neutral ? (
                        <p>{result.neutral}</p>
                    ) : (
                        <div className="empty-content">
                          <p className="empty-message">AI가 중립적 사실을 분석하는 중입니다...</p>
                          <p className="empty-hint">잠시 후 다시 시도해보세요</p>
                        </div>
                    )}
                    <div className="vote-section">
                      <button 
                        className={`vote-btn ${userVote === 'neutral' ? 'voted' : ''}`}
                        onClick={() => handleVote('neutral')}
                        title="투표하기"
                      >
                        🤔 중립
                      </button>
                      <span className="vote-count">{voteCounts.neutral}</span>
                      {!result.newsId && (
                        <p className="vote-hint">임시 투표 가능 (저장 후 영구 저장)</p>
                      )}
                    </div>
                  </div>
                  <div className="cons">
                    <h4>👎 반대</h4>
                    {result.cons ? (
                        <p>{result.cons}</p>
                    ) : (
                        <div className="empty-content">
                          <p className="empty-message">AI가 반대 근거를 분석하는 중입니다...</p>
                          <p className="empty-hint">잠시 후 다시 시도해보세요</p>
                        </div>
                    )}
                    <div className="vote-section">
                      <button 
                        className={`vote-btn ${userVote === 'cons' ? 'voted' : ''}`}
                        onClick={() => handleVote('cons')}
                        title="투표하기"
                      >
                        👎 반대
                      </button>
                      <span className="vote-count">{voteCounts.cons}</span>
                      {!result.newsId && (
                        <p className="vote-hint">임시 투표 가능 (저장 후 영구 저장)</p>
                      )}
                    </div>
                  </div>
                </div>

                <div className="bias-section">
                  <h4>🎯 편향도</h4>
                  <div className="bias-bar">
                    <div
                        className="bias-indicator"
                        style={{ left: `${(result.bias + 1) * 50}%` }}
                    ></div>
                  </div>
                  <p className="bias-text">
                    {result.bias > 0.3 ? '찬성 편향' :
                        result.bias < -0.3 ? '반대 편향' : '중립적'}
                  </p>
                </div>

                <div className="action-buttons">
                  <button onClick={handleSave} className="save-btn">
                    💾 분석 결과 저장
                  </button>
                  <button onClick={() => setResult(null)} className="clear-btn">
                    새로 분석하기
                  </button>
                </div>
              </div>
          )}
        </div>
      </div>
  );
};

export default NewsAnalyzer;
