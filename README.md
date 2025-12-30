# MakeBang 码客邦

> 面向中国国内市场的程序员接单平台，连接软件开发需求方与专业程序员，实现需求发布、接单、交付、支付的全流程闭环。

## 项目简介

码客邦是一个类似于 Freelancer 的在线接单平台，旨在：

- 为需求方提供便捷的项目发布和管理平台
- 为程序员提供优质的接单和展示平台
- 建立安全可靠的交易担保机制
- 构建程序员技能评价和信用体系

## 技术栈

### 后端

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.x | 主框架 |
| Spring Security | 6.x | 认证授权 |
| MyBatis-Plus | 3.5.x | ORM框架 |
| PostgreSQL | 16.x | 主数据库（支持 pgvector） |
| Redis | 7.x | 缓存/分布式锁 |
| JWT | 0.12.x | Token认证 |
| Knife4j | 4.x | API文档 |
| Hutool | 5.8.x | 工具库 |
| Aliyun OSS | 3.17.x | 文件存储 |

### 前端

| 技术 | 版本 | 说明 |
|------|------|------|
| React | 18.x | UI框架 |
| TypeScript | 5.x | 类型安全 |
| Vite | 5.x | 构建工具 |
| Ant Design | 5.x | UI组件库 |
| Redux Toolkit | 2.x | 状态管理 |
| React Router | 6.x | 前端路由 |
| Axios | 1.x | HTTP客户端 |
| TailwindCSS | 3.x | CSS框架 |
| ECharts | 5.x | 数据可视化 |

## 项目结构

```
makebang/
├── docs/                          # 文档目录
│   └── 技术架构文档.md
│
├── makebang-backend/              # 后端项目 (Spring Boot)
│   ├── src/main/java/com/makebang/
│   │   ├── config/                # 配置类
│   │   ├── controller/            # 控制器
│   │   ├── service/               # 服务层
│   │   │   └── impl/              # 服务实现
│   │   ├── mapper/                # MyBatis Mapper
│   │   ├── entity/                # 实体类
│   │   ├── dto/                   # 数据传输对象
│   │   └── common/                # 公共模块
│   ├── Dockerfile
│   └── pom.xml
│
├── makebang-frontend/             # 前端项目 (React)
│   ├── src/
│   │   ├── api/                   # API接口
│   │   ├── components/            # 组件
│   │   ├── pages/                 # 页面
│   │   ├── router/                # 路由配置
│   │   ├── store/                 # Redux状态管理
│   │   ├── types/                 # TypeScript类型
│   │   └── utils/                 # 工具函数
│   ├── Dockerfile
│   └── package.json
│
├── deploy/                        # 部署配置
│   └── docker/
│       └── init-db/               # 数据库初始化脚本
│
├── docker-compose.yml             # Docker编排配置
├── docker-compose.dev.yml         # 开发环境Docker配置
└── .env.example                   # 环境变量示例
```

## 核心功能模块

- **用户模块** - 注册登录、实名认证、个人资料管理
- **项目模块** - 需求发布、分类检索、项目管理
- **投标模块** - 投标竞价、选择中标
- **订单模块** - 订单管理、里程碑管理、交付验收
- **支付模块** - 担保交易、钱包管理、充值提现
- **消息模块** - 站内信、系统通知
- **评价模块** - 双向评价、信用评分
- **管理后台** - 用户管理、内容审核、数据统计

## 快速开始

### 环境要求

- Java 17+
- Node.js 18+
- Docker & Docker Compose
- Maven 3.8+

### 使用 Docker Compose 启动（推荐）

1. **克隆项目**

```bash
git clone <repository-url>
cd makebang
```

2. **配置环境变量**

```bash
cp .env.example .env
# 编辑 .env 文件，配置必要的环境变量
```

3. **启动所有服务**

```bash
# 生产模式
docker-compose up -d

# 开发模式（包含数据库管理工具）
docker-compose --profile dev up -d
```

4. **访问服务**

| 服务 | 地址 | 说明 |
|------|------|------|
| 前端 | http://localhost:80 | Web应用 |
| 后端API | http://localhost:8080 | API服务 |
| API文档 | http://localhost:8080/doc.html | Knife4j |
| Adminer | http://localhost:8081 | 数据库管理（dev模式） |
| Redis Commander | http://localhost:8082 | Redis管理（dev模式） |

### 本地开发

#### 后端

```bash
cd makebang-backend

# 安装依赖并运行
mvn clean install
mvn spring-boot:run
```

#### 前端

```bash
cd makebang-frontend

# 安装依赖
yarn install  # 或 npm install

# 启动开发服务器
yarn dev  # 或 npm run dev
```

## 环境配置

### 环境变量说明

```bash
# PostgreSQL
POSTGRES_DB=makebang
POSTGRES_USER=makebang
POSTGRES_PASSWORD=your_password
POSTGRES_PORT=5432

# Redis
REDIS_PASSWORD=your_password
REDIS_PORT=6379

# 后端
BACKEND_PORT=8080
JWT_SECRET=your_jwt_secret

# 前端
FRONTEND_PORT=80
VITE_API_BASE_URL=/api
```

## API 文档

启动后端服务后，访问 http://localhost:8080/doc.html 查看 Knife4j API 文档。

### 主要 API 端点

```
POST   /api/v1/auth/register     # 用户注册
POST   /api/v1/auth/login        # 用户登录
GET    /api/v1/users/me          # 当前用户信息
GET    /api/v1/projects          # 项目列表
POST   /api/v1/projects          # 发布项目
POST   /api/v1/bids              # 提交投标
GET    /api/v1/orders            # 订单列表
GET    /api/v1/wallet            # 钱包信息
```

## 数据库

项目使用 PostgreSQL 16，并启用了以下扩展：

- **uuid-ossp** - UUID 生成
- **pgvector** - 向量存储（用于 AI 语义检索）
- **pg_trgm** - 三元组匹配（用于模糊搜索）

数据库初始化脚本位于 `deploy/docker/init-db/` 目录。

## 开发规范

### Git 提交规范

```
feat: 新功能
fix: 修复bug
docs: 文档更新
style: 代码格式
refactor: 重构
test: 测试
chore: 构建/工具
```

### 分支管理

- `main` - 主分支，生产环境
- `develop` - 开发分支
- `feature/*` - 功能分支
- `hotfix/*` - 紧急修复

## 项目规划

| 阶段 | 内容 | 平台 |
|------|------|------|
| 第一阶段 | MVP版本 | Web网站 |
| 第二阶段 | 功能完善 | Web + 微信小程序 |
| 第三阶段 | 全平台 | Web + 小程序 + iOS/Android APP |

## 文档

- [技术架构文档](docs/技术架构文档.md)

## 许可证

本项目仅供学习交流使用。

---

**码客邦** - 连接需求与技术
