## Streak

스터디 그룹의 개설부터 세션(회차) 진행, 출석·과제 관리, 마이페이지 대시보드까지 지원하는 스터디 관리 서비스입니다.

> 이 문서는 초안입니다. 기능이 추가/변경될 때마다 아래 섹션들을 계속 업데이트해 주세요.

### 목차

1. [주요 기능](#주요-기능)
2. [기술 스택](#기술-스택)
3. [디렉토리 구조](#디렉토리-구조)
4. [핵심 기능 흐름](#핵심-기능-흐름)
5. [실행 방법](#실행-방법)
6. [테스트 계정](#테스트-계정)
7. [API 인증 흐름](#api-인증-흐름)
8. [API 목록](#api-목록)
9. [DB 스키마](#db-스키마)

---

### 주요 기능

- 회원가입 / 로그인 (세션 기반 인증)
- 스터디 개설, 정보 수정, 상태 관리(모집중 → 마감 → 종료), 리더 위임
- 스터디 세션(회차) 생성 및 관리
- 세션별 과제 등록 및 관리
- 세션별 출석 체크
- 마이페이지: 참여 스터디 목록, 출석률/출석 스트릭, 마감 임박 과제, 오늘의 일정, 대시보드 요약
- 월간 캘린더 일정 조회

> 스터디 참여자(Participant)/가입 신청(Application)/과제 제출(Submission) 도메인은 엔티티·레포지토리까지는 구현되어 있지만, 아직 전용 API(신청/승인·거절/제출 등)는 없습니다. 관련 데이터는 마이페이지 조회 API에서 간접적으로 사용됩니다.

### 기술 스택

- Java 21, Spring Boot 4.1.0
- Spring Data JPA (Hibernate), MySQL 8
- Spring Security (세션 + CSRF 기반 인증)
- Gradle

### 디렉토리 구조

도메인 단위 패키지 구조를 따릅니다. 각 도메인 패키지는 보통 `controller / service / repository / entity / dto / converter` 로 구성됩니다.

```
src/main/java/com/example
├── StreakApplication.java
├── auth/            # 회원가입, 로그인/로그아웃, CSRF
├── member/           # 회원 정보 조회/수정
├── study/            # 스터디 개설/수정/상태관리/리더위임
├── session/          # 스터디 세션(회차)
├── assignment/       # 세션별 과제
├── attendance/       # 세션별 출석
├── mypage/           # 마이페이지 대시보드/집계 조회
├── calendar/         # 월간 일정 조회
├── participant/      # 스터디 참여자 (엔티티만, API 없음)
├── application/      # 스터디 가입 신청 (엔티티만, API 없음)
├── submission/       # 과제 제출 (엔티티만, API 없음)
└── global/           # 공통 응답/예외 처리, 시큐리티 설정, BaseEntity

src/main/resources
├── application.properties
├── schema.sql        # 전체 테이블 DDL
├── data.sql          # 테스트용 더미 데이터
└── static/           # 정적 프론트엔드 (index.html, app.js, style.css)
```

### 핵심 기능 흐름

**1. 회원가입 → 로그인**
`POST /api/auth/signup`으로 가입 후, `GET /api/auth/csrf`로 토큰을 받아 `POST /api/auth/login`으로 로그인합니다. 로그인 성공 시 서버가 `HttpSession`에 인증 정보를 저장하고, 이후 요청은 세션 쿠키로 인증됩니다.

**2. 스터디 개설 → 운영**
리더가 `POST /api/studies`로 스터디를 개설하면 `RECRUITING` 상태로 시작합니다. `PATCH /api/studies/{studyId}/status`로 모집 상태를 전환하고, 필요 시 `PATCH /api/studies/{studyId}/leader`로 리더를 위임합니다.

**3. 세션 → 과제/출석**
스터디 아래에 `POST /api/studies/{studyId}/sessions`로 회차(세션)를 만들고, 각 세션에 `POST .../assignments`로 과제를 등록합니다. 세션이 끝나면 `PATCH .../attendances`로 참여자 출석 상태(`PRESENT`/`ABSENT`/`UNMARKED`)를 일괄 갱신합니다.

**4. 마이페이지 집계**
`mypage` 도메인은 회원 기준으로 참여 스터디, 출석률, 출석 스트릭, 마감 임박 과제, 오늘 일정 등을 집계해서 보여줍니다. 대시보드(`GET /api/members/me/dashboard`)가 이 정보들을 한 번에 모아 반환합니다.

### 실행 방법

#### 1. DB 준비

```sql
CREATE DATABASE streak CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'ureca'@'%' IDENTIFIED BY 'ureca';
GRANT ALL PRIVILEGES ON streak.* TO 'ureca'@'%';
FLUSH PRIVILEGES;
```

접속 정보는 [application.properties](src/main/resources/application.properties)에 이미 설정되어 있습니다 (`jdbc:mysql://localhost:3306/streak`, `ureca`/`ureca`). 다른 DB 계정을 쓰고 싶다면 이 파일을 수정하세요.

#### 2. 앱 실행

```bash
./gradlew bootRun
```

앱을 실행하면 다음이 자동으로 일어납니다.

1. Hibernate가 엔티티 기준으로 테이블을 생성/갱신 (`ddl-auto=update`)
2. [schema.sql](src/main/resources/schema.sql)이 실행되어 테이블을 초기화
3. [data.sql](src/main/resources/data.sql)이 실행되어 테스트용 더미 데이터가 채워짐

즉 **재시작할 때마다 DB가 초기화되고 더미 데이터로 다시 채워지므로**, 테스트 중 직접 추가/수정한 데이터는 재시작 시 사라집니다.

서버는 기본적으로 `http://localhost:8080`에서 뜨고, 정적 프론트엔드([static/index.html](src/main/resources/static/index.html))가 루트(`/`)에서 바로 제공됩니다.

### 테스트 계정

`data.sql`에 미리 채워진 계정입니다. 비밀번호는 전부 동일합니다.

| 이메일 | 이름 | 역할 | 비밀번호 |
| --- | --- | --- | --- |
| leader1@test.com | 김리더 | 알고리즘 스터디 리더 | password123! |
| member1@test.com | 이멤버 | 알고리즘 스터디 멤버 (과제 제출 완료) | password123! |
| member2@test.com | 박멤버 | 알고리즘 스터디 멤버 (미제출 상태 테스트용) | password123! |
| member3@test.com | 최지원 | 미가입 회원 (가입 신청 PENDING/REJECTED 데이터 보유) | password123! |
| leader2@test.com | 정리더 | 토익 900 스터디 리더 | password123! |

이 외에도 `member4`~`member16`, `leader3`~`leader4` 패턴(예: `member4@test.com`)으로 15개 계정이 더 있으며, 비밀번호는 모두 동일합니다 (정보처리기사 스터디, 사이드 프로젝트 스터디 등에 배정됨).

새 계정이 필요하면 `/api/auth/signup`으로 직접 회원가입해도 됩니다.

### API 인증 흐름

세션 기반 인증 + CSRF 토큰을 사용합니다. 로그인 전에 CSRF 토큰을 먼저 발급받아야 합니다.

```bash
# 1) CSRF 토큰 발급 (쿠키 저장)
curl -c cookies.txt http://localhost:8080/api/auth/csrf

# 2) 응답의 token 값을 X-XSRF-TOKEN 헤더에 담아 로그인
curl -b cookies.txt -c cookies.txt -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -H "X-XSRF-TOKEN: <위에서 받은 token>" \
  -d '{"email":"leader1@test.com","password":"password123!"}'

# 3) 이후 요청은 같은 쿠키로 인증 유지 (로그인 필요한 API 호출 시에도 X-XSRF-TOKEN 헤더 필요)
curl -b cookies.txt http://localhost:8080/api/members/me
```

### API 목록

| 도메인 | 메서드/경로 | 설명 |
| --- | --- | --- |
| 인증 | `GET /api/auth/csrf` | CSRF 토큰 발급 |
| 인증 | `POST /api/auth/signup` | 회원가입 |
| 인증 | `POST /api/auth/login` | 로그인 |
| 인증 | `POST /api/auth/logout` | 로그아웃 |
| 회원 | `GET /api/members/me` | 내 정보 조회 |
| 회원 | `PATCH /api/members/me` | 내 정보(이름/전화번호/비밀번호) 수정 |
| 스터디 | `GET /api/studies` | 스터디 목록 조회 |
| 스터디 | `POST /api/studies` | 스터디 개설 |
| 스터디 | `PATCH /api/studies/{studyId}` | 스터디 정보 수정 |
| 스터디 | `PATCH /api/studies/{studyId}/status` | 스터디 상태 변경 (모집중/마감/종료) |
| 스터디 | `PATCH /api/studies/{studyId}/leader` | 스터디 리더 위임 |
| 스터디 | `DELETE /api/studies/{studyId}` | 스터디 삭제(소프트 딜리트) |
| 세션 | `GET/POST /api/studies/{studyId}/sessions` | 세션 목록 조회/생성 |
| 세션 | `GET/PATCH/DELETE /api/studies/{studyId}/sessions/{sessionId}` | 세션 상세/수정/삭제 |
| 과제 | `GET/POST /api/studies/{studyId}/sessions/{sessionId}/assignments` | 과제 목록 조회/생성 |
| 과제 | `GET/PATCH/DELETE /api/studies/{studyId}/sessions/{sessionId}/assignments/{assignmentId}` | 과제 상세/수정/삭제 |
| 출석 | `GET /api/studies/{studyId}/sessions/{sessionId}/attendances` | 세션 출석부 조회 |
| 출석 | `PATCH /api/studies/{studyId}/sessions/{sessionId}/attendances` | 출석 상태 일괄 변경 |
| 캘린더 | `GET /api/members/me/calendar` | 월간 일정 조회 |
| 마이페이지 | `GET /api/members/me/dashboard` | 대시보드 요약 |
| 마이페이지 | `GET /api/members/me/studies` | 내가 참여 중인 스터디 목록 |
| 마이페이지 | `GET /api/members/me/attendance` | 내 출석률 |
| 마이페이지 | `GET /api/members/me/attendance/streak` | 내 최장 출석 스트릭 |
| 마이페이지 | `GET /api/members/me/assignments` | 마감 임박 과제 목록 |
| 마이페이지 | `GET /api/members/me/applications` | 내 가입 신청 목록 |
| 마이페이지 | `GET /api/members/me/schedule/today` | 오늘의 일정 |

### DB 스키마

전체 테이블 구조는 [schema.sql](src/main/resources/schema.sql)을 참고하세요. `members`, `studies`, `participants`, `applications`, `study_sessions`, `assignments`, `attendances`, `submissions` 8개 테이블로 구성되어 있습니다.
