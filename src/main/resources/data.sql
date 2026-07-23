-- Streak 프로젝트 더미(테스트) 데이터
-- application.properties의 spring.sql.init.mode=always 설정에 의해
-- 앱 실행 시 schema.sql 다음에 자동으로 실행된다. (수동 실행: mysql -u ureca -p streak < src/main/resources/data.sql)
--
-- 모든 계정의 비밀번호는 "password123!" 이다 (BCrypt로 해싱된 값이 저장되어 있어
-- 로그인 API(/api/auth/login)로 바로 로그인 테스트가 가능하다).
--
-- 시나리오
--   - leader1@test.com : "알고리즘 스터디"(RECRUITING) 리더. 세션 2회, 과제 2개, 출석 기록 보유
--   - member1@test.com  : 알고리즘 스터디 멤버. 과제 1개 제출 완료
--   - member2@test.com  : 알고리즘 스터디 멤버. 출석/과제 미제출 상태 테스트용
--   - member3@test.com  : 아직 스터디에 참여하지 않은 회원. 가입 신청(PENDING/REJECTED) 테스트용
--   - leader2@test.com  : "토익 900 스터디"(CLOSED) 리더

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

-- 회원 (비밀번호 원문: password123!)
INSERT INTO members (id, email, password, name, phone, status, created_at, updated_at) VALUES
(1, 'leader1@test.com', '$2a$10$yJnM9UjBKamN5JtgJnUjR.E6fGp95V4wz835T1CVODcqTrqYk4aga', '김리더', '010-1111-1111', 'ACTIVE', NOW(), NOW()),
(2, 'member1@test.com', '$2a$10$yJnM9UjBKamN5JtgJnUjR.E6fGp95V4wz835T1CVODcqTrqYk4aga', '이멤버', '010-2222-2222', 'ACTIVE', NOW(), NOW()),
(3, 'member2@test.com', '$2a$10$yJnM9UjBKamN5JtgJnUjR.E6fGp95V4wz835T1CVODcqTrqYk4aga', '박멤버', '010-3333-3333', 'ACTIVE', NOW(), NOW()),
(4, 'member3@test.com', '$2a$10$yJnM9UjBKamN5JtgJnUjR.E6fGp95V4wz835T1CVODcqTrqYk4aga', '최지원', '010-4444-4444', 'ACTIVE', NOW(), NOW()),
(5, 'leader2@test.com', '$2a$10$yJnM9UjBKamN5JtgJnUjR.E6fGp95V4wz835T1CVODcqTrqYk4aga', '정리더', '010-5555-5555', 'ACTIVE', NOW(), NOW());

-- 스터디
INSERT INTO studies (id, title, description, capacity, category, status, is_deleted, created_at, updated_at) VALUES
(1, '알고리즘 스터디', '매주 백준 문제를 풀고 코드 리뷰를 진행하는 스터디입니다.', 5, 'ALGORITHM', 'RECRUITING', 0, NOW(), NOW()),
(2, '토익 900 스터디', '토익 900점 목표 리스닝/리딩 스터디입니다.', 4, 'ENGLISH', 'CLOSED', 0, NOW(), NOW());

-- 참여자 (리더/멤버)
INSERT INTO participants (id, study_id, member_id, role, created_at, updated_at) VALUES
(1, 1, 1, 'LEADER', NOW(), NOW()),
(2, 1, 2, 'MEMBER', NOW(), NOW()),
(3, 1, 3, 'MEMBER', NOW(), NOW()),
(4, 2, 5, 'LEADER', NOW(), NOW());

-- 가입 신청 (대기/승인/거절 각 상태 테스트용)
INSERT INTO applications (id, member_id, study_id, content, status, created_at, updated_at) VALUES
(1, 4, 1, '알고리즘 스터디 참여 희망합니다. 열심히 하겠습니다.', 'PENDING', NOW(), NOW()),
(2, 2, 1, '알고리즘 스터디 지원합니다.', 'APPROVED', NOW(), NOW()),
(3, 4, 2, '토익 스터디 참여 희망합니다.', 'REJECTED', NOW(), NOW());

-- 스터디 세션(회차)
INSERT INTO study_sessions (id, study_id, session_number, title, content, starts_at, created_at, updated_at) VALUES
(1, 1, 1, '1주차 OT 및 스터디 규칙 안내', '스터디 진행 방식과 규칙을 안내합니다.', '2026-08-03 19:00:00', NOW(), NOW()),
(2, 1, 2, '2주차 그래프 탐색 문제풀이', 'BFS/DFS 관련 문제를 함께 풉니다.', '2026-08-10 19:00:00', NOW(), NOW()),
(3, 2, 1, '1주차 리스닝 파트 연습', 'LC Part1~2 집중 훈련.', '2026-08-04 20:00:00', NOW(), NOW());

-- 과제
INSERT INTO assignments (id, session_id, title, description, due_at, created_at, updated_at) VALUES
(1, 1, '백준 그래프 문제 3개 풀기', 'BFS, DFS, 다익스트라를 각각 활용하는 문제를 하나씩 풀어 제출하세요.', '2026-08-09 23:59:59', NOW(), NOW()),
(2, 2, '다익스트라 알고리즘 구현', '우선순위 큐를 이용해 다익스트라 알고리즘을 직접 구현하세요.', '2026-08-16 23:59:59', NOW(), NOW());

-- 출석 (일부는 UNMARKED로 남겨 관리자가 체크하는 흐름을 테스트할 수 있게 함)
INSERT INTO attendances (id, session_id, member_id, status, created_at, updated_at) VALUES
(1, 1, 1, 'PRESENT', NOW(), NOW()),
(2, 1, 2, 'PRESENT', NOW(), NOW()),
(3, 1, 3, 'ABSENT', NOW(), NOW()),
(4, 2, 1, 'PRESENT', NOW(), NOW()),
(5, 2, 2, 'UNMARKED', NOW(), NOW()),
(6, 2, 3, 'UNMARKED', NOW(), NOW());

-- 과제 제출 (member2/박멤버는 미제출 상태로 남겨 둠)
INSERT INTO submissions (id, assignment_id, member_id, content, created_at, updated_at) VALUES
(1, 1, 2, '1) BFS로 최단거리 구현\n2) DFS로 연결요소 탐색\n3) 다익스트라로 최소비용 경로 계산', NOW(), NOW()),
(2, 2, 1, '우선순위 큐 기반 다익스트라 구현 완료했습니다.', NOW(), NOW());

-- 다음 회원가입/신규 데이터가 기존 id와 충돌하지 않도록 AUTO_INCREMENT를 맞춰준다.
ALTER TABLE members AUTO_INCREMENT = 100;
ALTER TABLE studies AUTO_INCREMENT = 100;
ALTER TABLE participants AUTO_INCREMENT = 100;
ALTER TABLE applications AUTO_INCREMENT = 100;
ALTER TABLE study_sessions AUTO_INCREMENT = 100;
ALTER TABLE assignments AUTO_INCREMENT = 100;
ALTER TABLE attendances AUTO_INCREMENT = 100;
ALTER TABLE submissions AUTO_INCREMENT = 100;
