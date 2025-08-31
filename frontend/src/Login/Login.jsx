import React, { useState } from 'react';
import './Login.css';
import logo from '../assets/logo.png';
import { Link, useNavigate } from 'react-router-dom';

export default function Login() {
  const [email, setEmail] = useState(''); // username → email
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  // 로그인 처리 함수
  const handleLogin = async (e) => {
    e.preventDefault();
    setError('');

    const trimmedEmail = email.trim();
    const trimmedPassword = password.trim();

    if (trimmedPassword.length < 4) {
      setError('비밀번호는 최소 4자 이상이어야 합니다.');
      return;
    }

    try {
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          emailOrUsername: trimmedEmail,
          password: trimmedPassword,
        }),
      });

      const result = await response.json();

      if (response.ok && result.success) {
        localStorage.setItem('accessToken', result.data.accessToken);
        localStorage.setItem('refreshToken', result.data.refreshToken);
        alert('로그인 성공!');
        navigate('/home'); // /home 경로의 Home 컴포넌트로 이동
      } else {
        const message = result.message || '로그인 실패';
        setError(message);
        alert(message);
      }
    } catch (err) {
      console.error(err);
      setError('네트워크 오류가 발생했습니다.');
      alert('네트워크 오류');
    }
  };

  // 액세스 토큰 재발급 함수
  const refreshAccessToken = async () => {
    const refreshToken = localStorage.getItem('refreshToken');
    if (!refreshToken) return null;

    try {
      const response = await fetch('/api/auth/refresh', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ refreshToken }),
      });

      const data = await response.json();

      if (response.ok && data.success) {
        localStorage.setItem('accessToken', data.data.accessToken);
        return data.data.accessToken;
      } else {
        return null;
      }
    } catch (err) {
      console.error(err);
      return null;
    }
  };

  return (
    <div className="login-container">
      <img src={logo} alt="logo" className="login-logo-img" />
      <form onSubmit={handleLogin}>
        <input
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          placeholder="이메일"
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
      <Link to="/signup" className="signup-link">
        Sign up
      </Link>
      {error && <p className="error-message">{error}</p>}
    </div>
  );
}
