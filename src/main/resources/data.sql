-- Streak 프로젝트 더미(테스트) 데이터
-- application.properties의 spring.sql.init.mode=always 설정에 의해
-- 앱 실행 시 schema.sql 다음에 자동으로 실행된다. (수동 실행: mysql -u ureca -p streak < src/main/resources/data.sql)
--
-- 모든 계정의 비밀번호는 "password123!" 이다 (BCrypt로 해싱된 값이 저장되어 있어
-- 로그인 API(/api/auth/login)로 바로 로그인 테스트가 가능하다).
--
-- 구성: 회원 20명 / 스터디 4개 / 세션 8개 / 과제 5개 / 출석 38건 / 제출 8건 / 가입신청 6건
--
-- 스터디별 시나리오
--   1. 알고리즘 스터디   (ALGORITHM, RECRUITING) - leader1 리더 + member1,2,4,5 (5/5명, 정원 마감 임박)
--   2. 토익 900 스터디   (ENGLISH,   CLOSED)     - leader2 리더 + member6,7,8   (4/4명, 정원 마감)
--   3. 정보처리기사 스터디 (CERTIFICATE, RECRUITING) - leader3 리더 + member9~13 (6/6명)
--   4. 사이드 프로젝트 스터디 (ETC, ENDED)        - leader4 리더 + member14,15,16 (4/5명, 종료됨)
--   member3(최지원)은 어느 스터디에도 속하지 않은 회원으로, 가입 신청(PENDING/REJECTED) 테스트용

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE submissions;
TRUNCATE TABLE attendances;
TRUNCATE TABLE assignments;
TRUNCATE TABLE study_sessions;
TRUNCATE TABLE applications;
TRUNCATE TABLE participants;
TRUNCATE TABLE studies;
TRUNCATE TABLE members;
SET FOREIGN_KEY_CHECKS = 1;

-- 회원 20명 (비밀번호 원문: password123!)
INSERT INTO members (id, email, password, name, phone, status, created_at, updated_at) VALUES
(1,  'leader1@test.com', '$2a$10$yJnM9UjBKamN5JtgJnUjR.E6fGp95V4wz835T1CVODcqTrqYk4aga', '김리더', '010-1111-1111', 'ACTIVE', NOW(), NOW()),
(2,  'member1@test.com', '$2a$10$yJnM9UjBKamN5JtgJnUjR.E6fGp95V4wz835T1CVODcqTrqYk4aga', '이멤버', '010-2222-2222', 'ACTIVE', NOW(), NOW()),
(3,  'member2@test.com', '$2a$10$yJnM9UjBKamN5JtgJnUjR.E6fGp95V4wz835T1CVODcqTrqYk4aga', '박멤버', '010-3333-3333', 'ACTIVE', NOW(), NOW()),
(4,  'member3@test.com', '$2a$10$yJnM9UjBKamN5JtgJnUjR.E6fGp95V4wz835T1CVODcqTrqYk4aga', '최지원', '010-4444-4444', 'ACTIVE', NOW(), NOW()),
(5,  'leader2@test.com', '$2a$10$yJnM9UjBKamN5JtgJnUjR.E6fGp95V4wz835T1CVODcqTrqYk4aga', '정리더', '010-5555-5555', 'ACTIVE', NOW(), NOW()),
(6,  'member4@test.com', '$2a$10$yJnM9UjBKamN5JtgJnUjR.E6fGp95V4wz835T1CVODcqTrqYk4aga', '강민수', '010-1006-1006', 'ACTIVE', NOW(), NOW()),
(7,  'member5@test.com', '$2a$10$yJnM9UjBKamN5JtgJnUjR.E6fGp95V4wz835T1CVODcqTrqYk4aga', '윤서연', '010-1007-1007', 'ACTIVE', NOW(), NOW()),
(8,  'member6@test.com', '$2a$10$yJnM9UjBKamN5JtgJnUjR.E6fGp95V4wz835T1CVODcqTrqYk4aga', '한도윤', '010-1008-1008', 'ACTIVE', NOW(), NOW()),
(9,  'member7@test.com', '$2a$10$yJnM9UjBKamN5JtgJnUjR.E6fGp95V4wz835T1CVODcqTrqYk4aga', '오하은', '010-1009-1009', 'ACTIVE', NOW(), NOW()),
(10, 'member8@test.com', '$2a$10$yJnM9UjBKamN5JtgJnUjR.E6fGp95V4wz835T1CVODcqTrqYk4aga', '서준혁', '010-1010-1010', 'ACTIVE', NOW(), NOW()),
(11, 'member9@test.com', '$2a$10$yJnM9UjBKamN5JtgJnUjR.E6fGp95V4wz835T1CVODcqTrqYk4aga', '임지우', '010-1011-1011', 'ACTIVE', NOW(), NOW()),
(12, 'member10@test.com', '$2a$10$yJnM9UjBKamN5JtgJnUjR.E6fGp95V4wz835T1CVODcqTrqYk4aga', '노현우', '010-1012-1012', 'ACTIVE', NOW(), NOW()),
(13, 'member11@test.com', '$2a$10$yJnM9UjBKamN5JtgJnUjR.E6fGp95V4wz835T1CVODcqTrqYk4aga', '백승민', '010-1013-1013', 'ACTIVE', NOW(), NOW()),
(14, 'leader3@test.com', '$2a$10$yJnM9UjBKamN5JtgJnUjR.E6fGp95V4wz835T1CVODcqTrqYk4aga', '조은지', '010-1014-1014', 'ACTIVE', NOW(), NOW()),
(15, 'member12@test.com', '$2a$10$yJnM9UjBKamN5JtgJnUjR.E6fGp95V4wz835T1CVODcqTrqYk4aga', '신유진', '010-1015-1015', 'ACTIVE', NOW(), NOW()),
(16, 'member13@test.com', '$2a$10$yJnM9UjBKamN5JtgJnUjR.E6fGp95V4wz835T1CVODcqTrqYk4aga', '곽태양', '010-1016-1016', 'ACTIVE', NOW(), NOW()),
(17, 'member14@test.com', '$2a$10$yJnM9UjBKamN5JtgJnUjR.E6fGp95V4wz835T1CVODcqTrqYk4aga', '문채원', '010-1017-1017', 'ACTIVE', NOW(), NOW()),
(18, 'member15@test.com', '$2a$10$yJnM9UjBKamN5JtgJnUjR.E6fGp95V4wz835T1CVODcqTrqYk4aga', '유건우', '010-1018-1018', 'ACTIVE', NOW(), NOW()),
(19, 'leader4@test.com', '$2a$10$yJnM9UjBKamN5JtgJnUjR.E6fGp95V4wz835T1CVODcqTrqYk4aga', '배수아', '010-1019-1019', 'ACTIVE', NOW(), NOW()),
(20, 'member16@test.com', '$2a$10$yJnM9UjBKamN5JtgJnUjR.E6fGp95V4wz835T1CVODcqTrqYk4aga', '남기훈', '010-1020-1020', 'ACTIVE', NOW(), NOW());

-- 스터디 4개
INSERT INTO studies (id, title, description, capacity, category, status, is_deleted, created_at, updated_at) VALUES
(1, '알고리즘 스터디', '매주 백준 문제를 풀고 코드 리뷰를 진행하는 스터디입니다.', 5, 'ALGORITHM', 'RECRUITING', 0, NOW(), NOW()),
(2, '토익 900 스터디', '토익 900점 목표 리스닝/리딩 스터디입니다.', 4, 'ENGLISH', 'CLOSED', 0, NOW(), NOW()),
(3, '정보처리기사 스터디', '정보처리기사 실기 합격을 목표로 하는 스터디입니다.', 6, 'CERTIFICATE', 'RECRUITING', 0, NOW(), NOW()),
(4, '사이드 프로젝트 스터디', '토이 프로젝트를 기획부터 배포까지 진행했던 스터디입니다.', 5, 'ETC', 'ENDED', 0, NOW(), NOW());

-- 참여자 (리더/멤버)
INSERT INTO participants (id, study_id, member_id, role, created_at, updated_at) VALUES
(1,  1, 1,  'LEADER', NOW(), NOW()),
(2,  1, 2,  'MEMBER', NOW(), NOW()),
(3,  1, 3,  'MEMBER', NOW(), NOW()),
(4,  1, 6,  'MEMBER', NOW(), NOW()),
(5,  1, 7,  'MEMBER', NOW(), NOW()),
(6,  2, 5,  'LEADER', NOW(), NOW()),
(7,  2, 8,  'MEMBER', NOW(), NOW()),
(8,  2, 9,  'MEMBER', NOW(), NOW()),
(9,  2, 10, 'MEMBER', NOW(), NOW()),
(10, 3, 14, 'LEADER', NOW(), NOW()),
(11, 3, 11, 'MEMBER', NOW(), NOW()),
(12, 3, 12, 'MEMBER', NOW(), NOW()),
(13, 3, 13, 'MEMBER', NOW(), NOW()),
(14, 3, 15, 'MEMBER', NOW(), NOW()),
(15, 3, 16, 'MEMBER', NOW(), NOW()),
(16, 4, 19, 'LEADER', NOW(), NOW()),
(17, 4, 17, 'MEMBER', NOW(), NOW()),
(18, 4, 18, 'MEMBER', NOW(), NOW()),
(19, 4, 20, 'MEMBER', NOW(), NOW());

-- 가입 신청 (대기/승인/거절 상태 테스트용)
INSERT INTO applications (id, member_id, study_id, content, status, created_at, updated_at) VALUES
(1, 4, 1, '알고리즘 스터디 참여 희망합니다. 열심히 하겠습니다.', 'PENDING', NOW(), NOW()),
(2, 2, 1, '알고리즘 스터디 지원합니다.', 'APPROVED', NOW(), NOW()),
(3, 4, 2, '토익 스터디 참여 희망합니다.', 'REJECTED', NOW(), NOW()),
(4, 4, 3, '정보처리기사 스터디도 참여하고 싶습니다.', 'PENDING', NOW(), NOW()),
(5, 8, 2, '토익 스터디 지원합니다.', 'APPROVED', NOW(), NOW()),
(6, 4, 4, '사이드 프로젝트 스터디 참여 신청합니다.', 'REJECTED', NOW(), NOW());

-- 스터디 세션(회차) 8개
INSERT INTO study_sessions (id, study_id, session_number, title, content, starts_at, created_at, updated_at) VALUES
(1, 1, 1, '1주차 OT 및 스터디 규칙 안내', '스터디 진행 방식과 규칙을 안내합니다.', '2026-08-03 19:00:00', NOW(), NOW()),
(2, 1, 2, '2주차 그래프 탐색 문제풀이', 'BFS/DFS 관련 문제를 함께 풉니다.', '2026-08-10 19:00:00', NOW(), NOW()),
(3, 2, 1, '1주차 리스닝 파트 연습', 'LC Part1~2 집중 훈련.', '2026-08-04 20:00:00', NOW(), NOW()),
(4, 2, 2, '2주차 리딩 파트 연습', 'RC Part5~6 집중 훈련.', '2026-08-11 20:00:00', NOW(), NOW()),
(5, 3, 1, '1주차 정처기 실기 오리엔테이션', '실기 시험 범위와 학습 계획을 안내합니다.', '2026-08-05 19:30:00', NOW(), NOW()),
(6, 3, 2, '2주차 기출문제 풀이', '최근 3개년 기출문제를 함께 풉니다.', '2026-08-12 19:30:00', NOW(), NOW()),
(7, 4, 1, '1주차 프로젝트 기획 회의', '아이디어 브레인스토밍 및 역할 분담.', '2026-07-06 20:00:00', NOW(), NOW()),
(8, 4, 2, '2주차 MVP 데모 발표', '각자 만든 MVP를 시연하고 피드백을 나눕니다.', '2026-07-20 20:00:00', NOW(), NOW());

-- 과제 5개
INSERT INTO assignments (id, session_id, title, description, due_at, created_at, updated_at) VALUES
(1, 1, '백준 그래프 문제 3개 풀기', 'BFS, DFS, 다익스트라를 각각 활용하는 문제를 하나씩 풀어 제출하세요.', '2026-08-09 23:59:59', NOW(), NOW()),
(2, 2, '다익스트라 알고리즘 구현', '우선순위 큐를 이용해 다익스트라 알고리즘을 직접 구현하세요.', '2026-08-16 23:59:59', NOW(), NOW()),
(3, 5, '정처기 실기 기출 1회분 풀이', '가장 최근 회차 기출문제를 풀고 채점 결과를 정리해 제출하세요.', '2026-08-11 23:59:59', NOW(), NOW()),
(4, 7, '프로젝트 기획서 초안 작성', '팀 프로젝트 주제와 요구사항 정의서를 작성하세요.', '2026-07-13 23:59:59', NOW(), NOW()),
(5, 8, 'MVP 데모 시연 영상 제출', '완성된 MVP를 시연하는 5분 내외 영상을 제출하세요.', '2026-07-27 23:59:59', NOW(), NOW());

-- 출석 38건 (스터디별로 미래 세션은 UNMARKED, 이미 지난 세션은 PRESENT/ABSENT 위주로 구성)
INSERT INTO attendances (id, session_id, member_id, status, created_at, updated_at) VALUES
-- 알고리즘 스터디 1주차
(1, 1, 1, 'PRESENT', NOW(), NOW()),
(2, 1, 2, 'PRESENT', NOW(), NOW()),
(3, 1, 3, 'ABSENT', NOW(), NOW()),
(4, 1, 6, 'PRESENT', NOW(), NOW()),
(5, 1, 7, 'UNMARKED', NOW(), NOW()),
-- 알고리즘 스터디 2주차
(6, 2, 1, 'PRESENT', NOW(), NOW()),
(7, 2, 2, 'UNMARKED', NOW(), NOW()),
(8, 2, 3, 'UNMARKED', NOW(), NOW()),
(9, 2, 6, 'UNMARKED', NOW(), NOW()),
(10, 2, 7, 'UNMARKED', NOW(), NOW()),
-- 토익 900 스터디 1주차
(11, 3, 5, 'PRESENT', NOW(), NOW()),
(12, 3, 8, 'PRESENT', NOW(), NOW()),
(13, 3, 9, 'ABSENT', NOW(), NOW()),
(14, 3, 10, 'PRESENT', NOW(), NOW()),
-- 토익 900 스터디 2주차
(15, 4, 5, 'PRESENT', NOW(), NOW()),
(16, 4, 8, 'UNMARKED', NOW(), NOW()),
(17, 4, 9, 'UNMARKED', NOW(), NOW()),
(18, 4, 10, 'UNMARKED', NOW(), NOW()),
-- 정보처리기사 스터디 1주차
(19, 5, 14, 'PRESENT', NOW(), NOW()),
(20, 5, 11, 'PRESENT', NOW(), NOW()),
(21, 5, 12, 'PRESENT', NOW(), NOW()),
(22, 5, 13, 'ABSENT', NOW(), NOW()),
(23, 5, 15, 'PRESENT', NOW(), NOW()),
(24, 5, 16, 'UNMARKED', NOW(), NOW()),
-- 정보처리기사 스터디 2주차 (미래, 전원 미체크)
(25, 6, 14, 'UNMARKED', NOW(), NOW()),
(26, 6, 11, 'UNMARKED', NOW(), NOW()),
(27, 6, 12, 'UNMARKED', NOW(), NOW()),
(28, 6, 13, 'UNMARKED', NOW(), NOW()),
(29, 6, 15, 'UNMARKED', NOW(), NOW()),
(30, 6, 16, 'UNMARKED', NOW(), NOW()),
-- 사이드 프로젝트 스터디 1주차 (종료된 스터디, 출석 확정)
(31, 7, 19, 'PRESENT', NOW(), NOW()),
(32, 7, 17, 'PRESENT', NOW(), NOW()),
(33, 7, 18, 'ABSENT', NOW(), NOW()),
(34, 7, 20, 'PRESENT', NOW(), NOW()),
-- 사이드 프로젝트 스터디 2주차
(35, 8, 19, 'PRESENT', NOW(), NOW()),
(36, 8, 17, 'PRESENT', NOW(), NOW()),
(37, 8, 18, 'PRESENT', NOW(), NOW()),
(38, 8, 20, 'ABSENT', NOW(), NOW());

-- 과제 제출 8건 (일부 인원은 미제출로 남겨 둠)
INSERT INTO submissions (id, assignment_id, member_id, content, created_at, updated_at) VALUES
(1, 1, 2, '1) BFS로 최단거리 구현\n2) DFS로 연결요소 탐색\n3) 다익스트라로 최소비용 경로 계산', NOW(), NOW()),
(2, 1, 6, 'BFS/DFS/다익스트라 세 문제 모두 풀어서 제출합니다.', NOW(), NOW()),
(3, 2, 1, '우선순위 큐 기반 다익스트라 구현 완료했습니다.', NOW(), NOW()),
(4, 3, 14, '2024년 3회 기출 실기 풀이 정리해서 첨부합니다.', NOW(), NOW()),
(5, 3, 11, '기출문제 풀이 및 오답노트 제출합니다.', NOW(), NOW()),
(6, 4, 19, '프로젝트 기획서 초안 작성 완료했습니다. 검토 부탁드려요.', NOW(), NOW()),
(7, 5, 19, 'MVP 데모 영상 링크: (첨부 파일 참고)', NOW(), NOW()),
(8, 5, 17, '데모 영상 제출합니다. 피드백 부탁드립니다.', NOW(), NOW());

-- 다음 회원가입/신규 데이터가 기존 id와 충돌하지 않도록 AUTO_INCREMENT를 맞춰준다.
ALTER TABLE members AUTO_INCREMENT = 100;
ALTER TABLE studies AUTO_INCREMENT = 100;
ALTER TABLE participants AUTO_INCREMENT = 100;
ALTER TABLE applications AUTO_INCREMENT = 100;
ALTER TABLE study_sessions AUTO_INCREMENT = 100;
ALTER TABLE assignments AUTO_INCREMENT = 100;
ALTER TABLE attendances AUTO_INCREMENT = 100;
ALTER TABLE submissions AUTO_INCREMENT = 100;
