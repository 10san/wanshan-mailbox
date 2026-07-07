#!/bin/bash
# MinIO 初始化脚本 - 创建 bucket
# 在 MinIO 容器启动后自动执行

sleep 5

# 配置 MinIO 客户端
mc alias set local http://localhost:9000 minioadmin iwQM8foyH9xhQ7sW

# 创建 bucket（如果不存在）
mc mb local/wanshan-images --ignore-existing

# 设置 bucket 为公开下载
mc anonymous set download local/wanshan-images

echo "MinIO bucket 'wanshan-images' initialized."
