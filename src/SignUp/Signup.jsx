import React, { useState } from 'react';
import './Signup.css';
import logo from '../assets/logo.png';
import { Link } from 'react-router-dom';

export default function Signup() {
  const [form, setForm] = useState({
    username: '',         // <-- ERD에서 username으로 변경
    password: '',         // <-- ERD에서 password로 변경
    pwConfirm: '',
    email: '',
    birth_year: '',       // <-- ERD에서 birth_year로 변경
    sex: '',              // <-- ERD에서 sex로 변경
    job: '',              // <-- ERD에서 job으로 변경
  });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const jobList = ['대학생', '직장인', '전문가', '기타'];
  const genderList = ['남성', '여성', '기타'];

  const currentYear = new Date().getFullYear();

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSignup = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    if (form.password !== form.pwConfirm) {
      alert('비밀번호가 일치하지 않습니다.');
      setError('비밀번호가 일치하지 않습니다.');
      return;
    }
    try {
      // [API 명세서 + ERD 반영] 엔드포인트 및 필드명 수정
      const response = await fetch('/api/auth/register', { // <-- API 명세서 경로 사용
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          username: form.username,  // <-- ERD 명칭 반영
          password: form.password,  // <-- ERD 명칭 반영
          email: form.email,
          birth_year: form.birth_year,
          sex: form.sex,
          job: form.job,
        }),
      });
      if (response.ok) {
        alert('회원가입 성공!');
        setSuccess('회원가입 성공!');
        setForm({
          username: '',
          password: '',
          pwConfirm: '',
          email: '',
          birth_year: '',
          sex: '',
          job: '',
        });
      } else {
        const result = await response.json();
        alert(result.message || '회원가입 실패');
        setError(result.message || '회원가입 실패');
      }
    } catch {
      alert('네트워크 오류');
      setError('네트워크 오류');
    }
  };

  return (
    <div className="signup-container">
      <img src={logo} alt="logo" className="logo-img" />
      <form onSubmit={handleSignup}>
        {/* 사용자명, 비밀번호, 비밀번호 확인 */}
        <input
          type="text"
          name="username"
          value={form.username}
          onChange={handleChange}
          placeholder="사용자명"
          required
        />
        <input
          type="password"
          name="password"
          value={form.password}
          onChange={handleChange}
          placeholder="비밀번호"
          required
        />
        <input
          type="password"
          name="pwConfirm"
          value={form.pwConfirm}
          onChange={handleChange}
          placeholder="비밀번호 확인"
          required
        />

        {/* 이메일, 탄생년도, 성별 */}
        <input
          type="email"
          name="email"
          value={form.email}
          onChange={handleChange}
          placeholder="이메일"
          required
        />
        <input
          type="number"
          name="birth_year"
          value={form.birth_year}
          onChange={handleChange}
          placeholder="탄생년도 (예: 1990)"
          min="1900"
          max={currentYear}
          required
        />
        <select
          name="sex"
          value={form.sex}
          onChange={handleChange}
          required
        >
          <option value="" disabled>성별을 선택하세요</option>
          {genderList.map((g) => (
            <option key={g} value={g}>{g}</option>
          ))}
        </select>

        {/* 직업 선택 */}
        <select
          name="job"
          value={form.job}
          onChange={handleChange}
          required
        >
          <option value="" disabled>직업을 선택하세요</option>
          {jobList.map((j) => (
            <option key={j} value={j}>{j}</option>
          ))}
        </select>

        <button type="submit">Sign up</button>
        <Link to="/" className="signup-link">back</Link>
      </form>
    </div>
  );
}