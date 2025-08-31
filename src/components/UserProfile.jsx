import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './UserProfile.css';

const UserProfile = () => {
  const [user, setUser] = useState(null);
  const [isEditing, setIsEditing] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    age: '',
    gender: '',
    job: ''
  });

  useEffect(() => {
    fetchUserProfile();
  }, []);

  const fetchUserProfile = async () => {
    try {
      const token = localStorage.getItem('token');
      if (!token) {
        setError('로그인이 필요합니다.');
        setIsLoading(false);
        return;
      }

      const response = await axios.get('/api/users/me', {
        headers: { Authorization: `Bearer ${token}` }
      });

      if (response.data.success) {
        const userData = response.data.data;
        setUser(userData);
        setFormData({
          username: userData.username || '',
          email: userData.email || '',
          age: userData.age || '',
          gender: userData.gender || '',
          job: userData.job || ''
        });
      }
    } catch (err) {
      setError('사용자 정보를 불러오는데 실패했습니다.');
      console.error('Profile fetch error:', err);
    } finally {
      setIsLoading(false);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    try {
      const token = localStorage.getItem('token');
      const response = await axios.patch('/api/users/me', formData, {
        headers: { Authorization: `Bearer ${token}` }
      });

      if (response.data.success) {
        alert('프로필이 성공적으로 수정되었습니다!');
        setIsEditing(false);
        fetchUserProfile(); // 최신 정보로 업데이트
      }
    } catch (err) {
      alert('프로필 수정 중 오류가 발생했습니다.');
      console.error('Profile update error:', err);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
    window.location.href = '/login';
  };

  if (isLoading) {
    return <div className="loading">로딩 중...</div>;
  }

  if (error) {
    return <div className="error">{error}</div>;
  }

  if (!user) {
    return <div className="error">사용자 정보를 불러올 수 없습니다.</div>;
  }

  return (
    <div className="user-profile">
      <div className="profile-header">
        <h1>내 프로필</h1>
        <div className="profile-actions">
          {!isEditing && (
            <button onClick={() => setIsEditing(true)} className="edit-btn">
              ✏️ 수정하기
            </button>
          )}
          <button onClick={handleLogout} className="logout-btn">
            🚪 로그아웃
          </button>
        </div>
      </div>

      <div className="profile-content">
        <div className="profile-avatar">
          <img src="/profile-icon.png" alt="프로필" className="avatar-img" />
          <div className="avatar-overlay">
            <span>프로필 사진</span>
          </div>
        </div>

        {!isEditing ? (
          <div className="profile-info">
            <div className="info-group">
              <label>사용자명</label>
              <p>{user.username || '설정되지 않음'}</p>
            </div>
            <div className="info-group">
              <label>이메일</label>
              <p>{user.email || '설정되지 않음'}</p>
            </div>
            <div className="info-group">
              <label>나이</label>
              <p>{user.age ? `${user.age}세` : '설정되지 않음'}</p>
            </div>
            <div className="info-group">
              <label>성별</label>
              <p>{user.gender || '설정되지 않음'}</p>
            </div>
            <div className="info-group">
              <label>직업</label>
              <p>{user.job || '설정되지 않음'}</p>
            </div>
            <div className="info-group">
              <label>가입일</label>
              <p>{new Date(user.createdAt).toLocaleDateString()}</p>
            </div>
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="profile-form">
            <div className="form-group">
              <label htmlFor="username">사용자명</label>
              <input
                type="text"
                id="username"
                name="username"
                value={formData.username}
                onChange={handleInputChange}
                placeholder="사용자명을 입력하세요"
              />
            </div>
            
            <div className="form-group">
              <label htmlFor="email">이메일</label>
              <input
                type="email"
                id="email"
                name="email"
                value={formData.email}
                onChange={handleInputChange}
                placeholder="이메일을 입력하세요"
              />
            </div>
            
            <div className="form-group">
              <label htmlFor="age">나이</label>
              <input
                type="number"
                id="age"
                name="age"
                value={formData.age}
                onChange={handleInputChange}
                placeholder="나이를 입력하세요"
                min="1"
                max="120"
              />
            </div>
            
            <div className="form-group">
              <label htmlFor="gender">성별</label>
              <select
                id="gender"
                name="gender"
                value={formData.gender}
                onChange={handleInputChange}
              >
                <option value="">선택하세요</option>
                <option value="남성">남성</option>
                <option value="여성">여성</option>
                <option value="기타">기타</option>
              </select>
            </div>
            
            <div className="form-group">
              <label htmlFor="job">직업</label>
              <select
                id="job"
                name="job"
                value={formData.job}
                onChange={handleInputChange}
              >
                <option value="">선택하세요</option>
                <option value="학생">학생</option>
                <option value="회사원">회사원</option>
                <option value="자영업자">자영업자</option>
                <option value="전문직">전문직</option>
                <option value="주부">주부</option>
                <option value="무직">무직</option>
                <option value="기타">기타</option>
              </select>
            </div>
            
            <div className="form-actions">
              <button type="submit" className="save-btn">
                💾 저장하기
              </button>
              <button 
                type="button" 
                onClick={() => setIsEditing(false)}
                className="cancel-btn"
              >
                ❌ 취소하기
              </button>
            </div>
          </form>
        )}
      </div>

      <div className="profile-stats">
        <h3>활동 통계</h3>
        <div className="stats-grid">
          <div className="stat-item">
            <span className="stat-number">{user.voteCount || 0}</span>
            <span className="stat-label">투표 참여</span>
          </div>
          <div className="stat-item">
            <span className="stat-number">{user.commentCount || 0}</span>
            <span className="stat-label">댓글 작성</span>
          </div>
          <div className="stat-item">
            <span className="stat-number">{user.createdProsconsCount || 0}</span>
            <span className="stat-label">찬반 생성</span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default UserProfile;
