-- 启用 pgvector 扩展
CREATE EXTENSION IF NOT EXISTS vector;

-- 创建项目向量表（存储项目的语义向量）
CREATE TABLE IF NOT EXISTS project_embedding (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL UNIQUE,
    embedding vector(1536),  -- OpenAI text-embedding-3-small 维度
    content_hash VARCHAR(64),  -- 内容哈希，用于检测是否需要更新
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_project_embedding_project FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE
);

-- 创建向量索引（使用 IVFFlat 算法，适合中等规模数据）
CREATE INDEX IF NOT EXISTS idx_project_embedding_vector ON project_embedding
USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);

-- 创建项目ID索引
CREATE INDEX IF NOT EXISTS idx_project_embedding_project_id ON project_embedding(project_id);

-- 创建搜索历史表
CREATE TABLE IF NOT EXISTS search_history (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    keyword VARCHAR(200) NOT NULL,
    search_type VARCHAR(20) DEFAULT 'keyword',  -- keyword/semantic
    result_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_search_history_user FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE SET NULL
);

-- 创建用户搜索历史索引
CREATE INDEX IF NOT EXISTS idx_search_history_user_id ON search_history(user_id);
CREATE INDEX IF NOT EXISTS idx_search_history_keyword ON search_history(keyword);
CREATE INDEX IF NOT EXISTS idx_search_history_created_at ON search_history(created_at DESC);

-- 创建热门搜索词表
CREATE TABLE IF NOT EXISTS hot_keyword (
    id BIGSERIAL PRIMARY KEY,
    keyword VARCHAR(100) NOT NULL UNIQUE,
    search_count INT DEFAULT 1,
    last_searched_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建热门搜索索引
CREATE INDEX IF NOT EXISTS idx_hot_keyword_count ON hot_keyword(search_count DESC);

-- 添加注释
COMMENT ON TABLE project_embedding IS '项目语义向量表';
COMMENT ON COLUMN project_embedding.embedding IS '项目内容的语义向量（1536维）';
COMMENT ON COLUMN project_embedding.content_hash IS '内容MD5哈希，用于增量更新';

COMMENT ON TABLE search_history IS '用户搜索历史表';
COMMENT ON TABLE hot_keyword IS '热门搜索关键词表';

-- 创建更新热门关键词的函数
CREATE OR REPLACE FUNCTION update_hot_keyword(p_keyword VARCHAR)
RETURNS VOID AS $$
BEGIN
    INSERT INTO hot_keyword (keyword, search_count, last_searched_at)
    VALUES (p_keyword, 1, CURRENT_TIMESTAMP)
    ON CONFLICT (keyword)
    DO UPDATE SET
        search_count = hot_keyword.search_count + 1,
        last_searched_at = CURRENT_TIMESTAMP;
END;
$$ LANGUAGE plpgsql;
