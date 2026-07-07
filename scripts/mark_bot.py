#!/usr/bin/env python3
"""
晚山信箱 - 机器人内容标记脚本
==============================
将机器人发布的内容标记 is_bot=1，便于在数据库中区分真实内容和机器人内容。

使用方式:
  python3 mark_bot.py              # 标记所有机器人内容
  python3 mark_bot.py --undo       # 取消标记
"""

import sys
import os
import subprocess

# SSH 配置
SSH_KEY = os.environ.get("DEPLOY_KEY", "/tmp/deploy_key")
SERVER = os.environ.get("DEPLOY_SERVER", "ubuntu@132.232.154.56")
MYSQL_PASSWORD = "szKZsJ47hPGNl5l"
DB = "wanshan_mailbox"

UNDO = "--undo" in sys.argv
SET_VALUE = "0" if UNDO else "1"
ACTION = "取消标记" if UNDO else "标记"

def run_ssh(cmd):
    """执行远程MySQL命令"""
    full_cmd = (
        f'ssh -o StrictHostKeyChecking=no -i {SSH_KEY} {SERVER} '
        f'"sudo docker exec wanshan-mysql mysql -uroot -p{MYSQL_PASSWORD} {DB} '
        f'--default-character-set=utf8mb4 -e \\"{cmd}\\""'
    )
    result = subprocess.run(full_cmd, shell=True, capture_output=True, text=True)
    return result.stdout, result.stderr

# 标记所有 is_bot 为 NULL 或 0 的帖子（即所有当前帖子）
# 注意：这里我们标记所有帖子，因为我们目前都是机器人发的
# 后续有真实用户时，需要改为标记特定ID
stdout, stderr = run_ssh(f"UPDATE posts SET is_bot = {SET_VALUE}; SELECT ROW_COUNT() AS affected;")
print(stdout)

stdout, stderr = run_ssh(f"UPDATE comments SET is_bot = {SET_VALUE}; SELECT ROW_COUNT() AS affected;")
print(stdout)

print(f"✅ 已{ACTION}所有帖子和评论的 is_bot 字段")
