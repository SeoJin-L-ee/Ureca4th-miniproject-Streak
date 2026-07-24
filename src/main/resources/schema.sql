-- Streak 프로젝트 DB 스키마
-- MySQL 8.x 기준. JPA 엔티티(src/main/java/com/example/**/entity) 구조와 동일하게 작성됨.
--
-- 사용법
--   1) CREATE DATABASE streak CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
--   2) CREATE USER 'ureca'@'%' IDENTIFIED BY 'ureca';
--      GRANT ALL PRIVILEGES ON streak.* TO 'ureca'@'%';
--      FLUSH PRIVILEGES;
--   3) 앱 실행 (./gradlew bootRun 등)
--
-- application.properties에 spring.sql.init.mode=always,
-- spring.jpa.defer-datasource-initialization=true가 설정되어 있어
-- 앱을 실행할 때마다 이 schema.sql과 data.sql이 자동으로 실행된다.
-- 즉 재시작할 때마다 테이블이 초기화되고 더미 데이터로 다시 채워진다.
-- (테스트 중 직접 추가/수정한 데이터는 재시작 시 사라지니 주의)

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS submissions;
DROP TABLE IF EXISTS attendances;
DROP TABLE IF EXISTS assignments;
DROP TABLE IF EXISTS study_sessions;
DROP TABLE IF EXISTS applications;
DROP TABLE IF EXISTS participants;
DROP TABLE IF EXISTS studies;
DROP TABLE IF EXISTS members;

SET FOREIGN_KEY_CHECKS = 1;

-- 회원
CREATE TABLE members (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    email       VARCHAR(255) NOT NULL,
    password    VARCHAR(255) NOT NULL,
    name        VARCHAR(50)  NOT NULL,
    phone       VARCHAR(30),
    status      VARCHAR(20)  NOT NULL, -- ACTIVE, DISABLED
    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)  NOT NULL,
    CONSTRAINT uk_members_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 스터디
CREATE TABLE studies (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(100) NOT NULL,
    description TEXT         NOT NULL,
    capacity    INT          NOT NULL,
    category    VARCHAR(30)  NOT NULL, -- ALGORITHM, ENGLISH, CERTIFICATE, ETC
    status      VARCHAR(30)  NOT NULL, -- RECRUITING, CLOSED, ENDED
    is_deleted  TINYINT(1)   NOT NULL DEFAULT 0,
    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)  NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 스터디 참여자 (스터디 - 회원 매핑, 리더/멤버 역할)
CREATE TABLE participants (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    study_id    BIGINT      NOT NULL,
    member_id   BIGINT      NOT NULL,
    role        VARCHAR(20) NOT NULL, -- LEADER, MEMBER
    created_at  DATETIME(6) NOT NULL,
    updated_at  DATETIME(6) NOT NULL,
    CONSTRAINT uk_participant_study_member UNIQUE (study_id, member_id),
    CONSTRAINT fk_participants_study  FOREIGN KEY (study_id)  REFERENCES studies(id),
    CONSTRAINT fk_participants_member FOREIGN KEY (member_id) REFERENCES members(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 스터디 가입 신청
CREATE TABLE applications (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id   BIGINT      NOT NULL, -- 신청자
    study_id    BIGINT      NOT NULL,
    content     TEXT        NOT NULL,
    status      VARCHAR(20) NOT NULL, -- PENDING, APPROVED, REJECTED
    created_at  DATETIME(6) NOT NULL,
    updated_at  DATETIME(6) NOT NULL,
    CONSTRAINT uk_application_member_study UNIQUE (member_id, study_id),
    CONSTRAINT fk_applications_member FOREIGN KEY (member_id) REFERENCES members(id),
    CONSTRAINT fk_applications_study  FOREIGN KEY (study_id)  REFERENCES studies(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 스터디 세션(회차)
CREATE TABLE study_sessions (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    study_id       BIGINT       NOT NULL,
    session_number INT          NOT NULL,
    title          VARCHAR(150) NOT NULL,
    content        TEXT,
    starts_at      DATETIME(6)  NOT NULL,
    created_at     DATETIME(6)  NOT NULL,
    updated_at     DATETIME(6)  NOT NULL,
    CONSTRAINT uk_session_study_number UNIQUE (study_id, session_number),
    CONSTRAINT fk_sessions_study FOREIGN KEY (study_id) REFERENCES studies(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 과제
CREATE TABLE assignments (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id  BIGINT       NOT NULL,
    title       VARCHAR(150) NOT NULL,
    description TEXT         NOT NULL,
    due_at      DATETIME(6)  NOT NULL,
    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)  NOT NULL,
    CONSTRAINT fk_assignments_session FOREIGN KEY (session_id) REFERENCES study_sessions(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 출석
CREATE TABLE attendances (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id  BIGINT      NOT NULL,
    member_id   BIGINT      NOT NULL,
    status      VARCHAR(20) NOT NULL, -- UNMARKED, PRESENT, ABSENT
    created_at  DATETIME(6) NOT NULL,
    updated_at  DATETIME(6) NOT NULL,
    CONSTRAINT uk_attendance_session_member UNIQUE (session_id, member_id),
    CONSTRAINT fk_attendances_session FOREIGN KEY (session_id) REFERENCES study_sessions(id),
    CONSTRAINT fk_attendances_member  FOREIGN KEY (member_id)  REFERENCES members(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 과제 제출
CREATE TABLE submissions (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    assignment_id BIGINT      NOT NULL,
    member_id     BIGINT      NOT NULL,
    content       TEXT,
    created_at    DATETIME(6) NOT NULL,
    updated_at    DATETIME(6) NOT NULL,
    CONSTRAINT uk_submission_assignment_member UNIQUE (assignment_id, member_id),
    CONSTRAINT fk_submissions_assignment FOREIGN KEY (assignment_id) REFERENCES assignments(id),
    CONSTRAINT fk_submissions_member     FOREIGN KEY (member_id)     REFERENCES members(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
