-- init-sql/01-data.sql
SET NAMES utf8mb4;
-- Create tables if they don't exist to allow initialization before Hibernate runs

CREATE TABLE IF NOT EXISTS member (
    seq BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id VARCHAR(255) NOT NULL UNIQUE,
    member_pw VARCHAR(255) NOT NULL,
    member_name VARCHAR(255) NOT NULL,
    member_phone VARCHAR(255) NOT NULL,
    role_code VARCHAR(20) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6),
    modified_at DATETIME(6),
    created_by VARCHAR(100) NOT NULL DEFAULT 'system',
    modified_by VARCHAR(100)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS code (
    seq BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(255) NOT NULL UNIQUE,
    parent_seq BIGINT,
    code_name VARCHAR(255) NOT NULL,
    reg_date DATETIME(6) NOT NULL,
    FOREIGN KEY (parent_seq) REFERENCES code(seq)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS pet_house (
    house_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    nickname VARCHAR(255) NOT NULL,
    pet_house_status VARCHAR(50) NOT NULL,
    is_occupied BOOLEAN NOT NULL,
    last_connected_at DATETIME(6) NOT NULL,
    object_code_seq BIGINT,
    object_name VARCHAR(255),
    object_birth DATE,
    created_at DATETIME(6),
    modified_at DATETIME(6),
    created_by VARCHAR(100) NOT NULL DEFAULT 'system',
    modified_by VARCHAR(100),
    FOREIGN KEY (user_id) REFERENCES member(seq),
    FOREIGN KEY (object_code_seq) REFERENCES code(seq),
    UNIQUE KEY uk_pet_house_user_id_nickname (user_id, nickname)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS device (
    seq BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT,
    serial_num VARCHAR(255) NOT NULL,
    device_type VARCHAR(255) NOT NULL,
    is_use BOOLEAN NOT NULL DEFAULT TRUE,
    reg_date DATETIME(6),
    house_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES member(seq),
    FOREIGN KEY (house_id) REFERENCES pet_house(house_id)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS hospital (
    seq BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    location VARCHAR(255) NOT NULL,
    phone VARCHAR(50) NOT NULL,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    main_med_code_seq BIGINT,
    created_at DATETIME(6),
    modified_at DATETIME(6),
    created_by VARCHAR(100) NOT NULL DEFAULT 'system',
    modified_by VARCHAR(100),
    FOREIGN KEY (main_med_code_seq) REFERENCES code(seq)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS hospital_med_code_map (
    hospital_seq BIGINT NOT NULL,
    code_seq BIGINT NOT NULL,
    PRIMARY KEY (hospital_seq, code_seq),
    FOREIGN KEY (hospital_seq) REFERENCES hospital(seq),
    FOREIGN KEY (code_seq) REFERENCES code(seq)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS fan_schedule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    house_id BIGINT NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    created_at DATETIME(6),
    modified_at DATETIME(6),
    FOREIGN KEY (house_id) REFERENCES pet_house(house_id),
    UNIQUE KEY uk_fan_schedule_house_condition (house_id, start_time, end_time)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS fan_schedule_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    schedule_id BIGINT NOT NULL,
    temperature DECIMAL(3,1) NOT NULL,
    speed INT NOT NULL,
    FOREIGN KEY (schedule_id) REFERENCES fan_schedule(id)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS fan_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    schedule_id BIGINT,
    house_id BIGINT NOT NULL,
    temperature DECIMAL(3,1) NOT NULL,
    speed INT NOT NULL,
    start_time DATETIME(6) NOT NULL,
    end_time DATETIME(6) NOT NULL,
    execution_status VARCHAR(50) NOT NULL,
    trigger_type VARCHAR(50) NOT NULL,
    created_at DATETIME(6),
    FOREIGN KEY (schedule_id) REFERENCES fan_schedule(id),
    FOREIGN KEY (house_id) REFERENCES pet_house(house_id)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS supply_schedule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    house_id BIGINT NOT NULL,
    feed_type VARCHAR(50) NOT NULL,
    unit_type VARCHAR(50) NOT NULL,
    amount DECIMAL(6,2) NOT NULL,
    cron_expression VARCHAR(100) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    last_run_at DATETIME(6),
    created_at DATETIME(6),
    modified_at DATETIME(6),
    FOREIGN KEY (house_id) REFERENCES pet_house(house_id),
    UNIQUE KEY uk_supply_schedule_house_condition (house_id, feed_type, cron_expression)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS supply_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    schedule_id BIGINT,
    house_id BIGINT NOT NULL,
    feed_type VARCHAR(50) NOT NULL,
    unit_type VARCHAR(50) NOT NULL,
    amount DECIMAL(6,2) NOT NULL,
    execution_status VARCHAR(50) NOT NULL,
    trigger_type VARCHAR(50) NOT NULL,
    created_at DATETIME(6),
    FOREIGN KEY (schedule_id) REFERENCES supply_schedule(id),
    FOREIGN KEY (house_id) REFERENCES pet_house(house_id)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;


-- ==================================================
-- Mock Data
-- ==================================================

-- 1. 회원 (member)
-- Password: '{noop}password123' (Spring Security NoOpPasswordEncoder)
INSERT IGNORE INTO member (seq, member_id, member_pw, member_name, member_phone, role_code, enabled, created_at, modified_at, created_by)
VALUES
  (1, 'user1', '{noop}password123', '홍길동', '010-1111-2222', 'USER', true, NOW(), NOW(), 'system'),
  (2, 'user2', '{noop}password123', '김민지', '010-2222-3333', 'USER', true, NOW(), NOW(), 'system'),
  (3, 'admin1', '{noop}admin123', '이관리', '010-9999-8888', 'ADMIN', true, NOW(), NOW(), 'system');

-- 2. 코드 (code) - 동물 종류 (부모 없음)
INSERT IGNORE INTO code (seq, code, parent_seq, code_name, reg_date) VALUES
  (1,  'DOG',         NULL, '강아지',    NOW()),
  (2,  'CAT',         NULL, '고양이',    NOW()),
  (3,  'RABBIT',      NULL, '토끼',      NOW()),
  (4,  'HAMSTER',     NULL, '햄스터',    NOW());

-- 2-1. 코드 (code) - 진료 과목
INSERT IGNORE INTO code (seq, code, parent_seq, code_name, reg_date) VALUES
  (10, 'INTERNAL',    NULL, '내과',      NOW()),
  (11, 'SURGERY',     NULL, '외과',      NOW()),
  (12, 'DERMATOLOGY', NULL, '피부과',    NOW()),
  (13, 'OPHTHALMOLOGY',NULL,'안과',      NOW()),
  (14, 'DENTISTRY',   NULL, '치과',      NOW()),
  (15, 'ORTHOPEDICS', NULL, '정형외과',  NOW());

-- 3. 펫하우스 (pet_house)
INSERT IGNORE INTO pet_house (house_id, user_id, nickname, pet_house_status, is_occupied, last_connected_at, object_code_seq, object_name, object_birth, created_at, modified_at, created_by)
VALUES
  (1, 1, '우리집 멍멍이',   'ONLINE',  true,  NOW(), 1, '바둑이', '2022-03-15', NOW(), NOW(), 'system'),
  (2, 1, '거실 냥이집',     'OFFLINE', false, NOW(), 2, '나비',   '2021-07-20', NOW(), NOW(), 'system'),
  (3, 2, '김민지네 토끼장', 'ONLINE',  true,  NOW(), 3, '솜이',   '2023-11-01', NOW(), NOW(), 'system'),
  (4, 2, '햄스터 케이지',   'OFFLINE', false, NOW(), 4, '땅콩',   '2024-01-10', NOW(), NOW(), 'system');

-- 4. 디바이스 (device)
INSERT IGNORE INTO device (seq, device_id, user_id, serial_num, device_type, is_use, reg_date, house_id)
VALUES
  (1, 'PET-HOUSE-01', 1, 'SN-001-ALPHA', 'PETHOUSE_MAIN', true, NOW(), 1),
  (2, 'PET-HOUSE-02', 1, 'SN-002-ALPHA', 'PETHOUSE_MAIN', true, NOW(), 2),
  (3, 'PET-HOUSE-03', 2, 'SN-003-BETA',  'PETHOUSE_MAIN', true, NOW(), 3),
  (4, 'PET-HOUSE-04', 2, 'SN-004-BETA',  'PETHOUSE_MAIN', false, NOW(), 4);

-- 5. 병원 (hospital)
INSERT IGNORE INTO hospital (seq, name, location, phone, latitude, longitude, main_med_code_seq, created_at, modified_at, created_by)
VALUES
  (1, '행복동물병원',     '서울특별시 강남구 테헤란로 123',    '02-1111-2222', 37.4979, 127.0276, 10, NOW(), NOW(), 'system'),
  (2, '서울펫클리닉',     '서울특별시 마포구 홍대입구역 456',  '02-2222-3333', 37.5567, 126.9236, 11, NOW(), NOW(), 'system'),
  (3, '강북동물의료센터', '서울특별시 노원구 동일로 789',      '02-3333-4444', 37.6542, 127.0568, 10, NOW(), NOW(), 'system'),
  (4, '하남펫병원',       '경기도 하남시 미사대로 321',        '031-4444-5555', 37.5392, 127.2070, 12, NOW(), NOW(), 'system'),
  (5, '분당동물병원',     '경기도 성남시 분당구 판교역로 88',  '031-5555-6666', 37.3943, 127.1110, 11, NOW(), NOW(), 'system');

-- 5-1. 병원-진료과목 매핑 (hospital_med_code_map)
INSERT IGNORE INTO hospital_med_code_map (hospital_seq, code_seq) VALUES
  (1, 10), (1, 12), (1, 13),
  (2, 11), (2, 15),
  (3, 10), (3, 11), (3, 14),
  (4, 12), (4, 13),
  (5, 10), (5, 11), (5, 15);

-- 6. 팬 스케줄 (fan_schedule)
INSERT IGNORE INTO fan_schedule (id, house_id, enabled, start_time, end_time, created_at, modified_at)
VALUES
  (1, 1, true,  '08:00:00', '10:00:00', NOW(), NOW()),
  (2, 1, true,  '20:00:00', '22:00:00', NOW(), NOW()),
  (3, 2, true,  '07:00:00', '09:00:00', NOW(), NOW()),
  (4, 3, false, '14:00:00', '16:00:00', NOW(), NOW());

-- 6-1. 팬 스케줄 상세 (fan_schedule_detail) - 온도에 따른 속도 단계
INSERT IGNORE INTO fan_schedule_detail (id, schedule_id, temperature, speed)
VALUES
  (1, 1, 25.0, 1),
  (2, 1, 28.0, 2),
  (3, 1, 31.0, 3),
  (4, 2, 24.0, 1),
  (5, 2, 27.0, 2),
  (6, 3, 26.0, 1),
  (7, 3, 29.0, 2),
  (8, 3, 32.0, 3);

-- 6-2. 팬 로그 (fan_log)
INSERT IGNORE INTO fan_log (id, schedule_id, house_id, temperature, speed, start_time, end_time, execution_status, trigger_type, created_at)
VALUES
  (1,  1, 1, 27.5, 2, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 2 HOUR, 'SUCCESS',    'AUTO',   DATE_SUB(NOW(), INTERVAL 2 DAY)),
  (2,  1, 1, 29.0, 3, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 2 HOUR, 'SUCCESS',    'AUTO',   DATE_SUB(NOW(), INTERVAL 1 DAY)),
  (3,  2, 1, 25.0, 1, NOW() - INTERVAL 4 HOUR,         NOW() - INTERVAL 2 HOUR,                           'SUCCESS',    'AUTO',   NOW() - INTERVAL 4 HOUR),
  (4, NULL,1, 30.0, 3, NOW() - INTERVAL 1 HOUR,         NOW(),                                             'SUCCESS',    'MANUAL', NOW() - INTERVAL 1 HOUR),
  (5,  3, 2, 26.0, 1, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 2 HOUR, 'SUCCESS',    'AUTO',   DATE_SUB(NOW(), INTERVAL 1 DAY)),
  (6,  3, 2, 31.0, 3, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 2 HOUR, 'FAILED',     'AUTO',   DATE_SUB(NOW(), INTERVAL 3 DAY));

-- 7. 급식/급수 스케줄 (supply_schedule)
-- feed_type: FOOD(사료), WATER(물)
-- unit_type: G(그램), ML(밀리리터)
-- cron_expression: Spring Cron 형식
INSERT IGNORE INTO supply_schedule (id, house_id, feed_type, unit_type, amount, cron_expression, enabled, last_run_at, created_at, modified_at)
VALUES
  (1, 1, 'FOOD',  'G', 80.00, '0 0 8 * * *',  true,  NOW() - INTERVAL 1 HOUR,  NOW(), NOW()),
  (2, 1, 'FOOD',  'G', 80.00, '0 0 18 * * *', true,  NOW() - INTERVAL 1 HOUR,  NOW(), NOW()),
  (3, 1, 'WATER', 'ML',  200.00, '0 0 9 * * *',  true,  NOW() - INTERVAL 30 MINUTE, NOW(), NOW()),
  (4, 2, 'FOOD',  'G', 60.00, '0 0 8 * * *',  true,  NULL,                       NOW(), NOW()),
  (5, 3, 'FOOD',  'G', 50.00, '0 0 7 * * *',  false, NULL,                       NOW(), NOW()),
  (6, 3, 'WATER', 'ML',  150.00, '0 0 12 * * *', true,  NOW() - INTERVAL 2 HOUR,   NOW(), NOW());

-- 7-1. 급식/급수 로그 (supply_log)
INSERT IGNORE INTO supply_log (id, schedule_id, house_id, feed_type, unit_type, amount, execution_status, trigger_type, created_at)
VALUES
  (1,  1, 1, 'FOOD',  'G', 80.00, 'SUCCESS',    'AUTO',   NOW() - INTERVAL 2 DAY),
  (2,  1, 1, 'FOOD',  'G', 80.00, 'SUCCESS',    'AUTO',   NOW() - INTERVAL 1 DAY),
  (3,  2, 1, 'FOOD',  'G', 80.00, 'SUCCESS',    'AUTO',   NOW() - INTERVAL 1 DAY),
  (4,  3, 1, 'WATER', 'ML',  200.00, 'SUCCESS',    'AUTO',   NOW() - INTERVAL 1 DAY),
  (5, NULL,1, 'FOOD',  'G', 120.00, 'SUCCESS',    'MANUAL', NOW() - INTERVAL 3 HOUR),
  (6, NULL,1, 'WATER', 'ML',  100.00, 'PROCEEDING', 'MANUAL', NOW() - INTERVAL 10 MINUTE),
  (7,  4, 2, 'FOOD',  'G', 60.00, 'SUCCESS',    'AUTO',   NOW() - INTERVAL 1 DAY),
  (8,  6, 3, 'WATER', 'ML',  150.00, 'FAILED',     'AUTO',   NOW() - INTERVAL 2 DAY);
