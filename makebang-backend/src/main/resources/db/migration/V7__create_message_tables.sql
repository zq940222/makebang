-- 会话表
CREATE TABLE IF NOT EXISTS conversation (
    id BIGSERIAL PRIMARY KEY,
    type SMALLINT NOT NULL DEFAULT 1,
    participant1_id BIGINT NOT NULL,
    participant2_id BIGINT NOT NULL,
    order_id BIGINT,
    project_id BIGINT,
    last_message_id BIGINT,
    last_message_content VARCHAR(200),
    last_message_at TIMESTAMP,
    participant1_unread INT NOT NULL DEFAULT 0,
    participant2_unread INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_conversation_participant1 FOREIGN KEY (participant1_id) REFERENCES "user"(id),
    CONSTRAINT fk_conversation_participant2 FOREIGN KEY (participant2_id) REFERENCES "user"(id),
    CONSTRAINT fk_conversation_order FOREIGN KEY (order_id) REFERENCES "order"(id),
    CONSTRAINT fk_conversation_project FOREIGN KEY (project_id) REFERENCES project(id)
);

-- 会话索引
CREATE INDEX idx_conversation_participant1 ON conversation(participant1_id);
CREATE INDEX idx_conversation_participant2 ON conversation(participant2_id);
CREATE INDEX idx_conversation_order_id ON conversation(order_id);
CREATE INDEX idx_conversation_last_message_at ON conversation(last_message_at DESC);

-- 消息表
CREATE TABLE IF NOT EXISTS message (
    id BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    type SMALLINT NOT NULL DEFAULT 1,
    content TEXT NOT NULL,
    attachment_url VARCHAR(500),
    attachment_name VARCHAR(200),
    is_read BOOLEAN NOT NULL DEFAULT false,
    read_at TIMESTAMP,
    biz_type VARCHAR(50),
    biz_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_message_conversation FOREIGN KEY (conversation_id) REFERENCES conversation(id),
    CONSTRAINT fk_message_sender FOREIGN KEY (sender_id) REFERENCES "user"(id),
    CONSTRAINT fk_message_receiver FOREIGN KEY (receiver_id) REFERENCES "user"(id)
);

-- 消息索引
CREATE INDEX idx_message_conversation_id ON message(conversation_id);
CREATE INDEX idx_message_sender_id ON message(sender_id);
CREATE INDEX idx_message_receiver_id ON message(receiver_id);
CREATE INDEX idx_message_is_read ON message(is_read);
CREATE INDEX idx_message_created_at ON message(created_at DESC);

-- 系统通知表
CREATE TABLE IF NOT EXISTS notification (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type SMALLINT NOT NULL DEFAULT 1,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    biz_type VARCHAR(50),
    biz_id BIGINT,
    link VARCHAR(500),
    is_read BOOLEAN NOT NULL DEFAULT false,
    read_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES "user"(id)
);

-- 通知索引
CREATE INDEX idx_notification_user_id ON notification(user_id);
CREATE INDEX idx_notification_type ON notification(type);
CREATE INDEX idx_notification_is_read ON notification(is_read);
CREATE INDEX idx_notification_created_at ON notification(created_at DESC);

-- 添加注释
COMMENT ON TABLE conversation IS '会话';
COMMENT ON COLUMN conversation.type IS '类型：1-私聊 2-订单会话';
COMMENT ON COLUMN conversation.participant1_id IS '参与者1';
COMMENT ON COLUMN conversation.participant2_id IS '参与者2';
COMMENT ON COLUMN conversation.last_message_content IS '最后消息内容（截断）';

COMMENT ON TABLE message IS '消息';
COMMENT ON COLUMN message.type IS '类型：1-文本 2-图片 3-文件 4-系统消息';
COMMENT ON COLUMN message.content IS '消息内容';
COMMENT ON COLUMN message.is_read IS '是否已读';

COMMENT ON TABLE notification IS '系统通知';
COMMENT ON COLUMN notification.type IS '类型：1-系统 2-订单 3-投标 4-支付';
COMMENT ON COLUMN notification.title IS '通知标题';
COMMENT ON COLUMN notification.content IS '通知内容';
COMMENT ON COLUMN notification.link IS '跳转链接';
