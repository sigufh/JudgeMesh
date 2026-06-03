ALTER TABLE `submission`
    ADD COLUMN IF NOT EXISTS `judging_started_at` DATETIME NULL AFTER `submitted_at`,
    ADD COLUMN IF NOT EXISTS `judge_retry_count` INT NOT NULL DEFAULT 0 AFTER `judged_at`;

ALTER TABLE `submission`
    ADD INDEX `idx_status_judging_started` (`status`, `judging_started_at`);
