<template>
  <div class="min-h-screen flex flex-col">
    <header class="sticky top-0 z-10 bg-white/80 backdrop-blur-md border-b border-warm-100">
      <div class="max-w-3xl mx-auto px-4 h-14 flex items-center justify-between">
        <h1 class="text-xl font-serif font-semibold text-dusk-800">🌙 晚山信箱</h1>
        <div class="flex items-center gap-2">
          <router-link to="/feedback" class="p-2 text-dusk-500 hover:text-dusk-700 text-sm" title="意见反馈">💬</router-link>
          <router-link to="/search" class="p-2 text-dusk-500 hover:text-dusk-700 text-lg" title="搜索">🔍</router-link>
          <button class="p-2 text-dusk-500 hover:text-dusk-700 text-lg" @click="toggleDark" :title="isDark ? '日间模式' : '夜间模式'">
            {{ isDark ? '☀️' : '🌙' }}
          </button>
        </div>
      </div>
    </header>

    <!-- Hero 引导区：首次访问展示 -->
    <transition name="hero-fade">
      <section v-if="showHero" class="hero-section relative overflow-hidden">
        <!-- 背景装饰 -->
        <div class="absolute inset-0 pointer-events-none">
          <div class="hero-bg-circle hero-bg-circle-1"></div>
          <div class="hero-bg-circle hero-bg-circle-2"></div>
          <div class="hero-bg-circle hero-bg-circle-3"></div>
        </div>
        <div class="relative max-w-3xl mx-auto px-4 py-8 md:py-12 text-center">
          <div class="hero-content-anim">
            <div class="text-4xl md:text-5xl mb-4 hero-icon-anim">🌙</div>
            <h2 class="text-2xl md:text-3xl font-serif font-bold text-dusk-800 mb-3">把心事留在晚山</h2>
            <p class="text-dusk-500 text-sm md:text-base leading-relaxed max-w-md mx-auto mb-6">
              这里没有人知道你是谁<br class="md:hidden">
              只有山风会替你保守秘密<br>
              每一封信都是一颗星星<br class="md:hidden">
              在黑夜里安静地发光
            </p>
            <div class="flex items-center justify-center gap-4 mb-6">
              <div class="flex items-center gap-1.5 text-xs text-dusk-400">
                <span>🔒</span><span>完全匿名</span>
              </div>
              <div class="w-px h-3 bg-warm-200"></div>
              <div class="flex items-center gap-1.5 text-xs text-dusk-400">
                <span>📝</span><span>无需注册</span>
              </div>
              <div class="w-px h-3 bg-warm-200"></div>
              <div class="flex items-center gap-1.5 text-xs text-dusk-400">
                <span>💝</span><span>温暖回应</span>
              </div>
            </div>
            <router-link to="/new" class="hero-cta inline-block no-underline">
              写下你的第一封信 ✍️
            </router-link>
            <p class="text-xs text-dusk-400 mt-4 cursor-pointer hover:text-dusk-500 transition-colors" @click="dismissHero">
              向下滑动看看大家的故事 ↓
            </p>
          </div>
        </div>
      </section>
    </transition>

    <!-- 收起后的紧凑引导条 -->
    <transition name="hero-fade">
      <div v-if="!showHero && showCompactBar" class="compact-hero-bar">
        <div class="max-w-3xl mx-auto px-4 h-10 flex items-center justify-between">
          <p class="text-sm text-dusk-600 font-serif">🌙 把心事留在晚山，没有人知道你是谁</p>
          <button @click="dismissCompactBar" class="text-dusk-400 hover:text-dusk-600 text-sm">✕</button>
        </div>
      </div>
    </transition>

    <nav class="sticky top-14 z-10 bg-white/80 backdrop-blur-md border-b border-warm-100">
      <div class="max-w-3xl mx-auto px-4 py-3 flex gap-2 overflow-x-auto scrollbar-hide">
        <button v-for="tag in tags" :key="tag.value" @click="switchTag(tag.value)"
          :class="['tag-chip whitespace-nowrap shrink-0', { active: activeTag === tag.value }]">
          {{ tag.label }}
        </button>
      </div>
      <div class="flex items-center gap-2 px-1">
        <button @click="sortBy = 'latest'; fetchPosts(true)" :class="['text-xs px-2 py-1 rounded-full transition-colors', sortBy === 'latest' ? 'bg-warm-100 text-warm-600' : 'text-dusk-400']">最新</button>
        <button @click="sortBy = 'hot'; fetchPosts(true)" :class="['text-xs px-2 py-1 rounded-full transition-colors', sortBy === 'hot' ? 'bg-warm-100 text-warm-600' : 'text-dusk-400']">最热</button>
      </div>
    </nav>

    <main class="flex-1 max-w-3xl mx-auto px-4 py-6 w-full">
      <div v-if="error" class="text-center py-20">
        <p class="text-dusk-400 mb-4">{{ error }}</p>
        <button @click="fetchPosts(true)" class="btn-primary text-sm">点击重试</button>
      </div>

      <div v-else-if="loading" class="space-y-4">
        <div v-for="i in 5" :key="i" class="post-card animate-pulse">
          <div class="h-4 bg-warm-100 rounded w-3/4 mb-3"></div>
          <div class="h-4 bg-warm-100 rounded w-1/2 mb-4"></div>
          <div class="flex gap-4"><div class="h-3 bg-warm-100 rounded w-12"></div><div class="h-3 bg-warm-100 rounded w-12"></div></div>
        </div>
      </div>

      <div v-else-if="posts.length === 0" class="flex flex-col items-center justify-center py-20">
        <div class="text-6xl mb-4">🌙</div>
        <p class="text-dusk-400 text-lg">还没有心事，来做第一个吧</p>
        <router-link to="/new" class="btn-primary mt-6 inline-block no-underline">写下心事</router-link>
      </div>

      <div v-else class="space-y-4">
        <article v-for="post in posts" :key="post.id" class="post-card cursor-pointer" @click="$router.push(`/post/${post.id}`)">
          <div class="flex items-center gap-2 mb-2">
            <span class="w-6 h-6 rounded-full inline-block flex-shrink-0" :style="{ backgroundColor: avatarColor(post.id) }"></span>
            <span class="text-sm text-dusk-500">{{ nickname(post.id) }}</span>
          </div>
          <p class="text-dusk-700 leading-relaxed line-clamp-4 mb-3">{{ post.content }}</p>
          <div v-if="post.tag" class="mb-3">
            <span class="tag-chip text-xs">{{ tagLabel(post.tag) }}</span>
          </div>
          <div class="flex items-center justify-between text-sm text-dusk-400">
            <div class="flex items-center gap-4">
              <span>❤️ {{ post.likeCount || 0 }}</span>
              <span>💬 {{ post.commentCount || 0 }}</span>
            </div>
            <span>{{ formatTime(post.createdAt) }}</span>
          </div>
        </article>
      </div>

      <div v-if="hasMore && !loading && posts.length > 0" class="text-center py-6">
        <button @click="loadMore" class="text-warm-500 hover:text-warm-600 font-medium">加载更多</button>
      </div>
      <div v-if="!hasMore && posts.length > 0" class="text-center py-6 text-dusk-400 text-sm">没有更多了 🌙</div>
    </main>

    <router-link to="/new" class="fixed bottom-6 right-6 w-14 h-14 bg-warm-500 text-white rounded-full shadow-lg flex items-center justify-center text-2xl hover:bg-warm-600 active:scale-95 transition-all z-20 no-underline">
      ✏️
    </router-link>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { getPosts } from '@/api/posts'

const posts = ref([])
const loading = ref(true)
const error = ref('')
const activeTag = ref('all')
const sortBy = ref('latest')
const page = ref(1)
const hasMore = ref(true)
const isDark = ref(false)

// ====== Hero 引导状态 ======
const FIRST_VISIT_KEY = 'wanshan_visited'
const COMPACT_DISMISS_KEY = 'wanshan_compact_dismissed'
const showHero = ref(false)
const showCompactBar = ref(false)
let scrollHandler = null

const dismissHero = () => {
  showHero.value = false
  showCompactBar.value = true
  localStorage.setItem(FIRST_VISIT_KEY, '1')
}

const dismissCompactBar = () => {
  showCompactBar.value = false
  localStorage.setItem(COMPACT_DISMISS_KEY, '1')
}

onMounted(() => {
  const hasVisited = localStorage.getItem(FIRST_VISIT_KEY)
  const compactDismissed = localStorage.getItem(COMPACT_DISMISS_KEY)

  if (!hasVisited) {
    showHero.value = true
    showCompactBar.value = false
  } else if (!compactDismissed) {
    showHero.value = false
    showCompactBar.value = true
  }

  // 监听滚动：下滑超过 Hero 区域后自动收起
  scrollHandler = () => {
    if (showHero.value && window.scrollY > 200) {
      showHero.value = false
      showCompactBar.value = true
      localStorage.setItem(FIRST_VISIT_KEY, '1')
    }
  }
  window.addEventListener('scroll', scrollHandler, { passive: true })

  const hour = new Date().getHours()
  if (hour >= 18 || hour < 6) { isDark.value = true; document.documentElement.classList.add('dark') }
  fetchPosts(true)
})

onUnmounted(() => {
  if (scrollHandler) window.removeEventListener('scroll', scrollHandler)
})

const tags = [
  { label: '全部', value: 'all' },{ label: '🌙 深夜emo', value: '深夜emo' },
  { label: '💔 情感树洞', value: '情感树洞' },{ label: '🏠 家庭琐事', value: '家庭琐事' },
  { label: '💼 职场压力', value: '职场压力' },{ label: '📚 学业烦恼', value: '学业烦恼' },
  { label: '🌱 自我成长', value: '自我成长' },{ label: '💬 日常吐槽', value: '日常吐槽' },
  { label: '🤝 人际关系', value: '人际关系' },
]

const AVATARS = ['#FF7F1A','#E0680A','#2E86C1','#6F42C1','#0E8A16','#D93F0B','#876B4A']
const NICKNAMES = ['安静的考拉','勇敢的向日葵','温柔的星辰','孤独的山风','倔强的大海','迷茫的月亮','自由的小熊','热烈的狐狸','慢热的猫咪','敏感的鲸鱼']

const avatarColor = (id) => AVATARS[id % AVATARS.length]
const nickname = (id) => NICKNAMES[id % NICKNAMES.length]
const tagLabel = (v) => tags.find(t => t.value === v)?.label || v

const formatTime = (t) => {
  if (!t) return ''
  const d = new Date(t), now = new Date()
  const diff = Math.floor((now - d) / 1000)
  if (diff < 60) return '刚刚'
  if (diff < 3600) return Math.floor(diff / 60) + '分钟前'
  if (diff < 86400) return Math.floor(diff / 3600) + '小时前'
  if (diff < 604800) return Math.floor(diff / 86400) + '天前'
  return d.toLocaleDateString('zh-CN')
}

const fetchPosts = async (reset = false) => {
  if (reset) { page.value = 1; posts.value = [] }
  loading.value = true; error.value = ''
  try {
    const params = { page: page.value, size: 20, sort: sortBy.value }
    if (activeTag.value !== 'all') params.tag = activeTag.value
    const res = await getPosts(params)
    const data = res.data || res
    const records = data.records || []
    if (reset) posts.value = records
    else posts.value.push(...records)
    hasMore.value = data.hasMore !== undefined ? data.hasMore : records.length === 20
  } catch (e) {
    error.value = '加载失败，请检查网络后重试'
  } finally { loading.value = false }
}

const switchTag = (tag) => {
  activeTag.value = tag
  fetchPosts(true)
}

const loadMore = () => { page.value++; fetchPosts() }

const toggleDark = () => {
  isDark.value = !isDark.value
  document.documentElement.classList.toggle('dark', isDark.value)
}
</script>
