# 餐饮订单管理系统 (Order Management System)

一个完整的餐饮外卖订单管理系统，包含用户端、商户端和管理后台。

## 项目结构

```
├── backend/          # Spring Boot 后端
│   ├── src/
│   ├── pom.xml
│   ├── Dockerfile
│   └── ...
├── frontend/         # Vue 3 前端
│   ├── src/
│   ├── package.json
│   ├── Dockerfile
│   └── ...
└── docker-compose.yml
```

## 技术栈

### 后端
- Spring Boot 3.x
- Spring Security + JWT
- MyBatis Plus
- MySQL
- Redis
- Docker

### 前端
- Vue 3 + Vite
- Pinia (状态管理)
- Vue Router
- Axios

## 快速启动

### 前置要求
- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Redis

### 后端启动
```bash
cd backend
mvnw spring-boot:run
```

### 前端启动
```bash
cd frontend
npm install
npm run dev
```

### Docker 部署
```bash
docker-compose up -d
```

## 功能模块

- **用户端**：浏览商家、购物车、下单、订单管理、评价、优惠券
- **商户端**：菜品管理、订单处理、营业统计、评价管理
- **管理后台**：用户管理、商户审核、订单监控、数据统计
- **AI 客服**：集成大模型实现智能客服

## License

MIT