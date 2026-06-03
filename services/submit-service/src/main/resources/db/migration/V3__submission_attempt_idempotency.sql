ALTER TABLE `submission`
    ADD COLUMN IF NOT EXISTS `active_attempt_id` VARCHAR(64) NULL AFTER `judged_by_worker`;
