-- submits_db V1 init

CREATE TABLE `submission` (
    `id`               BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`          BIGINT       NOT NULL,
    `problem_id`       BIGINT       NOT NULL,
    `contest_id`       BIGINT COMMENT 'null = normal submission',
    `language`         VARCHAR(16)  NOT NULL,
    `code`             MEDIUMTEXT   NOT NULL,
    `code_length`      INT          NOT NULL,
    `status`           VARCHAR(16)  NOT NULL DEFAULT 'pending',
    `score`            INT          NOT NULL DEFAULT 0,
    `time_used_ms`     INT,
    `memory_used_kb`   INT,
    `judge_message`    TEXT,
    `judged_by_worker` VARCHAR(64) COMMENT 'Worker pod name',
    `submitted_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `judged_at`        DATETIME,
    PRIMARY KEY (`id`),
    INDEX `idx_user_submitted` (`user_id`, `submitted_at`),
    INDEX `idx_problem_status` (`problem_id`, `status`),
    INDEX `idx_contest_user` (`contest_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `contest` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `title`       VARCHAR(255) NOT NULL,
    `description` TEXT,
    `start_time`  DATETIME     NOT NULL,
    `end_time`    DATETIME     NOT NULL,
    `freeze_min`  INT          NOT NULL DEFAULT 30,
    `created_by`  BIGINT       NOT NULL,
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_time` (`start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `contest_problem` (
    `contest_id` BIGINT NOT NULL,
    `problem_id` BIGINT NOT NULL,
    `seq`        INT    NOT NULL COMMENT 'problem order in contest',
    PRIMARY KEY (`contest_id`, `problem_id`),
    INDEX `idx_problem` (`problem_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `contest_register` (
    `contest_id`    BIGINT   NOT NULL,
    `user_id`       BIGINT   NOT NULL,
    `registered_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`contest_id`, `user_id`),
    INDEX `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
