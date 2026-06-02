# 餐饮订单管理系统 (Order Management System)

一个完整的餐饮外卖订单管理系统，包含用户端、商户端、管理后台和 AI 客服。

## 项目结构

```text
├── backend/             # Spring Boot 后端
│   ├── src/
│   ├── pom.xml
│   ├── Dockerfile
│   └── ...
├── frontend/            # Vue 3 前端
│   ├── src/
│   ├── package.json
│   ├── Dockerfile
│   └── ...
├── docker-compose.yml   # 本地容器编排
└── README.md
```

## 技术栈

### 后端
- Spring Boot 3.x
- Spring Security + JWT
- MyBatis Plus
- Flyway
- MySQL
- Redis
- Spring AI + DeepSeek(OpenAI 协议兼容)
- Docker

### 前端
- Vue 3 + Vite
- Element Plus
- Pinia (状态管理)
- Vue Router
- Axios
- ECharts

## 快速启动

### 前置要求
- JDK 17+
- Maven 3.9+（或使用 `backend/mvnw`）
- Node.js 18+
- MySQL 8.0+
- Redis 7+

### 后端启动

请先准备 MySQL 和 Redis，然后通过环境变量提供敏感配置：

```bash
cd backend
export SPRING_DATASOURCE_URL='jdbc:mysql://localhost:3306/order_management?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false'
export SPRING_DATASOURCE_USERNAME='root'
export SPRING_DATASOURCE_PASSWORD='你的数据库密码'
export JWT_SECRET='至少32字节的强随机JWT密钥'
export DEEPSEEK_API_KEY='你的DeepSeek API Key'
./mvnw spring-boot:run
```

如果不需要 AI 客服，可暂时不设置 `DEEPSEEK_API_KEY`，但调用 AI 接口会不可用。

### 前端启动

```bash
cd frontend
npm install
npm run dev
```

### Docker Compose 部署

建议先创建 `.env` 文件覆盖默认值，尤其是数据库密码、JWT 密钥和 DeepSeek API Key：

```bash
cp .env.example .env
# 编辑 .env，替换 MYSQL_ROOT_PASSWORD、JWT_SECRET 和 DEEPSEEK_API_KEY 等配置
docker compose up -d --build
```

服务默认端口：

- 前端：http://localhost
- 后端：http://localhost:8080
- MySQL：localhost:3306
- Redis：localhost:6379

## 功能模块

- **用户端**：浏览商家、购物车、下单、订单管理、评价、优惠券
- **商户端**：菜品管理、订单处理、营业统计、评价管理
- **管理后台**：用户管理、商户审核、订单监控、数据统计
- **AI 客服**：集成大模型实现智能客服

## License

MIT
