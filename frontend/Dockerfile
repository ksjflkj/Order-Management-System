# =====================================================
# 前端 Dockerfile（多阶段构建）
# 阶段一：Node.js 构建 Vue 应用
# 阶段二：Nginx 托管静态文件
# =====================================================

# ---------- 阶段一：构建 ----------
FROM node:20-alpine AS builder

WORKDIR /app

# 先复制依赖文件，利用 Docker 层缓存加速构建
COPY package*.json ./
RUN npm install --registry=https://registry.npmmirror.com

# 复制源代码并构建
COPY . .
RUN npm run build

# ---------- 阶段二：托管 ----------
FROM nginx:1.25-alpine

# 复制 Nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 复制构建产物
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
