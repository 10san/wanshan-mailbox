<template>
  <div class="min-h-screen flex flex-col">
    <header class="sticky top-0 z-10 bg-white/80 backdrop-blur-md border-b border-warm-100">
      <div class="max-w-3xl mx-auto px-4 h-14 flex items-center gap-3">
        <button @click="$router.back()" class="p-2 text-dusk-500 text-lg">←</button>
        <input v-model="keyword" @keyup.enter="search" placeholder="搜索心事..." autofocus
          class="flex-1 px-4 py-2 rounded-full border border-warm-200 focus:outline-none focus:border-warm-400 text-sm bg-white" />
        <button @click="search" class="p-2 text-dusk-500 text-lg">🔍</button>
      </div>
    </header>

    <main class="flex-1 max-w-3xl mx-auto px-4 py-6 w-full">
      <div v-if="searching" class="text-center py-10 text-dusk-400">搜索中...</div>
      <div v-else-if="searched && results.length === 0" class="flex flex-col items-center justify-center py-20">
        <div class="text-5xl mb-3">🔍</div>
        <p class="text-dusk-400 text-lg">没有找到相关内容</p>
        <p class="text-dusk-300 text-sm mt-1">试试换个关键词</p>
      </div>
      <div v-else class="space-y-4">
        <p v-if="searched && results.length > 0" class="text-sm text-dusk-400 mb-2">找到 {{ results.length }} 条结果</p>
        <article v-for="post in results" :key="post.id" class="post-card cursor-pointer" @click="$router.push(`/post/${post.id}`)">
          <p class="text-dusk-700 leading-relaxed line-clamp-4 mb-3">{{ post.content }}</p>
          <div class="flex items-center justify-between text-sm text-dusk-400">
            <span>{{ formatTime(post.createdAt) }}</span>
          </div>
        </article>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { searchPosts } from '@/api/posts'

const keyword = ref('')
const results = ref([])
const searched = ref(false)
const searching = ref(false)

const formatTime = (t) => {
  if (!t) return ''
  const d = new Date(t), now = new Date()
  const diff = Math.floor((now - d) / 1000)
  if (diff < 60) return '刚刚'
  if (diff < 3600) return Math.floor(diff / 60) + '分钟前'
  if (diff < 86400) return Math.floor(diff / 3600) + '小时前'
  return d.toLocaleDateString('zh-CN')
}

const search = async () => {
  if (!keyword.value.trim()) return
  searching.value = true
  try {
    const res = await searchPosts(keyword.value.trim())
    const data = res.data || res
    results.value = data.records || []
    searched.value = true
  } catch (e) { results.value = []; searched.value = true }
  finally { searching.value = false }
}
</script>
