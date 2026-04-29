# =====================================================
# 后端 Dockerfile（多阶段构建）
# 阶段一：Maven 构建 Spring Boot JAR
# 阶段二：JRE 运行时镜像
# =====================================================

# ---------- 阶段一：构建 ----------
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder

WORKDIR /app

# 先复制 pom.xml，利用 Docker 层缓存下载依赖
COPY pom.xml ./
RUN mvn dependency:go-offline -B --no-transfer-progress \
    -s /usr/share/maven/conf/settings.xml

# 复制源代码并打包（跳过测试加快构建速度）
COPY src ./src
RUN mvn package -DskipTests -B --no-transfer-progress

# ---------- 阶段二：运行 ----------
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 创建上传文件目录
RUN mkdir -p /app/uploads

# 从构建阶段复制 JAR
COPY --from=builder /app/target/*.jar app.jar

# 时区设置
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    apk del tzdata

EXPOSE 8080

# JVM 优化参数
ENTRYPOINT ["java", \
  "-Xms256m", \
  "-Xmx512m", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
