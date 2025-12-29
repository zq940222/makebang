-- =====================================================
-- 码客邦数据库表结构
-- =====================================================

-- 用户表
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
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

-- 用户表索引
CREATE INDEX idx_user_phone ON "user"(phone) WHERE deleted_at IS NULL;
CREATE INDEX idx_user_email ON "user"(email) WHERE deleted_at IS NULL;
CREATE INDEX idx_user_username ON "user"(username) WHERE deleted_at IS NULL;
CREATE INDEX idx_user_status ON "user"(status) WHERE deleted_at IS NULL;

COMMENT ON TABLE "user" IS '用户表';
COMMENT ON COLUMN "user".user_type IS '用户类型: 0-需求方 1-程序员 2-两者';
COMMENT ON COLUMN "user".status IS '状态: 0-禁用 1-正常';

-- 程序员资料表
CREATE TABLE IF NOT EXISTS developer_profile (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES "user"(id),
    skills JSONB DEFAULT '[]',           -- 技能标签
    experience_years SMALLINT DEFAULT 0,
    hourly_rate DECIMAL(10,2),
    portfolio JSONB DEFAULT '[]',        -- 作品集
    bio TEXT,
    github_url VARCHAR(255),
    certification_status SMALLINT DEFAULT 0,  -- 0:未认证 1:认证中 2:已认证
    credit_score DECIMAL(5,2) DEFAULT 100.00,
    skill_embedding VECTOR(1536),        -- 技能向量(用于AI匹配)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    UNIQUE(user_id)
);

CREATE INDEX idx_developer_user_id ON developer_profile(user_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_developer_skills ON developer_profile USING GIN(skills) WHERE deleted_at IS NULL;
CREATE INDEX idx_developer_credit ON developer_profile(credit_score DESC) WHERE deleted_at IS NULL;

COMMENT ON TABLE developer_profile IS '程序员资料表';

-- 分类表
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

-- 项目/需求表
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
    status SMALLINT DEFAULT 0,           -- 0:草稿 1:开放 2:进行中 3:已完成 4:已取消 5:已关闭
    view_count INTEGER DEFAULT 0,
    bid_count INTEGER DEFAULT 0,
    content_embedding VECTOR(1536),      -- 需求向量(用于AI匹配)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_project_user_id ON project(user_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_project_category ON project(category_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_project_status ON project(status) WHERE deleted_at IS NULL;
CREATE INDEX idx_project_created ON project(created_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX idx_project_skills ON project USING GIN(skill_requirements) WHERE deleted_at IS NULL;
CREATE INDEX idx_project_title_trgm ON project USING GIN(title gin_trgm_ops) WHERE deleted_at IS NULL;

COMMENT ON TABLE project IS '项目需求表';
COMMENT ON COLUMN project.status IS '状态: 0-草稿 1-开放 2-进行中 3-已完成 4-已取消 5-已关闭';

-- 投标表
CREATE TABLE IF NOT EXISTS bid (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES project(id),
    developer_id BIGINT NOT NULL REFERENCES "user"(id),
    proposed_price DECIMAL(12,2) NOT NULL,
    proposed_days INTEGER NOT NULL,
    proposal TEXT,
    status SMALLINT DEFAULT 0,           -- 0:待处理 1:已接受 2:已拒绝 3:已撤回
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    UNIQUE(project_id, developer_id)
);

CREATE INDEX idx_bid_project ON bid(project_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_bid_developer ON bid(developer_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_bid_status ON bid(status) WHERE deleted_at IS NULL;

COMMENT ON TABLE bid IS '投标表';
COMMENT ON COLUMN bid.status IS '状态: 0-待处理 1-已接受 2-已拒绝 3-已撤回';

-- 订单表
CREATE TABLE IF NOT EXISTS "order" (
    id BIGSERIAL PRIMARY KEY,
    order_no VARCHAR(32) UNIQUE NOT NULL,
    project_id BIGINT NOT NULL REFERENCES project(id),
    employer_id BIGINT NOT NULL REFERENCES "user"(id),
    developer_id BIGINT NOT NULL REFERENCES "user"(id),
    amount DECIMAL(12,2) NOT NULL,
    status SMALLINT DEFAULT 0,           -- 0:待付款 1:进行中 2:已交付 3:已完成 4:已取消 5:争议中
    milestone_count INTEGER DEFAULT 1,
    started_at TIMESTAMP,
    deadline DATE,
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_order_employer ON "order"(employer_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_order_developer ON "order"(developer_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_order_project ON "order"(project_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_order_status ON "order"(status) WHERE deleted_at IS NULL;
CREATE INDEX idx_order_created ON "order"(created_at DESC) WHERE deleted_at IS NULL;

COMMENT ON TABLE "order" IS '订单表';
COMMENT ON COLUMN "order".status IS '状态: 0-待付款 1-进行中 2-已交付 3-已完成 4-已取消 5-争议中';

-- 里程碑表
CREATE TABLE IF NOT EXISTS milestone (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES "order"(id),
    title VARCHAR(200) NOT NULL,
    description TEXT,
    amount DECIMAL(12,2) NOT NULL,
    sequence INTEGER NOT NULL,
    status SMALLINT DEFAULT 0,           -- 0:待开始 1:进行中 2:已提交 3:已验收 4:已驳回
    due_date DATE,
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_milestone_order ON milestone(order_id) WHERE deleted_at IS NULL;

COMMENT ON TABLE milestone IS '里程碑表';

-- 支付记录表
CREATE TABLE IF NOT EXISTS payment (
    id BIGSERIAL PRIMARY KEY,
    payment_no VARCHAR(32) UNIQUE NOT NULL,
    order_id BIGINT NOT NULL REFERENCES "order"(id),
    milestone_id BIGINT REFERENCES milestone(id),
    payer_id BIGINT NOT NULL REFERENCES "user"(id),
    amount DECIMAL(12,2) NOT NULL,
    payment_method VARCHAR(20),          -- alipay, wechat, balance
    transaction_no VARCHAR(100),         -- 第三方交易号
    status SMALLINT DEFAULT 0,           -- 0:待支付 1:已支付 2:已退款 3:支付失败
    paid_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_payment_order ON payment(order_id);
CREATE INDEX idx_payment_payer ON payment(payer_id);
CREATE INDEX idx_payment_status ON payment(status);

COMMENT ON TABLE payment IS '支付记录表';

-- 评价表
CREATE TABLE IF NOT EXISTS review (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES "order"(id),
    reviewer_id BIGINT NOT NULL REFERENCES "user"(id),
    reviewee_id BIGINT NOT NULL REFERENCES "user"(id),
    rating SMALLINT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    content TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    UNIQUE(order_id, reviewer_id)
);

CREATE INDEX idx_review_reviewee ON review(reviewee_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_review_order ON review(order_id) WHERE deleted_at IS NULL;

COMMENT ON TABLE review IS '评价表';

-- 消息表
CREATE TABLE IF NOT EXISTS message (
    id BIGSERIAL PRIMARY KEY,
    sender_id BIGINT REFERENCES "user"(id),
    receiver_id BIGINT NOT NULL REFERENCES "user"(id),
    content TEXT NOT NULL,
    msg_type VARCHAR(20) DEFAULT 'CHAT', -- SYSTEM, CHAT, NOTIFICATION
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_message_receiver ON message(receiver_id, is_read) WHERE deleted_at IS NULL;
CREATE INDEX idx_message_sender ON message(sender_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_message_created ON message(created_at DESC) WHERE deleted_at IS NULL;

COMMENT ON TABLE message IS '消息表';

-- 会话表 (用于私信)
CREATE TABLE IF NOT EXISTS conversation (
    id BIGSERIAL PRIMARY KEY,
    user1_id BIGINT NOT NULL REFERENCES "user"(id),
    user2_id BIGINT NOT NULL REFERENCES "user"(id),
    last_message_id BIGINT,
    last_message_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user1_id, user2_id)
);

CREATE INDEX idx_conversation_user1 ON conversation(user1_id);
CREATE INDEX idx_conversation_user2 ON conversation(user2_id);

COMMENT ON TABLE conversation IS '会话表';

-- Agent对话历史表 (为AI功能预留)
CREATE TABLE IF NOT EXISTS agent_conversation (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES "user"(id),
    session_id UUID DEFAULT uuid_generate_v4(),
    messages JSONB DEFAULT '[]',         -- 对话消息数组
    context JSONB DEFAULT '{}',          -- 上下文信息
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_agent_conv_user ON agent_conversation(user_id);
CREATE INDEX idx_agent_conv_session ON agent_conversation(session_id);

COMMENT ON TABLE agent_conversation IS 'Agent对话历史表';

-- 用户钱包表
CREATE TABLE IF NOT EXISTS wallet (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES "user"(id) UNIQUE,
    balance DECIMAL(12,2) DEFAULT 0.00,
    frozen_amount DECIMAL(12,2) DEFAULT 0.00,  -- 冻结金额(担保中)
    total_income DECIMAL(12,2) DEFAULT 0.00,
    total_expense DECIMAL(12,2) DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE wallet IS '用户钱包表';

-- 钱包流水表
CREATE TABLE IF NOT EXISTS wallet_transaction (
    id BIGSERIAL PRIMARY KEY,
    wallet_id BIGINT NOT NULL REFERENCES wallet(id),
    type VARCHAR(20) NOT NULL,           -- INCOME, EXPENSE, FREEZE, UNFREEZE, WITHDRAW
    amount DECIMAL(12,2) NOT NULL,
    balance_after DECIMAL(12,2) NOT NULL,
    related_order_id BIGINT,
    description VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_wallet_trans_wallet ON wallet_transaction(wallet_id);
CREATE INDEX idx_wallet_trans_created ON wallet_transaction(created_at DESC);

COMMENT ON TABLE wallet_transaction IS '钱包流水表';

-- =====================================================
-- 创建向量索引 (需要足够数据后再创建)
-- =====================================================
-- 注意: IVFFlat索引需要表中有一定数据量后才能有效
-- 建议在有1000+条数据后执行以下语句

-- CREATE INDEX idx_developer_skill_embedding ON developer_profile
--     USING ivfflat (skill_embedding vector_cosine_ops) WITH (lists = 100);

-- CREATE INDEX idx_project_content_embedding ON project
--     USING ivfflat (content_embedding vector_cosine_ops) WITH (lists = 100);
