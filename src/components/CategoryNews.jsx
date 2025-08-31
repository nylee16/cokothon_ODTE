import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import './CategoryNews.css';

const CategoryNews = () => {
  const { category } = useParams();
  const navigate = useNavigate();
  const [news, setNews] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [sortBy, setSortBy] = useState('createdAt');
  const [sortDirection, setSortDirection] = useState('desc');

  const pageSize = 10;

  useEffect(() => {
    // 로그인 체크
    const token = localStorage.getItem('accessToken');
    if (!token) {
      navigate('/login');
      return;
    }

    fetchCategoryNews();
  }, [category, currentPage, sortBy, sortDirection, navigate]);

  const fetchCategoryNews = async () => {
    try {
      setIsLoading(true);
      const token = localStorage.getItem('accessToken');
      if (!token) {
        navigate('/login');
        return;
      }

      const response = await axios.get(`/api/news`, {
        params: {
          category: category,
          page: currentPage,
          size: pageSize,
          sort: `${sortBy},${sortDirection}`
        },
        headers: { Authorization: `Bearer ${token}` }
      });

      if (response.data.success) {
        const newsData = response.data.data;
        setNews(newsData.content);
        setTotalPages(newsData.totalPages);
        setTotalElements(newsData.totalElements);
      }
    } catch (err) {
      if (err.response && (err.response.status === 401 || err.response.status === 403)) {
        navigate('/login');
        return;
      }
      setError('뉴스를 불러오는데 실패했습니다.');
      console.error('Category news fetch error:', err);
    } finally {
      setIsLoading(false);
    }
  };

  const handlePageChange = (page) => {
    setCurrentPage(page);
    window.scrollTo(0, 0);
  };

  const handleSortChange = (newSortBy) => {
    if (sortBy === newSortBy) {
      setSortDirection(sortDirection === 'asc' ? 'desc' : 'asc');
    } else {
      setSortBy(newSortBy);
      setSortDirection('desc');
    }
    setCurrentPage(0);
  };

  const handleNewsClick = (newsId) => {
    navigate(`/news/${newsId}`);
  };

  const handleProsConsClick = (prosconsId) => {
    navigate(`/proscons/${prosconsId}`);
  };

  const getCategoryDisplayName = (cat) => {
    const categoryNames = {
      '정치': '정치',
      '경제': '경제',
      '사회': '사회',
      '국제': '국제',
      '문화': '문화',
      '스포츠': '스포츠',
      '기술': '기술',
      '연예': '연예'
    };
    return categoryNames[cat] || cat;
  };

  const getCategoryColor = (cat) => {
    const categoryColors = {
      '정치': '#dc3545',
      '경제': '#28a745',
      '사회': '#007bff',
      '국제': '#6f42c1',
      '문화': '#fd7e14',
      '스포츠': '#20c997',
      '기술': '#17a2b8',
      '연예': '#e83e8c'
    };
    return categoryColors[cat] || '#6c757d';
  };

  if (isLoading) {
    return <div className="loading">로딩 중...</div>;
  }

  if (error) {
    return <div className="error">{error}</div>;
  }

  return (
      <div className="category-news">
        <div className="category-header">
          <button onClick={() => navigate(-1)} className="back-btn">
            ← 뒤로가기
          </button>
          <div className="category-info">
            <h1>{getCategoryDisplayName(category)} 뉴스</h1>
            <p>총 {totalElements}개의 뉴스가 있습니다.</p>
          </div>
        </div>

        <div className="sort-controls">
          <div className="sort-buttons">
            <button
                onClick={() => handleSortChange('createdAt')}
                className={`sort-btn ${sortBy === 'createdAt' ? 'active' : ''}`}
            >
              최신순 {sortBy === 'createdAt' && (sortDirection === 'asc' ? '↑' : '↓')}
            </button>
            <button
                onClick={() => handleSortChange('views')}
                className={`sort-btn ${sortBy === 'views' ? 'active' : ''}`}
            >
              조회순 {sortBy === 'views' && (sortDirection === 'asc' ? '↑' : '↓')}
            </button>
          </div>
        </div>

        <div className="news-grid">
          {news.map((item) => (
              <div key={item.id} className="news-card">
                <div className="news-image">
                  <img
                      src={item.imageUrl || '/news1.jpg'}
                      alt={item.title}
                      onError={(e) => {
                        e.target.src = '/news1.jpg';
                      }}
                  />
                  <div className="category-badge" style={{ backgroundColor: getCategoryColor(category) }}>
                    {getCategoryDisplayName(category)}
                  </div>
                </div>

                <div className="news-content">
                  <h3
                      className="news-title"
                      onClick={() => handleNewsClick(item.id)}
                  >
                    {item.title}
                  </h3>
                  <p className="news-summary">{item.summary}</p>

                  <div className="news-meta">
                <span className="news-date">
                  {new Date(item.createdAt).toLocaleDateString()}
                </span>
                    <span className="news-views">👁️ {item.views || 0}</span>
                  </div>

                  {item.prosconsId && (
                      <div className="proscons-section">
                        <h4>찬반 토론</h4>
                        <div className="proscons-preview">
                          <div className="pros-preview">
                            <span className="pros-label">👍 찬성</span>
                            <p>{item.pros}</p>
                          </div>
                          <div className="cons-preview">
                            <span className="cons-label">👎 반대</span>
                            <p>{item.cons}</p>
                          </div>
                        </div>
                        <button
                            onClick={() => handleProsConsClick(item.prosconsId)}
                            className="view-discussion-btn"
                        >
                          토론 보기 →
                        </button>
                      </div>
                  )}
                </div>
              </div>
          ))}
        </div>

        {totalPages > 1 && (
            <div className="pagination">
              <button
                  onClick={() => handlePageChange(currentPage - 1)}
                  disabled={currentPage === 0}
                  className="page-btn"
              >
                이전
              </button>

              {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
                let pageNum;
                if (totalPages <= 5) {
                  pageNum = i;
                } else if (currentPage < 3) {
                  pageNum = i;
                } else if (currentPage > totalPages - 3) {
                  pageNum = totalPages - 5 + i;
                } else {
                  pageNum = currentPage - 2 + i;
                }

                return (
                    <button
                        key={pageNum}
                        onClick={() => handlePageChange(pageNum)}
                        className={`page-btn ${currentPage === pageNum ? 'active' : ''}`}
                    >
                      {pageNum + 1}
                    </button>
                );
              })}

              <button
                  onClick={() => handlePageChange(currentPage + 1)}
                  disabled={currentPage === totalPages - 1}
                  className="page-btn"
              >
                다음
              </button>
            </div>
        )}

        {news.length === 0 && (
            <div className="no-news">
              <p>이 카테고리에 뉴스가 없습니다.</p>
            </div>
        )}
      </div>
  );
};

export default CategoryNews;
