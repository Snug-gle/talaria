-- Talaria MySQL 초기화
-- docker-compose 첫 실행 시 자동 실행됨

CREATE DATABASE IF NOT EXISTS talaria_invest
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS talaria_notify
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- talaria 유저에게 두 DB 권한 부여
GRANT ALL PRIVILEGES ON talaria_invest.* TO 'talaria'@'%';
GRANT ALL PRIVILEGES ON talaria_notify.* TO 'talaria'@'%';

FLUSH PRIVILEGES;
