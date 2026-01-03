-- 0) DB / 인코딩 / FK 설정
CREATE DATABASE IF NOT EXISTS swyp
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE swyp;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

------------------------------------------------------
-- 2) SEED DATA (INSERT IGNORE 로 여러 번 실행해도 안전하게)
------------------------------------------------------

-- MEETING
INSERT IGNORE INTO meeting (id, public_id, created_at, is_active, updated_at, date, time, status, title)
VALUES
    (1, UUID_TO_BIN(UUID()), NOW(), 1, NULL, '2025-12-20', NULL, 'CREATED', '연말 모임'),
    (2, UUID_TO_BIN(UUID()), NOW(), 1, NULL, '2025-12-25', '19:00:00', 'PLACE_VOTING', '크리스마스 모임');

-- MEMBER
INSERT IGNORE INTO member (id, created_at, birth_date, email, gender, nickname, character_type, role)
VALUES (1, NOW(), '1999-01-01', 'host@example.com', 'MALE', '호스트유저', 'FOODIE', 'MEMBER'),
       (2, NOW(), '1998-05-10', 'member1@example.com', 'FEMALE', '참여자1', 'DRINKER', 'MEMBER'),
       (3, NOW(), '1995-02-14', 'member2@example.com', 'MALE', '참여자2', 'HEALER', 'MEMBER'),
       (4, NOW(), '1995-02-14', 'existing@example.com', 'MALE', '기존 회원', 'TRAVELER', 'MEMBER');

-- SOCIAL_ACCOUNT
INSERT IGNORE INTO social_account (id, created_at, social_provider, social_id, member_id)
VALUES (1, NOW(), 'KAKAO', 'existing-social-id-123', 4);

-- MEETING_PARTICIPANT
INSERT IGNORE INTO meeting_participant (id, created_at, is_active, updated_at, role, meeting_id, member_id)
VALUES (1, NOW(), 1, NULL, 'HOST', 1, 1),
       (2, NOW(), 1, NULL, 'MEMBER', 1, 2),
       (3, NOW(), 1, NULL, 'MEMBER', 1, 3),
       (4, NOW(), 1, NULL, 'HOST', 2, 2),
       (5, NOW(), 1, NULL, 'MEMBER', 2, 4);

-- COURSE
INSERT IGNORE INTO course (id, created_at, is_active, updated_at, step, meeting_id)
VALUES (1, NOW(), 1, NULL, 1, 1),
       (2, NOW(), 1, NULL, 2, 1),
       (3, NOW(), 1, NULL, 1, 2);

-- DATE_VOTE
INSERT IGNORE INTO date_vote (id, created_at, meeting_participant_id, meeting_id, date)
VALUES (1, NOW(), 1, 1, '2025-12-20'),
       (2, NOW(), 2, 1, '2025-12-21'),
       (3, NOW(), 3, 1, '2025-12-24'),
       (4, NOW(), 4, 2, '2025-12-25'),
       (5, NOW(), 5, 2, '2025-12-26');

-- TIME_VOTE
INSERT IGNORE INTO time_vote (id, created_at, meeting_id, meeting_participant_id, time)
VALUES (1, NOW(), 2, 1, '15:00:00'), -- host
       (2, NOW(), 2, 2, '15:00:00'), -- member1
       (3, NOW(), 2, 3, '16:00:00');
-- member2

-- PLACE_OPTION
INSERT IGNORE INTO place_option (id, created_at, is_active, updated_at, status, course_id)
VALUES (1, NOW(), 1, NULL, 'OPTION', 1),
       (2, NOW(), 1, NULL, 'OPTION', 1),
       (3, NOW(), 1, NULL, 'OPTION', 2),
       (4, NOW(), 1, NULL, 'FIXED', 3);

-- PLACE_VOTE
INSERT IGNORE INTO place_vote (id, created_at, is_active, updated_at, course_id, meeting_participant_id,
                               place_option_id)
VALUES (1, NOW(), 1, NULL, 1, 1, 1),
       (2, NOW(), 1, NULL, 1, 2, 2),
       (3, NOW(), 1, NULL, 2, 3, 3),
       (4, NOW(), 1, NULL, 3, 4, 4);

-- NOTIFICATION
INSERT IGNORE INTO notification (id, created_at, is_active, updated_at, member_id, type, status, title, body, deep_link,
                                 meeting_id, fail_reason, sent_at, read_at)
VALUES (1, NOW(), 1, NULL, 4, 'VOTE_STARTED', 'SENT', '모임 투표가 시작됐어요', '참여 중인 모임의 날짜 투표가 시작되었습니다.',
        'moit://meetings/1/votes', 1, NULL, NOW(), NULL),
       (2, NOW(), 1, NULL, 4, 'TIME_VOTE_RESULT_CONFIRMED', 'READ', '모임 시간이 확정됐어요', '투표 결과로 모임 시간이 확정되었습니다.',
        'moit://meetings/1', 1, NULL, NOW(), NOW());

-- FRIENDSHIP
INSERT IGNORE INTO friendship(id, created_at, is_active, updated_at, member_id, friend_id, met_count)
VALUES
    (1, NOW(), 1, NULL, 4, 3, 1),
    (2, NOW(), 1, NULL, 3, 4, 1),
    (3, NOW(), 1, NULL, 2, 4, 1),
    (4, NOW(), 1, NULL, 4, 2, 1),
    (5, NOW(), 1, NULL, 3, 2, 1),
    (6, NOW(), 1, NULL, 2, 3, 1);

-- FRIEND_GROUP
INSERT IGNORE INTO friend_group(id, created_at, is_active, updated_at, owner_id, group_name)
VALUES
    (1, NOW(), 1, NULL, 3, '모잉이들'),
    (2, NOW(), 1, NULL, 2, '스위프');

-- FRIEND_GROUP_MEMBER
INSERT IGNORE INTO friend_group_member(id, created_at, is_active, updated_at, friend_group_id, member_id)
VALUES
    (1, NOW(), 1, NULL, 1, 2),
    (2, NOW(), 1, NULL, 1, 4),
    (3, NOW(), 1, NULL, 2, 3),
    (4, NOW(), 1, NULL, 2, 4);

-- MEETING_REVIEW
INSERT IGNORE INTO meeting_review (id, created_at, is_active, updated_at, meeting_participant_id, content)
VALUES
    (1, NOW(), 1, NULL, 5, '분위기도 좋고 시간 조율이 잘 돼서 정말 만족스러운 모임이었어요.'),
    (2, NOW(), 1, NULL, 1, '처음 만나는 분들이었는데 어색하지 않게 잘 진행됐어요.'),
    (3, NOW(), 1, NULL, 2, '장소 선정이 특히 마음에 들었고 다음에도 참여하고 싶어요.'),
    (4, NOW(), 1, NULL, 3, '투표 과정이 편리해서 일정 잡기가 쉬웠습니다.');

SET FOREIGN_KEY_CHECKS = 1;
