#!/bin/bash
# 数据库初始化脚本 - 在服务器上执行
# 用法: bash init-db.sh

MYSQL_PASSWORD="szKZsJ47hPGNl5l"

echo "等待 MySQL 启动..."
until docker exec wanshan-mysql mysqladmin ping -h localhost --silent 2>/dev/null; do
  sleep 2
done
echo "MySQL 已就绪，开始初始化..."

# 执行建表 SQL
docker exec -i wanshan-mysql mysql -uroot -p"${MYSQL_PASSWORD}" wanshan_mailbox < backend/src/main/resources/init.sql

echo "数据库初始化完成！"
echo "管理后台: http://你的IP/admin"
echo "默认账号: admin"
echo "默认密码: admin123"
echo "⚠️  请立即登录修改默认密码！"
