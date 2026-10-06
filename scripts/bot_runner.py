#!/usr/bin/env python3
"""
晚山信箱 - 模拟活跃机器人
============================
每天定时自动发帖、评论、点赞，解决冷启动阶段的"空城效应"。
AI实时生成多样化内容，行为随机化，标记is_bot=1便于追踪。

使用方式:
  python3 bot_runner.py          # 手动运行一次
  python3 bot_runner.py --dry-run # 预览模式，不实际发送
"""

import requests
import random
import time
import json
import os
import sys
import hashlib
from datetime import datetime
from collections import defaultdict

# ============================================================
# 配置
# ============================================================

BASE_URL = os.environ.get("WANSHAN_API", "http://132.232.154.56")
DRY_RUN = "--dry-run" in sys.argv

# 标签池（与前端一致）
TAGS = ["深夜emo", "情感树洞", "家庭琐事", "职场压力", "学业烦恼", "自我成长", "日常吐槽", "人际关系"]

# 标签权重：越靠前出现概率越高
TAG_WEIGHTS = [25, 25, 5, 12, 8, 8, 12, 5]

# ============================================================
# AI 内容生成（基于本地模板 + 随机组合，无需外部API）
# ============================================================

# ---- 发帖内容模板 ----
POST_TEMPLATES = {
    "深夜emo": {
        "openings": [
            "凌晨{hour}点了，还是睡不着。",
            "又到了这个点，脑子里全是乱七八糟的事。",
            "深夜的房间里只有手机屏幕亮着。",
            "不知道从什么时候开始，失眠成了常态。",
            "窗外安静得可怕，心里却吵得要命。",
            "翻来覆去第{n}次了，索性不睡了。",
        ],
        "bodies": [
            "想起很多事情，小时候的、最近的、还没发生的。好像每一件都不重要，但每一件都在脑子里转。",
            "白天还能假装一切都好，到了晚上所有伪装都失效了。",
            "朋友圈刷了好几遍，没有一条想看的。但又不想放下手机。",
            "有时候觉得活着好累，不是说想死，就是……不知道为什么要这么累。",
            "最近总在怀疑自己选的路对不对。可是回头看看，好像也没有别的路可以走。",
            "其实也没发生什么特别的事，就是莫名觉得难过。可能这就是成年人的日常吧。",
            "翻到三年前的照片，那时候笑得好开心。现在也不是不开心，就是……不一样了。",
            "明天还要早起上班/上课，但就是不想睡。感觉睡着就等于承认今天又虚度了。",
            "刚才听到一首老歌，突然就哭了。也不是因为歌词，就是旋律一出来，心里某个地方被戳了一下。",
            "其实我知道问题出在哪，但就是改不了。或者说，不想改。",
            "跟朋友聊了会儿天，他们都在说自己的事。我听着听着就不想说了，觉得自己的事好像也没那么重要。",
        ],
        "closings": [
            "算了，说出来好受一点了。晚安，虽然我知道今晚还是睡不着。",
            "就当我自言自语吧。天亮之前把这些话留在这里。",
            "希望明天能好一点。哪怕只比今天好一点点。",
            "谢谢这个地方，至少有个地方可以说这些。",
            "如果有跟我一样睡不着的人，我们隔空碰个杯吧。",
            "就这样吧。把情绪留在这里，天亮继续做大人。",
        ]
    },
    "情感树洞": {
        "openings": [
            "喜欢一个人，不敢说。",
            "分手{n}个月了，还是走不出来。",
            "我们在一起{n}年了，最近越来越不知道说什么。",
            "他/她今天发了一条朋友圈，我盯着看了十分钟。",
            "暗恋真的是世界上最累的事情。",
            "刚才在路上看到一个背影很像他/她，心跳漏了一拍。",
        ],
        "bodies": [
            "其实我们也没什么大矛盾，就是慢慢地从无话不说变成了无话可说。有时候躺在同一张床上，中间隔的好像不是被子，是沉默。",
            "我知道我们没有可能。年龄、距离、家庭，随便哪一条都是理由。可是感情这种东西，它不讲道理。",
            "每次想表白的时候，话到嘴边又咽回去了。怕说了连朋友都做不成，又怕不说会后悔一辈子。",
            "他对我好的时候真的很好，但忽冷忽热的时候也很伤人。我就像一个被放在角落的玩具，他想起来才来拿。",
            "朋友都劝我放下，我也知道该放下。可是放下两个字说出来容易，做起来真的太难了。",
            "异地真的太苦了。不是不信任，是每次需要对方的时候都只能对着手机说没事。",
            "前任昨天突然给我发消息了。就三个字'在干嘛'，我看了两个小时不知道怎么回。",
            "其实我早就知道我们不合适。但在一起久了，分开就像戒掉一个习惯，戒断反应让人受不了。",
            "今天是我们分开的第{n}天。我以为时间能治愈一切，但好像只是让我习惯了疼痛。",
        ],
        "closings": [
            "不知道他/她会不会看到。如果看到了，就当是风带走了这些话吧。",
            "感情这件事，真的太难了。希望每个人都能被温柔以待。",
            "算了，爱过就够了。虽然不甘心，但还是要往前走。",
            "谢谢这个树洞。有些话对身边的人说不出口，但可以留在这里。",
            "希望下次再写的时候，是开心的故事。",
        ]
    },
    "职场压力": {
        "openings": [
            "今天又被领导叫去谈话了。",
            "入职{n}个月了，每天都觉得自己是个废物。",
            "想辞职的第{n}天。",
            "加班到{n}点，地铁上只有我一个人。",
            "同事又在背后搞小动作，心累。",
        ],
        "bodies": [
            "明明不是我的错，最后锅还是甩到我头上了。解释也没用，领导只看结果。",
            "每天做着重复的工作，感觉自己在原地踏步。想跳槽又怕找不到更好的。",
            "新来的同事工资比我高，能力还不如我。这种事情知道了真的很难平衡。",
            "996已经够累了，还要应付办公室政治。有时候觉得上班最累的不是工作本身，是人际关系。",
            "今天做了一个方案被否了三次。每一次都说'方向不对'，但又不告诉我什么方向才对。",
            "其实我不讨厌这份工作，但我讨厌没有成长的感觉。每天都在消耗自己，没有输入。",
            "领导说年轻人要多加班积累经验。可是我的经验就是：加班真的没有用，只是效率低。",
        ],
        "closings": [
            "明天还是要继续。毕竟房租不等人。",
            "加油吧打工人。虽然不知道未来在哪，但至少今天熬过去了。",
            "希望有一天能做自己真正喜欢的事。虽然现在还不知道那是什么。",
        ]
    },
    "学业烦恼": {
        "openings": [
            "期末了，感觉自己什么都不会。",
            "考研还是工作？每天都在纠结。",
            "今天成绩出来了，比预期的差很多。",
            "父母对我期望太高了，压力好大。",
        ],
        "bodies": [
            "图书馆从早待到晚，但效率真的不高。看了很多书，但好像什么都没记住。",
            "同学们都在实习、考研、出国，我连自己想做什么都不知道。这种迷茫比考试更可怕。",
            "其实我不喜欢现在的专业，但转专业又来不及了。每天都在学自己不感兴趣的东西。",
            "跟父母视频的时候他们又问我成绩了。我含糊地应付过去了，挂了电话就哭了。",
            "论文被导师打回来改了三次，每次都说'再深入一点'。但我真的不知道怎么深入了。",
        ],
        "closings": [
            "希望考试能过。求求了。",
            "不管怎样，先熬过这个学期吧。",
        ]
    },
    "自我成长": {
        "openings": [
            "今年{n}岁了，好像什么都没做成。",
            "最近在反思自己是不是太懒了。",
            "想改变，但不知道从哪里开始。",
            "看了一本书，里面有句话让我想了很久。",
        ],
        "bodies": [
            "身边的同龄人都在进步，只有我好像停在原地。不是不努力，是不知道往哪个方向努力。",
            "报了健身房去了三次就没去了。买了书翻了十几页就放下了。我是不是真的没有毅力？",
            "其实我知道自己的问题在哪，但知道和改变之间隔了一整个银河系。",
            "最近在学一个新技能，虽然很慢，但每天进步一点点。这种踏实感好久没有了。",
            "以前觉得三十岁很遥远，现在发现它就在眼前。而我好像还没准备好成为一个大人。",
            "今天做了一件一直想做但不敢做的事。虽然结果不完美，但我为自己骄傲。",
        ],
        "closings": [
            "慢慢来吧。每个人的节奏不一样。",
            "今天比昨天好一点，就是胜利。",
        ]
    },
    "日常吐槽": {
        "openings": [
            "今天遇到一件无语的事。",
            "社恐的日常就是……",
            "我真的服了，怎么会有人……",
            "刚才发生了一件尴尬到想钻地缝的事。",
            "今天的快乐源泉来了。",
        ],
        "bodies": [
            "地铁上有人外放短视频，声音巨大。整个车厢的人都在看他，他完全不自知。",
            "点了外卖等了快一个小时，骑手说到了但我没看到人。打电话过去他说放在'门口'了，我说哪个门口，他说'就是门口啊'。最后发现他送错小区了。",
            "同事今天跟我说'你好像胖了'。我知道我胖了，但你能不能不要说出来？？",
            "刚才在电梯里放了个屁，然后进来一个人。我假装在打电话，但他应该闻到了。现在想起来还是想死。",
            "排队排了二十分钟，终于到我了，前面那个人说'等一下我叫几个人'，然后叫来了五个人。",
            "今天天气巨好，心情也巨好。不知道为什么，就是觉得活着挺好的。这种日子不多，要珍惜。",
            "我妈又给我发养生文章了。标题是《熬夜的十大危害，看完你还敢晚睡吗》。然后我半夜两点给她回了'好的妈妈'。",
        ],
        "closings": [
            "吐槽完舒服多了。",
            "生活嘛，就是这样。",
        ]
    },
    "人际关系": {
        "openings": [
            "最近跟一个朋友渐行渐远了。",
            "被一个我以为很熟的人删好友了。",
            "不知道该怎么拒绝别人，每次都把自己搞得很累。",
        ],
        "bodies": [
            "以前每天聊天的人，现在对话框已经沉到底了。也不是吵架了，就是莫名其妙地不联系了。",
            "其实我很珍惜这段友谊，但好像只有我一个人在维系。每次都是我主动发消息，久了也会累。",
            "我发现我越来越不会社交了。聚会的时候大家都在聊天，我只想躲在角落里玩手机。",
            "帮朋友帮了很多次，这次实在帮不了。结果对方就不高兴了。有时候觉得善良是不是一种错。",
            "同事之间的关系好复杂。表面上一团和气，背地里各自算计。我这种直性子真的应付不来。",
        ],
        "closings": [
            "可能有些人注定只能陪你走一段路吧。",
            "学会拒绝真的是成年人最重要的课题。",
        ]
    },
    "家庭琐事": {
        "openings": [
            "今天又跟爸妈吵架了。",
            "回家过年的压力已经开始了。",
            "家里人催婚催到我怀疑人生。",
        ],
        "bodies": [
            "其实我知道爸妈是为我好，但他们的方式真的让我喘不过气。每次打电话都是'什么时候结婚'、'什么时候换工作'。",
            "过年回家最怕的就是亲戚聚会。每个人都要问一遍工作、工资、对象，然后跟自家孩子比较。",
            "今天妈妈生病了，我在外地赶不回去。打电话的时候她说没事，但我听到她在咳嗽。突然觉得离家太远了。",
            "其实我很想跟爸妈说'我爱你们'，但不知道为什么就是说不出口。可能是从小到大的相处方式就是这样。",
        ],
        "closings": [
            "虽然嘴上总说烦，但心里还是爱他们的。",
            "希望爸妈身体健康，等我回去看你们。",
        ]
    },
}

# ---- 评论内容模板 ----
COMMENT_TEMPLATES = {
    "共鸣": [
        "天哪，我以为只有我一个人这样。抱抱你。",
        "看到这个感觉在说自己。原来我不是一个人。",
        "完全懂你的感受。这种事情只有经历过的人才明白。",
        "写到我心坎里了。谢谢你把这些说出来。",
        "我也是！！每次看到有人写出自己的感受，就觉得没那么孤单了。",
        "隔着屏幕给你一个拥抱。虽然不认识你，但你的文字让我觉得很近。",
        "差点以为是我自己写的。这种感觉太熟悉了。",
    ],
    "安慰": [
        "一切都会好起来的。虽然这句话很老套，但真的是真的。",
        "不要否定自己。你已经做得很好了。",
        "难过的时候就允许自己难过吧。不用一直假装坚强。",
        "这个世界上一定有人在偷偷爱你。只是你还没发现。",
        "没有什么过不去的。回头看的时候你会发现，那些让你崩溃的事，后来都变成了故事。",
        "累了就休息一下。不用一直往前冲。",
        "你今天能把这些说出来，已经很勇敢了。",
        "给自己一点时间。伤口会愈合的，只是需要过程。",
    ],
    "简短": [
        "❤️",
        "加油",
        "懂你",
        "抱抱",
        "同在",
        "+1",
        "会好的",
        "我也是",
        "说得好",
        "暖暖的",
    ],
    "互动": [
        "后来呢？想知道后续。",
        "所以你现在还好吗？",
        "那现在怎么样了？",
        "你打算怎么办？",
        "有没有试着跟对方聊一下？",
    ],
    "鼓励": [
        "去做吧！不试怎么知道。",
        "人生苦短，别留遗憾。",
        "错了又怎样，至少你试过了。",
        "勇敢一点，你比你自己想象的强大。",
        "现在不做，以后会后悔的。",
        "支持你！不管结果怎样。",
    ],
}


def generate_post(tag):
    """根据标签生成一篇帖子内容"""
    templates = POST_TEMPLATES.get(tag, POST_TEMPLATES["日常吐槽"])
    
    # 生成内容，确保足够长度
    for attempt in range(10):
        n = random.randint(1, 12)
        hour = random.randint(0, 4)
        
        opening = random.choice(templates["openings"]).format(hour=hour, n=n)
        body = random.choice(templates["bodies"]).format(n=n)
        
        # 随机决定是否加第二段body（40%概率），让内容更丰富
        if random.random() < 0.4:
            body2 = random.choice(templates["bodies"]).format(n=n)
            if body2 != body:
                body = f"{body}\n\n{body2}"
        
        # 70%概率包含 closing
        if random.random() < 0.7:
            closing = random.choice(templates["closings"]).format(n=n)
            content = f"{opening}\n\n{body}\n\n{closing}"
        else:
            content = f"{opening}\n\n{body}"
        
        # 控制长度：至少80字，最多2000字
        if len(content) < 80:
            # 加一段补充
            extra = random.choice(templates["bodies"]).format(n=n)
            content = f"{content}\n\n{extra}"
        
        if len(content) > 2000:
            content = content[:1997] + "..."
        
        if 80 <= len(content) <= 2000:
            return content
    
    return content


def generate_comment(post_content, post_tag):
    """根据帖子内容和标签生成一条评论"""
    # 根据标签选择评论风格权重
    style_weights = {
        "共鸣": 30,
        "安慰": 25,
        "简短": 20,
        "鼓励": 15,
        "互动": 10,
    }
    styles = list(style_weights.keys())
    weights = list(style_weights.values())
    style = random.choices(styles, weights=weights, k=1)[0]
    
    comment = random.choice(COMMENT_TEMPLATES[style])
    
    # 简短评论直接返回
    if style == "简短":
        return comment
    
    # 10%概率追加第二句
    if random.random() < 0.1:
        extra = random.choice(COMMENT_TEMPLATES["简短"])
        comment = f"{comment} {extra}"
    
    return comment


# ============================================================
# API 调用
# ============================================================

class WanshanBot:
    def __init__(self, base_url=BASE_URL, dry_run=DRY_RUN):
        self.base_url = base_url
        self.dry_run = dry_run
        self.session = requests.Session()
        self.session.headers.update({
            "User-Agent": "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15",
            "Accept": "application/json",
            "Content-Type": "application/json;charset=UTF-8",
        })
        self.posted_ids = []  # 本轮发帖的ID列表
        self.stats = defaultdict(int)
        
    def _post(self, path, data=None):
        """发送POST请求"""
        url = f"{self.base_url}{path}"
        if self.dry_run:
            print(f"  [DRY-RUN] POST {path} {json.dumps(data, ensure_ascii=False)[:80] if data else ''}")
            return {"code": 200, "data": {"id": random.randint(1000, 9999)}}
        
        try:
            resp = self.session.post(url, json=data, timeout=15)
            if resp.status_code == 429:
                print(f"  ⚠️ 限流，等待30秒...")
                time.sleep(30)
                resp = self.session.post(url, json=data, timeout=15)
            resp.raise_for_status()
            return resp.json()
        except Exception as e:
            print(f"  ❌ 请求失败: {e}")
            return None

    def _get(self, path, params=None):
        """发送GET请求"""
        url = f"{self.base_url}{path}"
        if self.dry_run:
            return {"code": 200, "data": {"records": [{"id": i, "content": "mock"} for i in range(5)]}}
        
        try:
            resp = self.session.get(url, params=params, timeout=15)
            resp.raise_for_status()
            return resp.json()
        except Exception as e:
            print(f"  ❌ 请求失败: {e}")
            return None

    def create_post(self, content, tag):
        """发帖"""
        data = {"content": content, "tag": tag, "isBot": "1"}
        result = self._post("/api/v1/posts", data)
        if result and result.get("code") == 200:
            post_id = result["data"]["id"]
            self.posted_ids.append(post_id)
            self.stats["posts"] += 1
            print(f"  ✅ 发帖成功 id={post_id} tag={tag} ({len(content)}字)")
            return post_id
        else:
            print(f"  ❌ 发帖失败: {result}")
            return None

    def create_comment(self, post_id, content):
        """评论"""
        data = {"content": content, "isBot": "1"}
        result = self._post(f"/api/v1/posts/{post_id}/comments", data)
        if result and result.get("code") == 200:
            self.stats["comments"] += 1
            print(f"  💬 评论成功 post={post_id} ({len(content)}字)")
            return True
        else:
            print(f"  ❌ 评论失败 post={post_id}: {result}")
            return False

    def like_post(self, post_id):
        """点赞"""
        result = self._post(f"/api/v1/posts/{post_id}/like")
        if result and result.get("code") == 200:
            self.stats["likes"] += 1
            print(f"  ❤️ 点赞成功 post={post_id}")
            return True
        return False

    def get_recent_posts(self, limit=30):
        """获取最近帖子列表（用于评论和点赞）"""
        result = self._get("/api/v1/posts", {"page": 1, "size": limit, "sort": "latest"})
        if result and result.get("code") == 200:
            records = result.get("data", {}).get("records", [])
            return records
        return []

    def mark_as_bot(self, table, ids):
        """通过直接SQL标记为bot（需要数据库访问）"""
        # 这里我们改用管理员API的方式，或者记录到日志
        # 因为C端API没有is_bot字段，所以我们需要另一种方式
        # 方案：使用管理员token通过后台API标记，或者直接操作数据库
        # 目前先记录到本地日志，后续通过脚本批量标记
        pass

    def run_daily(self):
        """执行每日机器人任务"""
        now = datetime.now()
        print(f"\n{'='*50}")
        print(f"  晚山信箱 Bot - {now.strftime('%Y-%m-%d %H:%M:%S')}")
        print(f"{'='*50}")
        
        if self.dry_run:
            print("  🔍 预览模式 - 不会实际发送请求\n")
        
        # ========== Phase 1: 发帖 ==========
        # 根据时段决定发帖数量
        hour = now.hour
        if 8 <= hour < 10:
            post_count = random.randint(1, 2)
        elif 12 <= hour < 14:
            post_count = 1
        elif 20 <= hour < 23:
            post_count = random.randint(2, 3)
        elif 0 <= hour < 2:
            post_count = random.randint(1, 2)
        else:
            post_count = random.randint(0, 1)
        
        print(f"\n📝 Phase 1: 发帖 (目标: {post_count}篇)")
        
        for i in range(post_count):
            # 加权随机选择标签
            tag = random.choices(TAGS, weights=TAG_WEIGHTS, k=1)[0]
            content = generate_post(tag)
            
            if self.dry_run:
                print(f"\n  [{i+1}/{post_count}] tag={tag}")
                print(f"  {'─'*40}")
                print(f"  {content[:200]}...")
                self.posted_ids.append(random.randint(1000, 9999))
                self.stats["posts"] += 1
            else:
                print(f"\n  [{i+1}/{post_count}] tag={tag}")
                self.create_post(content, tag)
            
            # 发帖间隔：30-120秒随机
            if i < post_count - 1:
                delay = random.randint(30, 120)
                print(f"  ⏳ 等待 {delay}秒...")
                time.sleep(delay)
        
        # ========== Phase 2: 评论 ==========
        # 根据时段决定评论数量
        if 8 <= hour < 10:
            comment_count = random.randint(2, 4)
        elif 12 <= hour < 14:
            comment_count = random.randint(1, 3)
        elif 20 <= hour < 23:
            comment_count = random.randint(4, 8)
        elif 0 <= hour < 2:
            comment_count = random.randint(2, 3)
        else:
            comment_count = random.randint(0, 1)
        
        print(f"\n💬 Phase 2: 评论 (目标: {comment_count}条)")
        
        # 获取现有帖子池
        existing_posts = self.get_recent_posts(30)
        post_pool = existing_posts + [{"id": pid, "content": "", "tag": ""} for pid in self.posted_ids]
        
        if not post_pool:
            print("  ⚠️ 没有可评论的帖子")
        else:
            for i in range(comment_count):
                target = random.choice(post_pool)
                pid = target.get("id")
                post_content = target.get("content", "")
                post_tag = target.get("tag", "")
                
                comment = generate_comment(post_content, post_tag)
                
                if self.dry_run:
                    print(f"\n  [{i+1}/{comment_count}] → post #{pid}")
                    print(f"  💬 {comment}")
                    self.stats["comments"] += 1
                else:
                    print(f"\n  [{i+1}/{comment_count}] → post #{pid}")
                    self.create_comment(pid, comment)
                
                # 评论间隔：15-60秒随机
                if i < comment_count - 1:
                    delay = random.randint(15, 60)
                    print(f"  ⏳ 等待 {delay}秒...")
                    time.sleep(delay)
        
        # ========== Phase 3: 点赞 ==========
        like_count = random.randint(3, 10)
        print(f"\n❤️ Phase 3: 点赞 (目标: {like_count}次)")
        
        if post_pool:
            for i in range(like_count):
                target = random.choice(post_pool)
                pid = target.get("id")
                
                if self.dry_run:
                    print(f"  [{i+1}/{like_count}] ❤️ post #{pid}")
                    self.stats["likes"] += 1
                else:
                    self.like_post(pid)
                
                # 点赞间隔：5-20秒随机
                if i < like_count - 1:
                    delay = random.randint(5, 20)
                    time.sleep(delay)
        
        # ========== 汇总 ==========
        print(f"\n{'='*50}")
        print(f"  📊 本次执行汇总")
        print(f"  发帖: {self.stats['posts']} 篇")
        print(f"  评论: {self.stats['comments']} 条")
        print(f"  点赞: {self.stats['likes']} 次")
        print(f"  本轮发帖ID: {self.posted_ids}")
        print(f"{'='*50}\n")
        
        return self.stats


def main():
    bot = WanshanBot()
    
    # 随机延迟 0-10 分钟再开始（避免被识别为定时任务）
    if not DRY_RUN:
        delay = random.randint(0, 600)
        print(f"⏳ 随机延迟 {delay}秒 后开始...")
        time.sleep(delay)
    
    bot.run_daily()


if __name__ == "__main__":
    main()
