# 🎯 Topicurator Frontend

AI 기반 뉴스 찬반 분석 및 토론 플랫폼의 프론트엔드 애플리케이션입니다.

## ✨ 주요 기능

### 🤖 AI 뉴스 분석기
- **URL 입력**: 뉴스 URL을 입력하면 AI가 자동으로 분석
- **자동 요약**: 뉴스 내용을 간결하게 요약
- **찬반 분석**: 찬성, 중립, 반대 관점으로 분석
- **편향도 측정**: 뉴스의 편향 정도를 시각적으로 표시
- **결과 저장**: 분석 결과를 데이터베이스에 저장 (로그인 필요)

### 📰 카테고리별 뉴스
- **정치, 경제, 사회, 국제, 문화, 스포츠, 기술, 연예** 카테고리 지원
- **정렬 기능**: 최신순, 조회순으로 정렬
- **페이지네이션**: 대량의 뉴스를 효율적으로 탐색
- **찬반 미리보기**: 각 뉴스의 찬반 분석 결과 미리보기

### 🗳️ 찬반 토론 시스템
- **투표 참여**: 찬성/반대 투표로 의견 표현
- **실시간 통계**: 투표 결과를 시각적으로 확인
- **댓글 시스템**: 의견을 댓글로 공유
- **좋아요/싫어요**: 댓글에 대한 반응 표현

### 👤 사용자 프로필
- **개인정보 관리**: 사용자명, 이메일, 나이, 성별, 직업 설정
- **활동 통계**: 투표 참여, 댓글 작성, 찬반 생성 횟수 확인
- **프로필 수정**: 언제든지 개인정보 수정 가능

## 🚀 시작하기

### 필수 요구사항
- Node.js 16.0.0 이상
- npm 또는 yarn

### 설치 및 실행

1. **의존성 설치**
   ```bash
   npm install
   ```

2. **개발 서버 실행**
   ```bash
   npm run dev
   ```

3. **브라우저에서 확인**
   ```
   http://localhost:5173
   ```

### 빌드
```bash
npm run build
```

## 📁 프로젝트 구조

```
src/
├── components/           # 재사용 가능한 컴포넌트
│   ├── NewsAnalyzer.jsx     # AI 뉴스 분석기
│   ├── ProsConsDetail.jsx   # 찬반 상세 페이지
│   ├── UserProfile.jsx      # 사용자 프로필
│   ├── CategoryNews.jsx     # 카테고리별 뉴스
│   └── *.css               # 각 컴포넌트별 스타일
├── Home/                # 홈 페이지
├── Login/               # 로그인 페이지
├── SignUp/              # 회원가입 페이지
├── Social/              # 사회 뉴스 페이지
├── detail/              # 투표 상세 페이지
└── App.jsx              # 메인 앱 컴포넌트
```

## 🔗 API 연동

### 백엔드 API 엔드포인트
- **인증**: `/api/auth/*` (로그인, 회원가입, 로그아웃)
- **사용자**: `/api/users/*` (프로필 조회, 수정)
- **뉴스**: `/api/news/*` (뉴스 조회, 카테고리별 검색)
- **LLM**: `/api/llm/*` (AI 분석, 요약)
- **찬반**: `/api/proscons/*` (찬반 생성, 조회)
- **투표**: `/api/news/*/votes/*` (투표 등록, 통계)
- **댓글**: `/api/*/comments/*` (댓글 CRUD)

### 인증 방식
- JWT 토큰 기반 인증
- Access Token + Refresh Token 방식
- 자동 토큰 갱신

## 🎨 UI/UX 특징

### 디자인 시스템
- **모던한 카드 기반 레이아웃**
- **반응형 디자인**: 모바일, 태블릿, 데스크톱 지원
- **부드러운 애니메이션**: 호버 효과, 전환 애니메이션
- **직관적인 네비게이션**: 명확한 메뉴 구조

### 색상 팔레트
- **Primary**: #007bff (파란색)
- **Success**: #28a745 (초록색)
- **Warning**: #ffc107 (노란색)
- **Danger**: #dc3545 (빨간색)
- **Neutral**: #6c757d (회색)

## 📱 반응형 지원

### 브레이크포인트
- **모바일**: 768px 이하
- **태블릿**: 768px ~ 1024px
- **데스크톱**: 1024px 이상

### 모바일 최적화
- 터치 친화적인 버튼 크기
- 스와이프 제스처 지원
- 모바일 네비게이션 메뉴

## 🔧 개발 도구

### 사용 기술
- **React 18**: 최신 React 기능 활용
- **React Router**: SPA 라우팅
- **Axios**: HTTP 클라이언트
- **CSS3**: 모던 CSS 기능 (Grid, Flexbox, 애니메이션)

### 개발 환경
- **Vite**: 빠른 개발 서버 및 빌드 도구
- **ESLint**: 코드 품질 관리
- **Hot Module Replacement**: 실시간 코드 반영

## 🚀 배포

### 정적 호스팅
```bash
npm run build
```
빌드된 `dist` 폴더를 웹 서버에 업로드

### 환경 변수
```env
VITE_API_BASE_URL=http://localhost:8080
VITE_APP_TITLE=Topicurator
```

## 🤝 기여하기

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다.

## 📞 지원

문제가 있거나 질문이 있으시면 이슈를 생성해 주세요.

---

**Topicurator** - AI와 함께하는 스마트한 뉴스 토론 플랫폼 🚀
