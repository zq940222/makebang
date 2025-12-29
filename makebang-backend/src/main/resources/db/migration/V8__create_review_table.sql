-- 评价表
CREATE TABLE IF NOT EXISTS review (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    reviewer_id BIGINT NOT NULL,
    reviewee_id BIGINT NOT NULL,
    type SMALLINT NOT NULL,
    rating SMALLINT NOT NULL,
    skill_rating SMALLINT,
    communication_rating SMALLINT,
    attitude_rating SMALLINT,
    timeliness_rating SMALLINT,
    content TEXT NOT NULL,
    tags JSONB,
    is_anonymous BOOLEAN NOT NULL DEFAULT false,
    reply TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_review_order FOREIGN KEY (order_id) REFERENCES "order"(id),
    CONSTRAINT fk_review_project FOREIGN KEY (project_id) REFERENCES project(id),
    CONSTRAINT fk_review_reviewer FOREIGN KEY (reviewer_id) REFERENCES "user"(id),
    CONSTRAINT fk_review_reviewee FOREIGN KEY (reviewee_id) REFERENCES "user"(id)
);

-- 评价索引
CREATE INDEX idx_review_order_id ON review(order_id);
CREATE INDEX idx_review_project_id ON review(project_id);
CREATE INDEX idx_review_reviewer_id ON review(reviewer_id);
CREATE INDEX idx_review_reviewee_id ON review(reviewee_id);
CREATE INDEX idx_review_type ON review(type);
CREATE INDEX idx_review_rating ON review(rating);
CREATE INDEX idx_review_created_at ON review(created_at DESC);

-- 唯一约束：同一用户只能对同一订单评价一次
CREATE UNIQUE INDEX uk_review_order_reviewer ON review(order_id, reviewer_id) WHERE deleted_at IS NULL;

-- 添加注释
COMMENT ON TABLE review IS '评价';
COMMENT ON COLUMN review.order_id IS '订单ID';
COMMENT ON COLUMN review.project_id IS '项目ID';
COMMENT ON COLUMN review.reviewer_id IS '评价者ID';
COMMENT ON COLUMN review.reviewee_id IS '被评价者ID';
COMMENT ON COLUMN review.type IS '类型：1-雇主评开发者 2-开发者评雇主';
COMMENT ON COLUMN review.rating IS '综合评分（1-5）';
COMMENT ON COLUMN review.skill_rating IS '技能评分';
COMMENT ON COLUMN review.communication_rating IS '沟通评分';
COMMENT ON COLUMN review.attitude_rating IS '态度评分';
COMMENT ON COLUMN review.timeliness_rating IS '及时性评分';
COMMENT ON COLUMN review.content IS '评价内容';
COMMENT ON COLUMN review.tags IS '评价标签';
COMMENT ON COLUMN review.is_anonymous IS '是否匿名';
COMMENT ON COLUMN review.reply IS '回复内容';
