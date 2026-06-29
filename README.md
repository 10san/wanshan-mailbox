# 🌙 晚山信箱 (Wanshan Mailbox)

> "日暮苍山远" — 在一天的疲惫之后，有一个可以安放情绪的地方。

**晚山信箱** 是一款面向年轻群体的匿名情感树洞 Web 应用。纯匿名、零门槛、温暖治愈。

## ✨ 特性

- 🎭 **纯匿名** — 无需注册/登录，系统自动分配随机昵称
- 💬 **匿名倾诉** — 发布树洞心事，支持文字 + 图片
- 🏷️ **情绪标签** — 8 种预设情绪标签，快速分类浏览
- ❤️ **轻量互动** — 点赞、评论、"抱抱"、"同感"
- 🛡️ **安全可控** — 敏感词过滤、举报机制、管理员后台
- 🌓 **夜间模式** — 18:00 自动切换暗色主题
- 📱 **响应式** — 移动端/平板/桌面端全适配

## 🏗️ 技术架构

| 层级 | 技术 |
|------|------|
| 前端 (C端) | Vue 3 + TypeScript + Vite + TailwindCSS |
| 前端 (管理后台) | Vue 3 + TypeScript + Element Plus |
| 后端 | Spring Boot 3 + MyBatis-Plus |
| 数据库 | MySQL 8.0 |
| 缓存 | Redis 7 |
| 对象存储 | MinIO |
| 部署 | Docker Compose + Nginx |

## 🚀 快速开始

### 环境要求

- JDK 17+
- Node.js 18+
- Docker & Docker Compose

### 一键启动

```bash
# 克隆仓库
git clone https://github.com/10san/wanshan-mailbox.git
cd wanshan-mailbox

# 启动所有服务
docker-compose up -d

# 访问
# C 端: http://localhost
# 管理后台: http://localhost/admin
```

### 本地开发

```bash
# 后端
cd backend
./mvnw spring-boot:run

# 前端 C 端
cd frontend
pnpm install && pnpm dev

# 管理后台
cd admin
pnpm install && pnpm dev
```

## 📁 项目结构

```
wanshan-mailbox/
├── backend/          # Spring Boot 后端
├── frontend/         # C 端 Vue 3 前端
├── admin/            # 管理后台 Vue 3
├── docs/             # 项目文档
├── nginx/            # Nginx 配置
├── docker-compose.yml
└── README.md
```

## 📖 文档

- [产品需求文档 (PRD)](docs/晚山信箱_V1_PRD.md)
- [技术架构方案](docs/晚山信箱_技术架构方案.md)
- [评审材料](docs/晚山信箱_评审材料.md)

## 📄 许可证

本项目采用 [MIT License](LICENSE) 开源。

---

> Made with 🌙 by 晚山信箱团队
