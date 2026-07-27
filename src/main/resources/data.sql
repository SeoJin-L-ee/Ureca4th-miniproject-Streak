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

-- 회원 20명 (비밀번호 원문: 회원1,2 : 1234qwer)
INSERT INTO members (id, email, password, name, phone, status, created_at, updated_at) VALUES
(1,'test1@gmail.com','$2a$10$N9KNK2LfyhMsb8x8odJfpeD/B4mLlOGn7YFh5RfVsJUvyJbigxqyS','회원01','010-1000-0001','ACTIVE',NOW(),NOW()),
(2,'test2@gmail.com','$2a$10$N9KNK2LfyhMsb8x8odJfpeD/B4mLlOGn7YFh5RfVsJUvyJbigxqyS','회원02','010-1000-0002','ACTIVE',NOW(),NOW()),
(3,'member3@test.com','$2a$10$dummyhash','회원03','010-1000-0003','ACTIVE',NOW(),NOW()),
(4,'member4@test.com','$2a$10$dummyhash','회원04','010-1000-0004','ACTIVE',NOW(),NOW()),
(5,'member5@test.com','$2a$10$dummyhash','회원05','010-1000-0005','ACTIVE',NOW(),NOW()),
(6,'member6@test.com','$2a$10$dummyhash','회원06','010-1000-0006','ACTIVE',NOW(),NOW()),
(7,'member7@test.com','$2a$10$dummyhash','회원07','010-1000-0007','ACTIVE',NOW(),NOW()),
(8,'member8@test.com','$2a$10$dummyhash','회원08','010-1000-0008','ACTIVE',NOW(),NOW()),
(9,'member9@test.com','$2a$10$dummyhash','회원09','010-1000-0009','ACTIVE',NOW(),NOW()),
(10,'member10@test.com','$2a$10$dummyhash','회원10','010-1000-0010','ACTIVE',NOW(),NOW()),
(11,'member11@test.com','$2a$10$dummyhash','회원11','010-1000-0011','ACTIVE',NOW(),NOW()),
(12,'member12@test.com','$2a$10$dummyhash','회원12','010-1000-0012','ACTIVE',NOW(),NOW()),
(13,'member13@test.com','$2a$10$dummyhash','회원13','010-1000-0013','ACTIVE',NOW(),NOW()),
(14,'member14@test.com','$2a$10$dummyhash','회원14','010-1000-0014','ACTIVE',NOW(),NOW()),
(15,'member15@test.com','$2a$10$dummyhash','회원15','010-1000-0015','ACTIVE',NOW(),NOW()),
(16,'member16@test.com','$2a$10$dummyhash','회원16','010-1000-0016','ACTIVE',NOW(),NOW()),
(17,'member17@test.com','$2a$10$dummyhash','회원17','010-1000-0017','ACTIVE',NOW(),NOW()),
(18,'member18@test.com','$2a$10$dummyhash','회원18','010-1000-0018','ACTIVE',NOW(),NOW()),
(19,'member19@test.com','$2a$10$dummyhash','회원19','010-1000-0019','DISABLED',NOW(),NOW()),
(20,'member20@test.com','$2a$10$dummyhash','회원20','010-1000-0020','ACTIVE',NOW(),NOW());

-- 스터디 4개
INSERT INTO studies (id, title, description, capacity, category, status, is_deleted, created_at, updated_at) VALUES
(1,'테스트 스터디 1','스터디 설명 1',6,'ALGORITHM','RECRUITING',false,NOW(),NOW()),
(2,'테스트 스터디 2','스터디 설명 2',7,'ENGLISH','CLOSED',false,NOW(),NOW()),
(3,'테스트 스터디 3','스터디 설명 3',8,'CERTIFICATE','RECRUITING',false,NOW(),NOW()),
(4,'테스트 스터디 4','스터디 설명 4',9,'ETC','RECRUITING',false,NOW(),NOW()),
(5,'테스트 스터디 5','스터디 설명 5',10,'ALGORITHM','CLOSED',false,NOW(),NOW()),
(6,'테스트 스터디 6','스터디 설명 6',5,'ENGLISH','RECRUITING',false,NOW(),NOW()),
(7,'테스트 스터디 7','스터디 설명 7',6,'CERTIFICATE','RECRUITING',false,NOW(),NOW()),
(8,'테스트 스터디 8','스터디 설명 8',7,'ETC','ENDED',false,NOW(),NOW()),
(9,'테스트 스터디 9','스터디 설명 9',8,'ALGORITHM','RECRUITING',false,NOW(),NOW()),
(10,'테스트 스터디 10','스터디 설명 10',9,'ENGLISH','CLOSED',false,NOW(),NOW()),
(11,'테스트 스터디 11','스터디 설명 11',10,'CERTIFICATE','RECRUITING',false,NOW(),NOW()),
(12,'테스트 스터디 12','스터디 설명 12',5,'ETC','RECRUITING',false,NOW(),NOW()),
(13,'테스트 스터디 13','스터디 설명 13',6,'ALGORITHM','CLOSED',false,NOW(),NOW()),
(14,'테스트 스터디 14','스터디 설명 14',7,'ENGLISH','RECRUITING',false,NOW(),NOW()),
(15,'테스트 스터디 15','스터디 설명 15',8,'CERTIFICATE','RECRUITING',false,NOW(),NOW()),
(16,'테스트 스터디 16','스터디 설명 16',9,'ETC','ENDED',false,NOW(),NOW()),
(17,'테스트 스터디 17','스터디 설명 17',10,'ALGORITHM','RECRUITING',false,NOW(),NOW()),
(18,'테스트 스터디 18','스터디 설명 18',5,'ENGLISH','CLOSED',false,NOW(),NOW()),
(19,'테스트 스터디 19','스터디 설명 19',6,'CERTIFICATE','RECRUITING',true,NOW(),NOW()),
(20,'테스트 스터디 20','스터디 설명 20',7,'ETC','RECRUITING',false,NOW(),NOW());

-- 참여자 (리더/멤버)
INSERT INTO participants (id, study_id, member_id, role, created_at, updated_at) VALUES
(1,1,1,'LEADER',NOW(),NOW()),
(2,1,2,'MEMBER',NOW(),NOW()),
(3,1,3,'MEMBER',NOW(),NOW()),
(4,1,4,'MEMBER',NOW(),NOW()),
(5,1,5,'MEMBER',NOW(),NOW()),
(6,1,6,'MEMBER',NOW(),NOW()),
(7,1,7,'MEMBER',NOW(),NOW()),
(8,1,8,'MEMBER',NOW(),NOW()),
(9,2,2,'LEADER',NOW(),NOW()),
(10,2,1,'MEMBER',NOW(),NOW()),
(11,2,9,'MEMBER',NOW(),NOW()),
(12,2,10,'MEMBER',NOW(),NOW()),
(13,3,3,'LEADER',NOW(),NOW()),
(14,3,11,'MEMBER',NOW(),NOW()),
(15,3,12,'MEMBER',NOW(),NOW()),
(16,4,4,'LEADER',NOW(),NOW()),
(17,4,13,'MEMBER',NOW(),NOW()),
(18,5,5,'LEADER',NOW(),NOW()),
(19,5,14,'MEMBER',NOW(),NOW()),
(20,19,1,'LEADER',NOW(),NOW());

-- 가입 신청 (대기/승인/거절 상태 테스트용)
INSERT INTO applications (id, member_id, study_id, content, status, created_at, updated_at) VALUES
(1,9,1,'지원 동기 1','PENDING',NOW(),NOW()),
(2,10,2,'지원 동기 2','APPROVED',NOW(),NOW()),
(3,11,3,'지원 동기 3','REJECTED',NOW(),NOW()),
(4,12,4,'지원 동기 4','PENDING',NOW(),NOW()),
(5,13,5,'지원 동기 5','APPROVED',NOW(),NOW()),
(6,14,6,'지원 동기 6','REJECTED',NOW(),NOW()),
(7,15,7,'지원 동기 7','PENDING',NOW(),NOW()),
(8,16,8,'지원 동기 8','APPROVED',NOW(),NOW()),
(9,17,9,'지원 동기 9','REJECTED',NOW(),NOW()),
(10,18,10,'지원 동기 10','PENDING',NOW(),NOW()),
(11,19,1,'지원 동기 11','APPROVED',NOW(),NOW()),
(12,20,2,'지원 동기 12','REJECTED',NOW(),NOW()),
(13,1,3,'지원 동기 13','PENDING',NOW(),NOW()),
(14,2,4,'지원 동기 14','APPROVED',NOW(),NOW()),
(15,3,5,'지원 동기 15','REJECTED',NOW(),NOW()),
(16,4,6,'지원 동기 16','PENDING',NOW(),NOW()),
(17,5,7,'지원 동기 17','APPROVED',NOW(),NOW()),
(18,6,8,'지원 동기 18','REJECTED',NOW(),NOW()),
(19,7,9,'지원 동기 19','PENDING',NOW(),NOW()),
(20,8,10,'지원 동기 20','APPROVED',NOW(),NOW());

-- 스터디 세션(회차) 8개
INSERT INTO study_sessions (id, study_id, session_number, title, content, starts_at, created_at, updated_at) VALUES
(1,1,1,'1회차 제목','진행 내용 1','2026-06-01 19:00:00',NOW(),NOW()),
(2,1,2,'2회차 제목','진행 내용 2','2026-06-02 19:00:00',NOW(),NOW()),
(3,1,3,'3회차 제목','진행 내용 3','2026-06-03 19:00:00',NOW(),NOW()),
(4,1,4,'4회차 제목','진행 내용 4','2026-07-28 19:00:00',NOW(),NOW()),
(5,2,1,'1회차 제목','진행 내용 5','2026-06-05 19:00:00',NOW(),NOW()),
(6,2,2,'2회차 제목','진행 내용 6','2026-06-06 19:00:00',NOW(),NOW()),
(7,2,3,'3회차 제목','진행 내용 7','2026-06-07 19:00:00',NOW(),NOW()),
(8,2,4,'4회차 제목','진행 내용 8','2026-06-08 19:00:00',NOW(),NOW()),
(9,3,1,'1회차 제목','진행 내용 9','2026-06-09 19:00:00',NOW(),NOW()),
(10,3,2,'2회차 제목','진행 내용 10','2026-06-10 19:00:00',NOW(),NOW()),
(11,3,3,'3회차 제목','진행 내용 11','2026-06-11 19:00:00',NOW(),NOW()),
(12,3,4,'4회차 제목','진행 내용 12','2026-06-12 19:00:00',NOW(),NOW()),
(13,4,1,'1회차 제목','진행 내용 13','2026-08-13 19:00:00',NOW(),NOW()),
(14,4,2,'2회차 제목','진행 내용 14','2026-08-14 19:00:00',NOW(),NOW()),
(15,4,3,'3회차 제목','진행 내용 15','2026-08-15 19:00:00',NOW(),NOW()),
(16,4,4,'4회차 제목','진행 내용 16','2026-08-16 19:00:00',NOW(),NOW()),
(17,5,1,'1회차 제목','진행 내용 17','2026-08-17 19:00:00',NOW(),NOW()),
(18,5,2,'2회차 제목','진행 내용 18','2026-08-18 19:00:00',NOW(),NOW()),
(19,5,3,'3회차 제목','진행 내용 19','2026-08-19 19:00:00',NOW(),NOW()),
(20,5,4,'4회차 제목','진행 내용 20','2026-08-20 19:00:00',NOW(),NOW());

-- 과제 5개
INSERT INTO assignments (id, session_id, title, description, due_at, created_at, updated_at) VALUES
(1,1,'과제 1','과제 설명 1','2026-06-03 23:59:59',NOW(),NOW()),
(2,2,'과제 2','과제 설명 2','2026-06-04 23:59:59',NOW(),NOW()),
(3,3,'과제 3','과제 설명 3','2026-06-05 23:59:59',NOW(),NOW()),
(4,4,'과제 4','과제 설명 4','2026-06-06 23:59:59',NOW(),NOW()),
(5,5,'과제 5','과제 설명 5','2026-06-07 23:59:59',NOW(),NOW()),
(6,6,'과제 6','과제 설명 6','2026-06-08 23:59:59',NOW(),NOW()),
(7,7,'과제 7','과제 설명 7','2026-06-09 23:59:59',NOW(),NOW()),
(8,8,'과제 8','과제 설명 8','2026-06-10 23:59:59',NOW(),NOW()),
(9,9,'과제 9','과제 설명 9','2026-06-11 23:59:59',NOW(),NOW()),
(10,10,'과제 10','과제 설명 10','2026-06-12 23:59:59',NOW(),NOW()),
(11,11,'과제 11','과제 설명 11','2026-06-13 23:59:59',NOW(),NOW()),
(12,12,'과제 12','과제 설명 12','2026-06-14 23:59:59',NOW(),NOW()),
(13,13,'과제 13','과제 설명 13','2026-08-15 23:59:59',NOW(),NOW()),
(14,14,'과제 14','과제 설명 14','2026-08-16 23:59:59',NOW(),NOW()),
(15,15,'과제 15','과제 설명 15','2026-08-17 23:59:59',NOW(),NOW()),
(16,16,'과제 16','과제 설명 16','2026-08-18 23:59:59',NOW(),NOW()),
(17,17,'과제 17','과제 설명 17','2026-08-19 23:59:59',NOW(),NOW()),
(18,18,'과제 18','과제 설명 18','2026-08-20 23:59:59',NOW(),NOW()),
(19,19,'과제 19','과제 설명 19','2026-08-21 23:59:59',NOW(),NOW()),
(20,20,'과제 20','과제 설명 20','2026-08-22 23:59:59',NOW(),NOW());

-- 출석 38건 (스터디별로 미래 세션은 UNMARKED, 이미 지난 세션은 PRESENT/ABSENT 위주로 구성)
INSERT INTO attendances (id, session_id, member_id, status, created_at, updated_at) VALUES
(1,1,1,'PRESENT',NOW(),NOW()),
(2,1,2,'PRESENT',NOW(),NOW()),
(3,1,3,'PRESENT',NOW(),NOW()),
(4,1,4,'ABSENT',NOW(),NOW()),
(5,1,5,'UNMARKED',NOW(),NOW()),
(6,1,6,'PRESENT',NOW(),NOW()),
(7,1,7,'PRESENT',NOW(),NOW()),
(8,1,8,'PRESENT',NOW(),NOW()),
(9,2,1,'ABSENT',NOW(),NOW()),
(10,2,2,'UNMARKED',NOW(),NOW()),
(11,2,3,'PRESENT',NOW(),NOW()),
(12,2,4,'PRESENT',NOW(),NOW()),
(13,2,5,'PRESENT',NOW(),NOW()),
(14,2,6,'ABSENT',NOW(),NOW()),
(15,2,7,'UNMARKED',NOW(),NOW()),
(16,2,8,'PRESENT',NOW(),NOW()),
(17,3,1,'PRESENT',NOW(),NOW()),
(18,3,2,'PRESENT',NOW(),NOW()),
(19,3,3,'ABSENT',NOW(),NOW()),
(20,3,4,'UNMARKED',NOW(),NOW()),
(21,3,5,'PRESENT',NOW(),NOW()),
(22,3,6,'PRESENT',NOW(),NOW()),
(23,3,7,'PRESENT',NOW(),NOW()),
(24,3,8,'ABSENT',NOW(),NOW());

-- 과제 제출 8건 (일부 인원은 미제출로 남겨 둠)
INSERT INTO submissions (id, assignment_id, member_id, content, created_at, updated_at) VALUES
(1,1,1,'제출 내용 1',NOW(),NOW()),
(2,2,2,'제출 내용 2',NOW(),NOW()),
(3,3,3,'제출 내용 3',NOW(),NOW()),
(4,4,4,'제출 내용 4',NOW(),NOW()),
(5,5,5,'제출 내용 5',NOW(),NOW()),
(6,6,6,'제출 내용 6',NOW(),NOW()),
(7,7,7,'제출 내용 7',NOW(),NOW()),
(8,8,8,'제출 내용 8',NOW(),NOW()),
(9,9,1,'제출 내용 9',NOW(),NOW()),
(10,10,2,'제출 내용 10',NOW(),NOW()),
(11,1,3,'제출 내용 11',NOW(),NOW()),
(12,2,4,'제출 내용 12',NOW(),NOW()),
(13,3,5,'제출 내용 13',NOW(),NOW()),
(14,4,6,'제출 내용 14',NOW(),NOW()),
(15,5,7,'제출 내용 15',NOW(),NOW()),
(16,6,8,'제출 내용 16',NOW(),NOW()),
(17,7,1,'제출 내용 17',NOW(),NOW()),
(18,8,2,'제출 내용 18',NOW(),NOW()),
(19,9,3,'제출 내용 19',NOW(),NOW()),
(20,10,4,'제출 내용 20',NOW(),NOW());

-- 다음 회원가입/신규 데이터가 기존 id와 충돌하지 않도록 AUTO_INCREMENT를 맞춰준다.
ALTER TABLE members AUTO_INCREMENT = 100;
ALTER TABLE studies AUTO_INCREMENT = 100;
ALTER TABLE participants AUTO_INCREMENT = 100;
ALTER TABLE applications AUTO_INCREMENT = 100;
ALTER TABLE study_sessions AUTO_INCREMENT = 100;
ALTER TABLE assignments AUTO_INCREMENT = 100;
ALTER TABLE attendances AUTO_INCREMENT = 100;
ALTER TABLE submissions AUTO_INCREMENT = 100;
