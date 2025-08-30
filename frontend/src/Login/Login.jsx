import React, { useState } from 'react';
import './Login.css';
import logo from '../assets/logo.png';
import { Link, useNavigate } from 'react-router-dom';

export default function Login() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    setError('');
    try {
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password }),
      });
      const result = await response.json();
      if (response.ok) {
        alert('로그인 성공!');
        localStorage.setItem('accessToken', result.accessToken);
        localStorage.setItem('refreshToken', result.refreshToken);
        navigate('/');
      } else {
        alert(result.message || '로그인 실패');
        setError(result.message || '로그인 실패');
      }
    } catch {
      alert('네트워크 오류');
      setError('네트워크 오류');
    }
  };

  // 액세스 토큰 재발급 함수 (필요 시 호출)
  const refreshAccessToken = async () => {
    const refreshToken = localStorage.getItem('refreshToken');
    if (!refreshToken) return null;
    try {
      const response = await fetch('/api/auth/refresh', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ refreshToken }),
      });
      if (response.ok) {
        const data = await response.json();
        localStorage.setItem('accessToken', data.accessToken);
        return data.accessToken;
      } else {
        // 재발급 실패 시 로그아웃 처리 등 추가 구현 가능
        return null;
      }
    } catch {
      return null;
    }
  };

  return (
    <div className="login-container">
      <img src={logo} alt="logo" className="login-logo-img" />
      <form onSubmit={handleLogin}>
        <input
          type="text"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          placeholder="사용자명"
          required
        />
        <input
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          placeholder="비밀번호"
          required
        />
        <button type="submit">Login</button>
      </form>
      <Link to="/signup" className="signup-link">Sign up</Link>
      {error && <p className="error-message">{error}</p>}
    </div>
  );
}