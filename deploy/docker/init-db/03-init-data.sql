-- =====================================================
-- 初始化数据
-- =====================================================

-- 初始化分类数据
INSERT INTO category (id, name, parent_id, icon, sort) VALUES
(1, 'Web开发', 0, 'icon-web', 1),
(2, '移动应用', 0, 'icon-mobile', 2),
(3, '小程序开发', 0, 'icon-miniprogram', 3),
(4, '数据分析', 0, 'icon-data', 4),
(5, 'UI/UX设计', 0, 'icon-design', 5),
(6, 'AI/机器学习', 0, 'icon-ai', 6),
(7, '后端开发', 0, 'icon-backend', 7),
(8, '运维/DevOps', 0, 'icon-devops', 8);

-- Web开发子分类
INSERT INTO category (name, parent_id, icon, sort) VALUES
('企业官网', 1, NULL, 1),
('电商网站', 1, NULL, 2),
('管理系统', 1, NULL, 3),
('数据可视化', 1, NULL, 4);

-- 移动应用子分类
INSERT INTO category (name, parent_id, icon, sort) VALUES
('iOS应用', 2, NULL, 1),
('Android应用', 2, NULL, 2),
('跨平台应用', 2, NULL, 3);

-- 小程序子分类
INSERT INTO category (name, parent_id, icon, sort) VALUES
('微信小程序', 3, NULL, 1),
('支付宝小程序', 3, NULL, 2),
('抖音小程序', 3, NULL, 3);

-- 创建管理员账号 (密码: admin123, BCrypt加密)
-- role: 0-普通用户 1-管理员 2-超级管理员
INSERT INTO "user" (username, password, phone, email, user_type, real_name, status, role)
VALUES (
    'admin',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi',
    '13800000000',
    'admin@makebang.com',
    2,
    '系统管理员',
    1,
    2  -- 超级管理员
);

-- 创建测试需求方账号 (密码: test123)
INSERT INTO "user" (username, password, phone, email, user_type, real_name, status, role)
VALUES (
    'employer_test',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi',
    '13800000001',
    'employer@makebang.com',
    0,
    '测试需求方',
    1,
    0  -- 普通用户
);

-- 创建测试程序员账号 (密码: test123)
INSERT INTO "user" (username, password, phone, email, user_type, real_name, status, role)
VALUES (
    'developer_test',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi',
    '13800000002',
    'developer@makebang.com',
    1,
    '测试程序员',
    1,
    0  -- 普通用户
);

-- 为测试程序员创建资料
INSERT INTO developer_profile (user_id, skills, experience_years, hourly_rate, bio, certification_status, credit_score)
VALUES (
    3,
    '["React", "TypeScript", "Node.js", "PostgreSQL", "Docker"]',
    5,
    200.00,
    '5年全栈开发经验,擅长React和Node.js技术栈,有丰富的企业级项目经验。',
    2,
    100.00
);

-- 为所有用户创建钱包
INSERT INTO wallet (user_id, balance) VALUES (1, 0), (2, 10000), (3, 5000);

-- 创建示例项目
INSERT INTO project (user_id, title, description, category_id, budget_min, budget_max, deadline, skill_requirements, status, view_count, bid_count)
VALUES
(2, '企业官网开发 - 响应式设计',
'需要开发一个企业官网,具体要求如下:

1. 响应式设计,支持PC和移动端访问
2. 包含首页、关于我们、产品展示、新闻动态、联系我们等模块
3. 后台管理系统,支持内容管理
4. SEO优化
5. 需要提供源代码和部署文档

技术栈要求: React + TypeScript + TailwindCSS',
1, 5000, 10000, '2025-02-01', '["React", "TypeScript", "TailwindCSS"]', 1, 156, 3),

(2, '微信小程序商城开发',
'需要开发一个微信小程序商城,包含以下功能:

1. 商品展示和搜索
2. 购物车功能
3. 订单管理
4. 微信支付集成
5. 会员系统
6. 后台管理系统

技术要求: 原生小程序或Taro框架',
3, 8000, 15000, '2025-02-15', '["微信小程序", "Taro", "Node.js"]', 1, 234, 5),

(2, '数据可视化大屏开发',
'需要开发一个数据可视化大屏,用于展示公司运营数据:

1. 支持多种图表类型(柱状图、折线图、饼图、地图等)
2. 实时数据更新(WebSocket)
3. 自适应不同分辨率屏幕
4. 炫酷的动画效果

技术要求: Vue3 + ECharts',
1, 6000, 12000, '2025-01-30', '["Vue3", "ECharts", "WebSocket"]', 1, 89, 2);

-- 创建示例投标
INSERT INTO bid (project_id, developer_id, proposed_price, proposed_days, proposal, status)
VALUES
(1, 3, 6500, 14, '您好,我有丰富的企业官网开发经验,使用React+TypeScript技术栈已有3年。之前做过多个类似项目,可以高质量完成您的需求。', 0),
(2, 3, 12000, 21, '您好,我熟悉微信小程序开发,使用Taro框架开发过多个商城类小程序。可以保证代码质量和按时交付。', 0);

-- 初始化数据完成
