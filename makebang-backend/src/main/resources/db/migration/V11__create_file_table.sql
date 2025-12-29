-- 创建文件记录表
CREATE TABLE IF NOT EXISTS file_record (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    original_name VARCHAR(255) NOT NULL,
    stored_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    thumbnail_url VARCHAR(500),
    file_size BIGINT NOT NULL,
    file_type VARCHAR(100),
    mime_type VARCHAR(100),
    storage_type VARCHAR(50) DEFAULT 'local',
    hash VARCHAR(64),
    width INT,
    height INT,
    business_type VARCHAR(50),
    business_id BIGINT,
    status INT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_file_record_user FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE SET NULL
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_file_record_user_id ON file_record(user_id);
CREATE INDEX IF NOT EXISTS idx_file_record_hash ON file_record(hash);
CREATE INDEX IF NOT EXISTS idx_file_record_business ON file_record(business_type, business_id);
CREATE INDEX IF NOT EXISTS idx_file_record_created_at ON file_record(created_at DESC);

-- 添加注释
COMMENT ON TABLE file_record IS '文件记录表';
COMMENT ON COLUMN file_record.original_name IS '原始文件名';
COMMENT ON COLUMN file_record.stored_name IS '存储文件名';
COMMENT ON COLUMN file_record.file_path IS '文件存储路径';
COMMENT ON COLUMN file_record.file_url IS '文件访问URL';
COMMENT ON COLUMN file_record.thumbnail_url IS '缩略图URL（图片类型）';
COMMENT ON COLUMN file_record.file_size IS '文件大小（字节）';
COMMENT ON COLUMN file_record.file_type IS '文件类型（后缀）';
COMMENT ON COLUMN file_record.mime_type IS 'MIME类型';
COMMENT ON COLUMN file_record.storage_type IS '存储类型: local, aliyun-oss, tencent-cos, minio';
COMMENT ON COLUMN file_record.hash IS '文件MD5哈希';
COMMENT ON COLUMN file_record.width IS '图片宽度';
COMMENT ON COLUMN file_record.height IS '图片高度';
COMMENT ON COLUMN file_record.business_type IS '业务类型: avatar, project, attachment, etc.';
COMMENT ON COLUMN file_record.business_id IS '业务ID';
COMMENT ON COLUMN file_record.status IS '状态: 0-已删除 1-正常';
