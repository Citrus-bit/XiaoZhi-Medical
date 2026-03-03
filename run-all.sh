#!/bin/bash

# 杀死占用端口的进程（可选，防止端口冲突）
# lsof -ti:8080 | xargs kill -9 2>/dev/null
# lsof -ti:5173 | xargs kill -9 2>/dev/null

echo "🚀 正在启动后端服务..."
# 后台启动后端，日志输出到 backend.log
mvn spring-boot:run > backend.log 2>&1 &
BACKEND_PID=$!
echo "后端服务已在后台启动 (PID: $BACKEND_PID)，日志请查看 backend.log"

echo "⏳ 等待后端启动..."
sleep 5

echo "🚀 正在启动前端服务..."
cd xiaozhi-ui
npm run dev
