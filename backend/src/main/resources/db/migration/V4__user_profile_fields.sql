-- =====================================================
-- V4: 用户资料字段
-- =====================================================

ALTER TABLE `user` ADD COLUMN `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称';
ALTER TABLE `user` ADD COLUMN `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL';
