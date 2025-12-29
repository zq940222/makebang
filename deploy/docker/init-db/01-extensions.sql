-- 启用必要的扩展
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "vector";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";  -- 用于模糊搜索

-- 验证扩展安装
SELECT extname, extversion FROM pg_extension WHERE extname IN ('uuid-ossp', 'vector', 'pg_trgm');
