-- 添加用户角色字段
ALTER TABLE "user" ADD COLUMN IF NOT EXISTS role INTEGER DEFAULT 0;

-- 添加注释
COMMENT ON COLUMN "user".role IS '角色: 0-普通用户 1-管理员 2-超级管理员';

-- 创建索引以便按角色查询
CREATE INDEX IF NOT EXISTS idx_user_role ON "user"(role);

-- 为项目表添加审核相关字段（如果不存在）
ALTER TABLE project ADD COLUMN IF NOT EXISTS reject_reason VARCHAR(500);

COMMENT ON COLUMN project.reject_reason IS '审核拒绝/下架原因';
