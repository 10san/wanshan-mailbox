<template>
  <div class="min-h-screen flex flex-col">
    <header class="sticky top-0 z-10 bg-white/80 backdrop-blur-md border-b border-warm-100">
      <div class="max-w-3xl mx-auto px-4 h-14 flex items-center gap-3">
        <button @click="$router.back()" class="p-2 text-dusk-500">←</button>
        <input v-model="keyword" @keyup.enter="search" placeholder="搜索心事..." 
          class="flex-1 px-4 py-2 rounded-full border border-warm-200 focus:outline-none focus:border-warm-400 text-sm" />
        <button @click="search" class="p-2 text-dusk-500">🔍</button>
      </div>
    </header>

    <main class="flex-1 max-w-3xl mx-auto px-4 py-6 w-full">
      <div v-if="searched && results.length === 0" class="flex flex-col items-center justify-center py-20">
        <p class="text-dusk-400 text-lg">没有找到相关内容</p>
      </div>

      <div v-else class="space-y-4">
        <article v-for="post in results" :key="post.id" class="post-card cursor-pointer" @click="$router.push(`/post/${post.id}`)">
          <p class="text-dusk-700 leading-relaxed line-clamp-4 mb-3">{{ post.content }}</p>
          <div class="flex items-center justify-between text-sm text-dusk-400">
            <span>{{ post.createdAt }}</span>
          </div>
        </article>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import api from '@/api'

const keyword = ref('')
const results = ref([])
const searched = ref(false)

const search = async () => {
  if (!keyword.value.trim()) return
  try {
    const res = await api.get('/search', { params: { q: keyword.value } })
    results.value = res.data.records || []
    searched.value = true
  } catch (e) {
    results.value = []
    searched.value = true
  }
}
</script>
