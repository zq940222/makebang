-- =====================================================
-- 码客邦数据库初始化
-- V1: 创建扩展和基础表结构
-- =====================================================

-- 启用必要的扩展
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "vector";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";  -- 用于模糊搜索

-- =====================================================
-- 用户表
-- =====================================================
CREATE TABLE IF NOT EXISTS "user" (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20) UNIQUE,
    email VARCHAR(100) UNIQUE,
    avatar VARCHAR(500),
    user_type SMALLINT DEFAULT 0,  -- 0:需求方 1:程序员 2:两者
    real_name VARCHAR(50),
    id_card VARCHAR(20),
    status SMALLINT DEFAULT 1,     -- 0:禁用 1:正常
    role SMALLINT DEFAULT 0,       -- 0:普通用户 1:管理员 2:超级管理员
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_user_phone ON "user"(phone) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_user_email ON "user"(email) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_user_username ON "user"(username) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_user_status ON "user"(status) WHERE deleted_at IS NULL;

COMMENT ON TABLE "user" IS '用户表';
COMMENT ON COLUMN "user".user_type IS '用户类型: 0-需求方 1-程序员 2-两者';
COMMENT ON COLUMN "user".status IS '状态: 0-禁用 1-正常';
COMMENT ON COLUMN "user".role IS '角色: 0-普通用户 1-管理员 2-超级管理员';

-- =====================================================
-- 程序员资料表
-- =====================================================
CREATE TABLE IF NOT EXISTS developer_profile (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES "user"(id),
    skills JSONB DEFAULT '[]',
    experience_years SMALLINT DEFAULT 0,
    hourly_rate DECIMAL(10,2),
    portfolio JSONB DEFAULT '[]',
    bio TEXT,
    github_url VARCHAR(255),
    certification_status SMALLINT DEFAULT 0,
    credit_score DECIMAL(5,2) DEFAULT 100.00,
    skill_embedding VECTOR(1536),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    UNIQUE(user_id)
);

CREATE INDEX IF NOT EXISTS idx_developer_user_id ON developer_profile(user_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_developer_skills ON developer_profile USING GIN(skills) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_developer_credit ON developer_profile(credit_score DESC) WHERE deleted_at IS NULL;

COMMENT ON TABLE developer_profile IS '程序员资料表';

-- =====================================================
-- 分类表
-- =====================================================
CREATE TABLE IF NOT EXISTS category (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    parent_id INTEGER DEFAULT 0,
    icon VARCHAR(100),
    sort INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

COMMENT ON TABLE category IS '项目分类表';

-- =====================================================
-- 项目/需求表
-- =====================================================
CREATE TABLE IF NOT EXISTS project (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES "user"(id),
    title VARCHAR(200) NOT NULL,
    description TEXT,
    category_id INTEGER REFERENCES category(id),
    budget_min DECIMAL(12,2),
    budget_max DECIMAL(12,2),
    deadline DATE,
    skill_requirements JSONB DEFAULT '[]',
    attachment_urls JSONB DEFAULT '[]',
    status SMALLINT DEFAULT 0,
    view_count INTEGER DEFAULT 0,
    bid_count INTEGER DEFAULT 0,
    content_embedding VECTOR(1536),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_project_user_id ON project(user_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_project_category ON project(category_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_project_status ON project(status) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_project_created ON project(created_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_project_skills ON project USING GIN(skill_requirements) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_project_title_trgm ON project USING GIN(title gin_trgm_ops) WHERE deleted_at IS NULL;

COMMENT ON TABLE project IS '项目需求表';
COMMENT ON COLUMN project.status IS '状态: 0-草稿 1-开放 2-进行中 3-已完成 4-已取消 5-已关闭';

-- =====================================================
-- 投标表
-- =====================================================
CREATE TABLE IF NOT EXISTS bid (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES project(id),
    developer_id BIGINT NOT NULL REFERENCES "user"(id),
    proposed_price DECIMAL(12,2) NOT NULL,
    proposed_days INTEGER NOT NULL,
    proposal TEXT,
    status SMALLINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    UNIQUE(project_id, developer_id)
);

CREATE INDEX IF NOT EXISTS idx_bid_project ON bid(project_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_bid_developer ON bid(developer_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_bid_status ON bid(status) WHERE deleted_at IS NULL;

COMMENT ON TABLE bid IS '投标表';
COMMENT ON COLUMN bid.status IS '状态: 0-待处理 1-已接受 2-已拒绝 3-已撤回';

-- =====================================================
-- 订单表
-- =====================================================
CREATE TABLE IF NOT EXISTS "order" (
    id BIGSERIAL PRIMARY KEY,
    order_no VARCHAR(64) UNIQUE NOT NULL,
    project_id BIGINT NOT NULL REFERENCES project(id),
    bid_id BIGINT REFERENCES bid(id),
    employer_id BIGINT NOT NULL REFERENCES "user"(id),
    developer_id BIGINT NOT NULL REFERENCES "user"(id),
    amount DECIMAL(12,2) NOT NULL,
    status SMALLINT DEFAULT 0,
    milestone_count INTEGER DEFAULT 1,
    started_at TIMESTAMP,
    deadline DATE,
    completed_at TIMESTAMP,
    remark VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_order_employer ON "order"(employer_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_order_developer ON "order"(developer_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_order_project ON "order"(project_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_order_status ON "order"(status) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_order_created ON "order"(created_at DESC) WHERE deleted_at IS NULL;

COMMENT ON TABLE "order" IS '订单表';
COMMENT ON COLUMN "order".status IS '状态: 0-待付款 1-进行中 2-已交付 3-已完成 4-已取消 5-争议中';

-- =====================================================
-- 里程碑表
-- =====================================================
CREATE TABLE IF NOT EXISTS milestone (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES "order"(id),
    title VARCHAR(200) NOT NULL,
    description TEXT,
    amount DECIMAL(12,2) NOT NULL,
    sequence INTEGER NOT NULL,
    status SMALLINT DEFAULT 0,
    due_date DATE,
    completed_at TIMESTAMP,
    submit_note TEXT,
    review_note TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_milestone_order ON milestone(order_id) WHERE deleted_at IS NULL;

COMMENT ON TABLE milestone IS '里程碑表';
COMMENT ON COLUMN milestone.status IS '状态: 0-待开始 1-进行中 2-已提交 3-已验收 4-已驳回';

-- =====================================================
-- 支付记录表
-- =====================================================
CREATE TABLE IF NOT EXISTS payment (
    id BIGSERIAL PRIMARY KEY,
    payment_no VARCHAR(64) UNIQUE NOT NULL,
    order_id BIGINT NOT NULL REFERENCES "order"(id),
    milestone_id BIGINT REFERENCES milestone(id),
    payer_id BIGINT NOT NULL REFERENCES "user"(id),
    amount DECIMAL(12,2) NOT NULL,
    payment_method VARCHAR(20),
    transaction_no VARCHAR(100),
    status SMALLINT DEFAULT 0,
    paid_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_payment_order ON payment(order_id);
CREATE INDEX IF NOT EXISTS idx_payment_payer ON payment(payer_id);
CREATE INDEX IF NOT EXISTS idx_payment_status ON payment(status);

COMMENT ON TABLE payment IS '支付记录表';
COMMENT ON COLUMN payment.status IS '状态: 0-待支付 1-已支付 2-已退款 3-支付失败';

-- =====================================================
-- Agent对话历史表
-- =====================================================
CREATE TABLE IF NOT EXISTS agent_conversation (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES "user"(id),
    session_id UUID DEFAULT uuid_generate_v4(),
    messages JSONB DEFAULT '[]',
    context JSONB DEFAULT '{}',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_agent_conv_user ON agent_conversation(user_id);
CREATE INDEX IF NOT EXISTS idx_agent_conv_session ON agent_conversation(session_id);

COMMENT ON TABLE agent_conversation IS 'Agent对话历史表';

-- =====================================================
-- 用户钱包表
-- =====================================================
CREATE TABLE IF NOT EXISTS wallet (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES "user"(id) UNIQUE,
    balance DECIMAL(12,2) DEFAULT 0.00,
    frozen_amount DECIMAL(12,2) DEFAULT 0.00,
    total_income DECIMAL(12,2) DEFAULT 0.00,
    total_expense DECIMAL(12,2) DEFAULT 0.00,
    status SMALLINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_wallet_user ON wallet(user_id);
CREATE INDEX IF NOT EXISTS idx_wallet_status ON wallet(status);

COMMENT ON TABLE wallet IS '用户钱包表';
COMMENT ON COLUMN wallet.status IS '状态: 0-正常 1-冻结';

-- =====================================================
-- 钱包流水/交易记录表
-- =====================================================
CREATE TABLE IF NOT EXISTS transaction (
    id BIGSERIAL PRIMARY KEY,
    transaction_no VARCHAR(64) NOT NULL UNIQUE,
    wallet_id BIGINT NOT NULL REFERENCES wallet(id),
    user_id BIGINT NOT NULL REFERENCES "user"(id),
    type SMALLINT NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    balance_before DECIMAL(12,2) NOT NULL,
    balance_after DECIMAL(12,2) NOT NULL,
    order_id BIGINT REFERENCES "order"(id),
    milestone_id BIGINT REFERENCES milestone(id),
    status SMALLINT DEFAULT 0,
    remark VARCHAR(500),
    out_trade_no VARCHAR(64),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_transaction_wallet_id ON transaction(wallet_id);
CREATE INDEX IF NOT EXISTS idx_transaction_user_id ON transaction(user_id);
CREATE INDEX IF NOT EXISTS idx_transaction_order_id ON transaction(order_id);
CREATE INDEX IF NOT EXISTS idx_transaction_type ON transaction(type);
CREATE INDEX IF NOT EXISTS idx_transaction_status ON transaction(status);
CREATE INDEX IF NOT EXISTS idx_transaction_created_at ON transaction(created_at DESC);

COMMENT ON TABLE transaction IS '交易记录表';
COMMENT ON COLUMN transaction.type IS '类型: 1-充值 2-提现 3-支付 4-收入 5-退款 6-服务费';
COMMENT ON COLUMN transaction.status IS '状态: 0-处理中 1-成功 2-失败';
