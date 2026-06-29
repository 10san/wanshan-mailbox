<template>
  <div class="min-h-screen flex flex-col">
    <header class="sticky top-0 z-10 bg-white/80 backdrop-blur-md border-b border-warm-100">
      <div class="max-w-3xl mx-auto px-4 h-14 flex items-center gap-3">
        <button @click="$router.back()" class="p-2 text-dusk-500">←</button>
        <h1 class="text-lg font-medium text-dusk-800 flex-1">写心事</h1>
        <button @click="submit" :disabled="!content.trim() || submitting" class="btn-primary text-sm px-5 py-2">
          {{ submitting ? '发布中...' : '发布' }}
        </button>
      </div>
    </header>

    <main class="flex-1 max-w-3xl mx-auto px-4 py-6 w-full">
      <textarea v-model="content" placeholder="在这里写下你的心事..." maxlength="2000"
        class="w-full h-64 p-4 rounded-2xl border border-warm-200 focus:outline-none focus:border-warm-400 resize-none text-dusk-700 leading-relaxed" />

      <div class="flex items-center justify-between text-sm text-dusk-400 mt-2 mb-6">
        <span>{{ content.length }} / 2000</span>
      </div>

      <!-- 情绪标签 -->
      <div class="mb-6">
        <h3 class="text-sm font-medium text-dusk-600 mb-3">选择情绪标签（可选）</h3>
        <div class="flex flex-wrap gap-2">
          <button v-for="tag in tags" :key="tag.value" @click="selectedTag = tag.value"
            :class="['tag-chip', { active: selectedTag === tag.value }]">
            {{ tag.label }}
          </button>
        </div>
      </div>

      <!-- 删除密码 -->
      <div class="mb-6">
        <h3 class="text-sm font-medium text-dusk-600 mb-3">设置删除密码（可选）</h3>
        <input v-model="deletePassword" type="password" placeholder="设置后，凭此密码可删除帖子"
          class="w-full px-4 py-2 rounded-full border border-warm-200 focus:outline-none focus:border-warm-400 text-sm" />
        <p class="text-xs text-dusk-400 mt-1">不设置则只能由管理员删除</p>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api'

const router = useRouter()
const content = ref('')
const selectedTag = ref('')
const deletePassword = ref('')
const submitting = ref(false)

const tags = [
  { label: '🌙 深夜emo', value: '深夜emo' },
  { label: '💔 情感树洞', value: '情感树洞' },
  { label: '🏠 家庭琐事', value: '家庭琐事' },
  { label: '💼 职场压力', value: '职场压力' },
  { label: '📚 学业烦恼', value: '学业烦恼' },
  { label: '🌱 自我成长', value: '自我成长' },
  { label: '💬 日常吐槽', value: '日常吐槽' },
  { label: '🤝 人际关系', value: '人际关系' },
]

const submit = async () => {
  if (!content.value.trim()) return
  submitting.value = true
  try {
    const body = { content: content.value }
    if (selectedTag.value) body.tag = selectedTag.value
    if (deletePassword.value) body.deletePassword = deletePassword.value
    const res = await api.post('/posts', body)
    router.push(`/post/${res.data.id}`)
  } catch (e) {
    const msg = e.response?.data?.message || '发布失败，请稍后再试'
    alert(msg)
  } finally {
    submitting.value = false
  }
}
</script>
