# Streak - 스터디 통합 관리 서비스

### LG 유플러스 Ureca 4th 백엔드 과정 / 미니 프로젝트2

<br>

<img width="1533" height="750" alt="image" src="https://github.com/user-attachments/assets/4d8caa66-639c-4ee1-8cbe-3fbc4e13cef0" />

<br>
<br>
<br>

스터디 그룹의 개설부터 가입 신청/승인, 세션 진행, 출석/과제/과제제출 관리, 마이페이지 대시보드까지 지원하는 스터디 관리 서비스입니다. <br>
백엔드(Spring Boot)와 프론트엔드(React, `frontend/` 폴더)가 하나의 저장소에서 함께 관리됩니다.

<!-- 이 문서는 계속 업데이트되는 문서입니다. 기능이 추가/변경될 때마다 아래 섹션들을 갱신해 주세요. -->

<br>

## 목차

1. [주요 기능](#주요-기능)
2. [기술 스택](#기술-스택)
3. [디렉토리 구조](#디렉토리-구조)
4. [핵심 기능 흐름](#핵심-기능-흐름)
5. [실행 방법](#실행-방법)
6. [테스트 계정](#테스트-계정)
7. [API 인증 흐름](#api-인증-흐름)
8. [API 목록](#api-목록)
9. [DB 스키마](#db-스키마)

<br>
<br>

---

<br>
<br>

## 주요 기능

- 회원가입 / 로그인 (세션 + CSRF 기반 인증)
- 스터디 개설, 정보 수정, 상태 관리(모집중 → 마감 → 종료), 리더 위임, 삭제
- 스터디 탐색(카테고리/제목 검색) 및 가입 신청 → 리더의 신청 승인/거절, 신청 취소
- 스터디 세션(회차) 생성 및 관리
- 세션별 과제 등록/수정/삭제 및 과제 제출(등록/수정/삭제/조회)
- 세션별 출석 체크 및 스터디 단위 출석 현황 조회
- 마이페이지: 참여 스터디 목록, 출석률/출석 스트릭, 마감 임박 과제, 오늘의 일정, 대시보드 요약
- 월간 캘린더 일정 조회
- React 기반 실제 프론트엔드가 위 기능을 백엔드 API와 전부 연동해 제공

<!-- `participant`(스터디 참여자) 도메인은 엔티티·레포지토리까지만 구현되어 있고 아직 전용 API가 없습니다. 즉 **스터디 탈퇴(본인이 나가기)나 리더의 멤버 강퇴 기능은 없습니다.** 참여자 데이터 자체는 다른 도메인(출석, 마이페이지 등)에서 간접적으로 조회됩니다. -->

<br>
<br>

## 기술 스택

**백엔드**
- Java 21, Spring Boot
- Spring Data JPA (Hibernate), MySQL 8
- Spring Security (세션 + CSRF 기반 인증)
- Gradle

**프론트엔드** (Claude를 사용한 바이브코딩으로 구현)
- React 19 + TypeScript, Vite
- react-router-dom, recharts, lucide-react
- Tailwind CSS v4

<br>
<br>

## 디렉토리 구조

백엔드는 도메인 단위 패키지 구조를 따릅니다.
각 도메인 패키지는 `controller / service / repository / entity / dto / converter` 로 구성됩니다.

```
src/main/java/com/example
├── StreakApplication.java
├── auth/            # 회원가입, 로그인/로그아웃, CSRF
├── member/           # 회원 정보 조회/수정
├── study/            # 스터디 개설/수정/상태관리/리더위임/탐색/상세
├── session/          # 스터디 세션(회차)
├── assignment/       # 세션별 과제
├── attendance/       # 세션별/스터디별 출석
├── application/      # 스터디 가입 신청/승인/거절/취소
├── submission/       # 과제 제출 등록/수정/삭제/조회
├── mypage/           # 마이페이지 대시보드/집계 조회
├── calendar/         # 월간 일정 조회
├── participant/      # 스터디 참여자 (엔티티만, API 없음 — 탈퇴/강퇴 기능 미구현)
└── global/           # 공통 응답/예외 처리, 시큐리티 설정, BaseEntity, @CurrentUser

src/main/resources
├── application.properties
├── schema.sql        # 전체 테이블 DDL
├── data.sql          # 테스트용 더미 데이터 (서버 재시작 시마다 다시 로드됨)
└── static/           # 프론트엔드 빌드 산출물(frontend/npm run build 결과가 여기로 들어감)
```

프론트엔드는 `frontend/` 폴더에 독립된 Vite 프로젝트로 존재합니다.

```
frontend/src
├── api/          # 도메인별 fetch 래퍼 (auth, studies, sessions, assignments, submissions, attendance, applications, member, mypage, calendar, client, types)
├── context/      # AuthContext.tsx - 로그인 세션 상태 관리
├── pages/        # 라우트별 화면 (Home, Login, Signup, StudyExplore, StudyDetail, MyStudyList, RoundList, RoundDetail, AssignmentCreate/Detail, ApplicantManage, CalendarPage, MyPage 등)
├── components/   # 공통 컴포넌트 (Avatar, Badge, Card, Modal, StatTile, TopNav, Topbar)
├── layouts/      # AppLayout.tsx
├── lib/          # format, helpers, labels 등 유틸
└── data/         # mock.ts (데모/폴백 데이터)
```

<br>
<br>

## 핵심 기능 흐름

<br>

<img width="1683" height="942" alt="image" src="https://github.com/user-attachments/assets/f0632218-62a7-4094-937c-858715d57e7f" />

<br>
<br>

**1. 회원가입 → 로그인**
`POST /api/auth/signup`으로 가입 후, `GET /api/auth/csrf`로 토큰을 받아 `POST /api/auth/login`으로 로그인합니다. <br>
로그인 성공 시 서버가 `HttpSession`에 인증 정보를 저장하고, 이후 요청은 세션 쿠키로 인증됩니다.

**2. 스터디 개설 → 운영**
리더가 `POST /api/studies`로 스터디를 개설하면 `RECRUITING` 상태로 시작합니다. <br>
`PATCH /api/studies/{studyId}/status`로 모집 상태를 전환하고, 필요 시 `PATCH /api/studies/{studyId}/leader`로 리더를 위임합니다.

**3. 탐색 → 가입 신청 → 승인**
`GET /api/studies?category=&title=`로 모집 중인 스터디를 검색하고, `GET /api/studies/{studyId}`로 상세를 확인한 뒤 `POST /api/studies/{studyId}/applications`로 가입을 신청합니다. <br>
리더는 `GET /api/studies/{studyId}/applications`로 신청자 목록을 확인하고 `PATCH /api/applications/{applicationId}/status`로 승인/거절합니다. <br>
신청자는 본인 신청을 `DELETE /api/applications/{applicationId}`로 취소할 수 있습니다.

**4. 세션 → 과제/제출/출석**
스터디 아래에 `POST /api/studies/{studyId}/sessions`로 회차(세션)를 만들고, 각 세션에 `POST .../assignments`로 과제를 등록합니다. <br>
참여자는 `POST .../submissions`로 과제를 제출합니다. 세션이 끝나면 `PATCH .../attendances`로 참여자 출석 상태(`PRESENT`/`ABSENT`/`UNMARKED`)를 일괄 갱신합니다.

**5. 마이페이지 집계**
`mypage` 도메인은 회원 기준으로 참여 스터디, 출석률, 출석 스트릭, 마감 임박 과제, 오늘 일정 등을 집계해서 보여줍니다. <br>
대시보드(`GET /api/members/me/dashboard`)가 이 정보들을 한 번에 모아 반환합니다.

**6. 프론트엔드 연동**
프론트엔드는 위 API 전체를 실제로 호출하도록 연동되어 있으며, 세션 쿠키 + CSRF 헤더 기반 인증을 `frontend/src/api/client`와 `AuthContext`에서 처리합니다.

<br>
<br>

## 실행 방법

로컬에 Java 21 + MySQL 8을 설치하고 Spring Boot 앱으로 직접 실행하는 것을 기준으로 합니다.

<br>

**1) DB 준비**

```sql
CREATE DATABASE streak CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'ureca'@'%' IDENTIFIED BY 'ureca';
GRANT ALL PRIVILEGES ON streak.* TO 'ureca'@'%';
FLUSH PRIVILEGES;
```

접속 정보는 [application.properties](src/main/resources/application.properties)에 이미 설정되어 있습니다 (`jdbc:mysql://localhost:3306/streak`, `ureca`/`ureca`).
(로컬 구동이기 때문에 환경변수 및 민감정보 관련해서는 미처리)

<br>

```bash
./gradlew bootRun
```

앱을 실행하면 아래 명령이 자동으로 수행됩니다.

1. [schema.sql](src/main/resources/schema.sql)이 실행되어 테이블을 초기화
2. [data.sql](src/main/resources/data.sql)이 실행되어 테스트용 더미 데이터가 채워짐

즉 **재시작할 때마다 DB가 초기화되고 더미 데이터로 다시 채워지므로**, 테스트 중 직접 추가/수정한 데이터는 재시작 시 사라집니다. 
(`http://localhost:8080`)

<br>
<br>

## 테스트 계정

`data.sql`에 미리 채워진 계정입니다. **실제로 로그인 가능한 계정은 아래 2개뿐**이며, 그 외 `member3@test.com` ~ `member20@test.com`은 비밀번호 해시가 더미값(`$2a$10$dummyhash`)으로 들어있어 로그인할 수 없습니다 (데이터 조회/연관관계 테스트용으로만 사용).

| 이메일 | 비밀번호 | 비고 |
| --- | --- | --- |
| test1@gmail.com | 1234qwer | study1 리더, study2 멤버 |
| test2@gmail.com | 1234qwer | study2 리더, study1 멤버 |

`member3` ~ `member20@test.com`은 여러 스터디에 리더/멤버/가입 신청(PENDING/APPROVED/REJECTED) 상태로 분산 배정되어 있어, 로그인 없이 데이터만 확인하는 조회성 테스트(리더 화면에서 신청자 목록 보기 등)에 활용할 수 있습니다.

새 계정이 필요하면 `/api/auth/signup`으로 직접 회원가입이 가능합니다.

<br>
<br>

## API 목록

| 도메인 | 메서드/경로 | 설명 |
| --- | --- | --- |
| 인증 | `GET /api/auth/csrf` | CSRF 토큰 발급 |
| 인증 | `POST /api/auth/signup` | 회원가입 |
| 인증 | `POST /api/auth/login` | 로그인 |
| 인증 | `POST /api/auth/logout` | 로그아웃 |
| 회원 | `GET /api/members/me` | 내 정보 조회 |
| 회원 | `PATCH /api/members/me` | 내 정보(이름/전화번호/비밀번호) 수정 |
| 스터디 | `GET /api/studies?category=&title=` | 스터디 탐색(검색/필터, 페이징, 로그인 필요) |
| 스터디 | `GET /api/studies/{studyId}` | 스터디 상세(가입 신청 화면용) |
| 스터디 | `GET /api/studies/me` | 내가 속한 스터디 목록(페이징) |
| 스터디 | `GET /api/studies/{studyId}/dashboard` | 스터디 상세 대시보드(세션 목록 페이징) |
| 스터디 | `POST /api/studies` | 스터디 개설 |
| 스터디 | `PATCH /api/studies/{studyId}` | 스터디 정보 수정 |
| 스터디 | `PATCH /api/studies/{studyId}/status?status=` | 스터디 상태 변경 (모집중/마감/종료) |
| 스터디 | `PATCH /api/studies/{studyId}/leader?newLeaderId=` | 스터디 리더 위임 |
| 스터디 | `DELETE /api/studies/{studyId}` | 스터디 삭제(소프트 딜리트) |
| 가입 신청 | `POST /api/studies/{studyId}/applications` | 스터디 가입 신청 |
| 가입 신청 | `GET /api/studies/{studyId}/applications` | 신청자 목록 조회 (리더 전용) |
| 가입 신청 | `PATCH /api/applications/{applicationId}/status` | 신청 승인/거절 (리더 전용) |
| 가입 신청 | `DELETE /api/applications/{applicationId}` | 내 신청 취소 |
| 세션 | `GET/POST /api/studies/{studyId}/sessions` | 세션 목록 조회/생성 |
| 세션 | `GET/PATCH/DELETE /api/studies/{studyId}/sessions/{sessionId}` | 세션 상세/수정/삭제 |
| 과제 | `GET/POST /api/studies/{studyId}/sessions/{sessionId}/assignments` | 과제 목록 조회/생성 |
| 과제 | `GET/PATCH/DELETE ` <br> `/api/studies/{studyId}/sessions/{sessionId}/` <br> `assignments/{assignmentId}` | 과제 상세/수정/삭제 |
| 과제 제출 | `GET/POST ` <br> `/api/studies/{studyId}/sessions/{sessionId}/` <br> `assignments/{assignmentId}/submissions` | 제출 목록 조회/등록 |
| 과제 제출 | `PATCH/DELETE ` <br> `/api/studies/{studyId}/sessions/{sessionId}/` <br> `assignments/{assignmentId}/submissions/{submissionId}` | 제출 수정/삭제 |
| 출석 | `GET /api/studies/{studyId}/attendances` | 스터디 단위 출석 현황 조회 |
| 출석 | `GET /api/studies/{studyId}/sessions/{sessionId}/attendances` | 세션 출석부 조회 |
| 출석 | `PATCH /api/studies/{studyId}/sessions/{sessionId}/attendances` | 출석 상태 일괄 변경 (리더 전용) |
| 캘린더 | `GET /api/members/me/calendar?month=yyyy-MM` | 월간 일정 조회 |
| 마이페이지 | `GET /api/members/me/dashboard` | 대시보드 요약 |
| 마이페이지 | `GET /api/members/me/studies` | 내가 참여 중인 스터디 목록 |
| 마이페이지 | `GET /api/members/me/attendance` | 내 출석률 |
| 마이페이지 | `GET /api/members/me/attendance/streak` | 내 최장 출석 스트릭 |
| 마이페이지 | `GET /api/members/me/assignments` | 마감 임박 과제 목록 |
| 마이페이지 | `GET /api/members/me/applications?status=` | 내 가입 신청 목록 |
| 마이페이지 | `GET /api/members/me/schedule/today` | 오늘의 일정 |

<br>
<br>

## DB 스키마

전체 테이블 구조 : [schema.sql](src/main/resources/schema.sql) 
`members`, `studies`, `participants`, `applications`, `study_sessions`, `assignments`, `attendances`, `submissions` 8개 테이블로 구성되어 있습니다.
