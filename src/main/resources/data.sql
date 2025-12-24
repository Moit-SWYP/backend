-- 0) DB / 인코딩 / FK 설정
CREATE DATABASE IF NOT EXISTS swyp
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;

USE swyp;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

------------------------------------------------------
-- 1) TABLE DDL (IF NOT EXISTS)
------------------------------------------------------

-- CREATE TABLE IF NOT EXISTS meeting
-- (
--     id         BIGINT AUTO_INCREMENT PRIMARY KEY,
--     created_at DATETIME(6)                                 NOT NULL,
--     is_active  BIT                                         NOT NULL,
--     updated_at DATETIME(6)                                 NULL,
--     date       DATETIME(6)                                 NULL,
--     status     ENUM ('CREATED', 'DONE', 'FIXED', 'VOTING') NOT NULL,
--     title      VARCHAR(255)                                NOT NULL
--     );
--
-- CREATE TABLE IF NOT EXISTS course
-- (
--     id         BIGINT AUTO_INCREMENT PRIMARY KEY,
--     created_at DATETIME(6) NOT NULL,
--     is_active  BIT         NOT NULL,
--     updated_at DATETIME(6) NULL,
--     step       INT         NOT NULL,
--     meeting_id BIGINT      NOT NULL,
--     CONSTRAINT fk_course_meeting
--     FOREIGN KEY (meeting_id) REFERENCES meeting (id)
--     );
--
-- CREATE TABLE IF NOT EXISTS date_option
-- (
--     id             BIGINT AUTO_INCREMENT PRIMARY KEY,
--     created_at     DATETIME(6) NOT NULL,
--     is_active      BIT         NOT NULL,
--     updated_at     DATETIME(6) NULL,
--     candidate_date DATETIME(6) NOT NULL,
--     meeting_id     BIGINT      NOT NULL,
--     CONSTRAINT fk_date_option_meeting
--     FOREIGN KEY (meeting_id) REFERENCES meeting (id)
--     );
--
-- CREATE TABLE IF NOT EXISTS member
-- (
--     id              BIGINT AUTO_INCREMENT PRIMARY KEY,
--     created_at      DATETIME(6)                                NOT NULL,
--     is_active       BIT                                        NOT NULL,
--     updated_at      DATETIME(6)                                NULL,
--     birth_date      DATE                                       NOT NULL,
--     email           VARCHAR(100)                               NOT NULL,
--     gender          ENUM ('FEMALE', 'MALE')                    NOT NULL,
--     nickname        VARCHAR(100)                               NOT NULL,
--     social_provider ENUM ('APPLE', 'GOOGLE', 'KAKAO', 'NAVER') NOT NULL,
--     social_id       VARCHAR(255)                               NOT NULL
--     );
--
-- CREATE TABLE IF NOT EXISTS meeting_participant
-- (
--     id         BIGINT AUTO_INCREMENT PRIMARY KEY,
--     created_at DATETIME(6)             NOT NULL,
--     is_active  BIT                     NOT NULL,
--     updated_at DATETIME(6)             NULL,
--     participantRole       ENUM ('HOST', 'MEMBER') NOT NULL,
--     meeting_id BIGINT                  NOT NULL,
--     member_id  BIGINT                  NOT NULL,
--     CONSTRAINT fk_meeting_participant_member
--     FOREIGN KEY (member_id) REFERENCES member (id),
--     CONSTRAINT fk_meeting_participant_meeting
--     FOREIGN KEY (meeting_id) REFERENCES meeting (id)
--     );
--
-- CREATE TABLE IF NOT EXISTS date_vote
-- (
--     id                     BIGINT AUTO_INCREMENT PRIMARY KEY,
--     created_at             DATETIME(6) NOT NULL,
--     is_active              BIT         NOT NULL,
--     updated_at             DATETIME(6) NULL,
--     date_option_id         BIGINT      NOT NULL,
--     meeting_participant_id BIGINT      NOT NULL,
--     CONSTRAINT fk_date_vote_date_option
--     FOREIGN KEY (date_option_id) REFERENCES date_option (id),
--     CONSTRAINT fk_date_vote_meeting_participant
--     FOREIGN KEY (meeting_participant_id) REFERENCES meeting_participant (id)
--     );
--
-- CREATE TABLE IF NOT EXISTS place_option
-- (
--     id         BIGINT AUTO_INCREMENT PRIMARY KEY,
--     created_at DATETIME(6)                      NOT NULL,
--     is_active  BIT                              NOT NULL,
--     updated_at DATETIME(6)                      NULL,
--     status     ENUM ('DONE', 'FIXED', 'OPTION') NOT NULL,
--     course_id  BIGINT                           NOT NULL,
--     CONSTRAINT fk_place_option_course
--     FOREIGN KEY (course_id) REFERENCES course (id)
--     );
--
-- CREATE TABLE IF NOT EXISTS place_vote
-- (
--     id                     BIGINT AUTO_INCREMENT PRIMARY KEY,
--     created_at             DATETIME(6) NOT NULL,
--     is_active              BIT         NOT NULL,
--     updated_at             DATETIME(6) NULL,
--     course_id              BIGINT      NOT NULL,
--     meeting_participant_id BIGINT      NOT NULL,
--     place_option_id        BIGINT      NOT NULL,
--     CONSTRAINT fk_place_vote_course
--     FOREIGN KEY (course_id) REFERENCES course (id),
--     CONSTRAINT fk_place_vote_place_option
--     FOREIGN KEY (place_option_id) REFERENCES place_option (id),
--     CONSTRAINT fk_place_vote_meeting_participant
--     FOREIGN KEY (meeting_participant_id) REFERENCES meeting_participant (id)
--     );

------------------------------------------------------
-- 2) SEED DATA (INSERT IGNORE 로 여러 번 실행해도 안전하게)
------------------------------------------------------

-- MEETING
INSERT IGNORE INTO meeting (id, created_at, is_active, updated_at, date, time, status, title)
VALUES
    (1, NOW(), 1, NULL, '2025-12-20', NULL, 'CREATED', '연말 모임'),
    (2, NOW(), 1, NULL, '2025-12-25', '19:00:00', 'PLACE_VOTING', '크리스마스 모임');

-- MEMBER
INSERT IGNORE INTO member (id, created_at, birth_date, email, gender, nickname, character_type)
VALUES
    (1, NOW(), '1999-01-01', 'host@example.com',   'MALE',   '호스트유저', 'FOODIE'),
    (2, NOW(), '1998-05-10', 'member1@example.com', 'FEMALE', '참여자1', 'DRINKER'),
    (3, NOW(), '1995-02-14', 'member2@example.com', 'MALE',   '참여자2', 'HEALER'),
    (4, NOW(), '1995-02-14', 'existing@example.com', 'MALE',   '기존 회원', 'TRAVELER');

-- SOCIAL_ACCOUNT
INSERT IGNORE INTO social_account (id, created_at, social_provider, social_id, member_id)
VALUES
    (1, NOW(), 'KAKAO', 'existing-social-id-123', 4);

-- MEETING_PARTICIPANT
INSERT IGNORE INTO meeting_participant (id, created_at, is_active, updated_at, participant_role, meeting_id, member_id)
VALUES
    (1, NOW(), 1, NULL, 'HOST',   1, 1),
    (2, NOW(), 1, NULL, 'MEMBER', 1, 2),
    (3, NOW(), 1, NULL, 'MEMBER', 1, 3),
    (4, NOW(), 1, NULL, 'HOST',   2, 2),
    (5, NOW(), 1, NULL, 'MEMBER', 2, 4);

-- COURSE
INSERT IGNORE INTO course (id, created_at, is_active, updated_at, step, meeting_id)
VALUES
    (1, NOW(), 1, NULL, 1, 1),
    (2, NOW(), 1, NULL, 2, 1),
    (3, NOW(), 1, NULL, 1, 2);

-- DATE_OPTION
INSERT IGNORE INTO date_option (id, created_at, is_active, updated_at, candidate_date, meeting_id)
VALUES
    (1, NOW(), 1, NULL, '2025-12-20 18:00:00', 1),
    (2, NOW(), 1, NULL, '2025-12-21 18:00:00', 1),
    (3, NOW(), 1, NULL, '2025-12-24 19:00:00', 2),
    (4, NOW(), 1, NULL, '2025-12-25 19:00:00', 2);

-- DATE_VOTE
INSERT IGNORE INTO date_vote (id, created_at, is_active, updated_at, date_option_id, meeting_participant_id)
VALUES
    (1, NOW(), 1, NULL, 1, 1),
    (2, NOW(), 1, NULL, 2, 2),
    (3, NOW(), 1, NULL, 1, 3),
    (4, NOW(), 1, NULL, 3, 4),
    (5, NOW(), 1, NULL, 4, 5);

-- PLACE_OPTION
INSERT IGNORE INTO place_option (id, created_at, is_active, updated_at, status, course_id)
VALUES
    (1, NOW(), 1, NULL, 'OPTION', 1),
    (2, NOW(), 1, NULL, 'OPTION', 1),
    (3, NOW(), 1, NULL, 'OPTION', 2),
    (4, NOW(), 1, NULL, 'FIXED',  3);

-- PLACE_VOTE
INSERT IGNORE INTO place_vote (id, created_at, is_active, updated_at, course_id, meeting_participant_id, place_option_id)
VALUES
    (1, NOW(), 1, NULL, 1, 1, 1),
    (2, NOW(), 1, NULL, 1, 2, 2),
    (3, NOW(), 1, NULL, 2, 3, 3),
    (4, NOW(), 1, NULL, 3, 4, 4);

SET FOREIGN_KEY_CHECKS = 1;
