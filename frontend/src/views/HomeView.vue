<template>
  <div class="min-h-screen flex flex-col">
    <!-- 顶部导航 -->
    <header class="sticky top-0 z-10 bg-white/80 backdrop-blur-md border-b border-warm-100">
      <div class="max-w-3xl mx-auto px-4 h-14 flex items-center justify-between">
        <h1 class="text-xl font-serif font-semibold text-dusk-800">🌙 晚山信箱</h1>
        <div class="flex items-center gap-3">
          <router-link to="/search" class="p-2 text-dusk-500 hover:text-dusk-700">
            🔍
          </router-link>
          <button class="p-2 text-dusk-500 hover:text-dusk-700" @click="toggleDark">
            {{ isDark ? '☀️' : '🌙' }}
          </button>
        </div>
      </div>
    </header>

    <!-- 标签筛选栏 -->
    <nav class="sticky top-14 z-10 bg-white/80 backdrop-blur-md border-b border-warm-100">
      <div class="max-w-3xl mx-auto px-4 py-3 flex gap-2 overflow-x-auto scrollbar-hide">
        <button
          v-for="tag in tags"
          :key="tag.value"
          @click="activeTag = tag.value"
          :class="['tag-chip whitespace-nowrap', { active: activeTag === tag.value }]"
        >
          {{ tag.label }}
        </button>
      </div>
    </nav>

    <!-- 帖子列表 -->
    <main class="flex-1 max-w-3xl mx-auto px-4 py-6 w-full">
      <!-- 骨架屏 -->
      <div v-if="loading" class="space-y-4">
        <div v-for="i in 5" :key="i" class="post-card animate-pulse">
          <div class="h-4 bg-warm-100 rounded w-3/4 mb-3"></div>
          <div class="h-4 bg-warm-100 rounded w-1/2 mb-4"></div>
          <div class="flex gap-4">
            <div class="h-3 bg-warm-100 rounded w-16"></div>
            <div class="h-3 bg-warm-100 rounded w-16"></div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else-if="posts.length === 0" class="flex flex-col items-center justify-center py-20">
        <div class="text-6xl mb-4">🌙</div>
        <p class="text-dusk-400 text-lg">还没有心事，来做第一个吧</p>
        <router-link to="/new" class="btn-primary mt-6 inline-block">写下心事</router-link>
      </div>

      <!-- 帖子卡片流 -->
      <div v-else class="space-y-4">
        <article v-for="post in posts" :key="post.id" class="post-card cursor-pointer" @click="goDetail(post.id)">
          <p class="text-dusk-700 leading-relaxed line-clamp-4 mb-3">{{ post.content }}</p>
          <div class="flex items-center gap-2 mb-3">
            <span v-if="post.tag" class="tag-chip text-xs">{{ tagLabel(post.tag) }}</span>
          </div>
          <div class="flex items-center justify-between text-sm text-dusk-400">
            <div class="flex items-center gap-4">
              <span>❤️ {{ post.likeCount || 0 }}</span>
              <span>💬 {{ post.commentCount || 0 }}</span>
            </div>
            <span>{{ post.createdAt }}</span>
          </div>
        </article>
      </div>

      <!-- 加载更多 -->
      <div v-if="hasMore && !loading" class="text-center py-6">
        <button @click="loadMore" class="text-warm-500 hover:text-warm-600 font-medium">加载更多</button>
      </div>
      <div v-if="!hasMore && posts.length > 0" class="text-center py-6 text-dusk-400 text-sm">
        没有更多了 🌙
      </div>
    </main>

    <!-- FAB 发帖按钮 -->
    <router-link to="/new" class="fixed bottom-6 right-6 w-14 h-14 bg-warm-500 text-white rounded-full shadow-lg flex items-center justify-center text-2xl hover:bg-warm-600 active:scale-95 transition-all z-20">
      ✏️
    </router-link>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api'

const router = useRouter()
const posts = ref([])
const loading = ref(true)
const activeTag = ref('all')
const page = ref(1)
const hasMore = ref(true)
const isDark = ref(false)

const tags = [
  { label: '全部', value: 'all' },
  { label: '🌙 深夜emo', value: '深夜emo' },
  { label: '💔 情感树洞', value: '情感树洞' },
  { label: '🏠 家庭琐事', value: '家庭琐事' },
  { label: '💼 职场压力', value: '职场压力' },
  { label: '📚 学业烦恼', value: '学业烦恼' },
  { label: '🌱 自我成长', value: '自我成长' },
  { label: '💬 日常吐槽', value: '日常吐槽' },
  { label: '🤝 人际关系', value: '人际关系' },
]

const tagLabel = (value) => tags.find(t => t.value === value)?.label || value

const fetchPosts = async (reset = false) => {
  if (reset) { page.value = 1; posts.value = [] }
  loading.value = true
  try {
    const params = { page: page.value, size: 20 }
    if (activeTag.value !== 'all') params.tag = activeTag.value
    const res = await api.get('/posts', { params })
    if (reset) {
      posts.value = res.data.records
    } else {
      posts.value.push(...res.data.records)
    }
    hasMore.value = res.data.hasMore
  } catch (e) {
    console.error('加载失败', e)
  } finally {
    loading.value = false
  }
}

const loadMore = () => {
  page.value++
  fetchPosts()
}

const goDetail = (id) => {
  router.push(`/post/${id}`)
}

const toggleDark = () => {
  isDark.value = !isDark.value
  document.documentElement.classList.toggle('dark', isDark.value)
}

onMounted(() => {
  // 自动检测夜间模式
  const hour = new Date().getHours()
  if (hour >= 18 || hour < 6) {
    isDark.value = true
    document.documentElement.classList.add('dark')
  }
  fetchPosts(true)
})
</script>
