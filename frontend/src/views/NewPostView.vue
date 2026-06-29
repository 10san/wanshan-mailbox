<template>
  <div class="min-h-screen flex flex-col">
    <header class="sticky top-0 z-10 bg-white/80 backdrop-blur-md border-b border-warm-100">
      <div class="max-w-3xl mx-auto px-4 h-14 flex items-center gap-3">
        <button @click="$router.back()" class="p-2 text-dusk-500 text-lg">←</button>
        <h1 class="text-lg font-medium text-dusk-800 flex-1">写心事</h1>
        <button @click="submit" :disabled="!content.trim() || submitting" class="btn-primary text-sm px-5 py-2">
          {{ submitting ? '发布中...' : '发布' }}
        </button>
      </div>
    </header>

    <main class="flex-1 max-w-3xl mx-auto px-4 py-6 w-full">
      <textarea v-model="content" placeholder="在这里写下你的心事..." maxlength="2000"
        class="w-full h-64 p-4 rounded-2xl border border-warm-200 focus:outline-none focus:border-warm-400 resize-none text-dusk-700 leading-relaxed bg-white" />
      <div class="flex items-center justify-between text-sm mt-2 mb-6">
        <span :class="content.length > 1900 ? 'text-red-400' : 'text-dusk-400'">{{ content.length }} / 2000</span>
        <span v-if="content.length > 1900" class="text-red-400">即将达到上限</span>
      </div>

      <!-- 图片上传 -->
      <ImageUploader :imageUrl="imageUrl" @update:imageUrl="imageUrl = $event" />

      <div class="mb-6">
        <h3 class="text-sm font-medium text-dusk-600 mb-3">选择情绪标签（可选）</h3>
        <div class="flex flex-wrap gap-2">
          <button v-for="tag in tags" :key="tag.value" @click="selectedTag = tag.value"
            :class="['tag-chip', { active: selectedTag === tag.value }]">{{ tag.label }}</button>
        </div>
      </div>

      <div class="mb-6">
        <h3 class="text-sm font-medium text-dusk-600 mb-3">设置删除密码（可选）</h3>
        <input v-model="deletePassword" type="password" placeholder="设置后凭密码可删除，不设则只能管理员删除"
          class="w-full px-4 py-3 rounded-full border border-warm-200 focus:outline-none focus:border-warm-400 text-sm bg-white" />
        <p class="text-xs text-dusk-400 mt-1 ml-1">不设置密码则只能由管理员删除</p>
      </div>

      <div v-if="errorMsg" class="bg-red-50 text-red-500 px-4 py-3 rounded-xl text-sm mb-4">{{ errorMsg }}</div>
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { createPost } from '@/api/posts'
import ImageUploader from '@/components/ImageUploader.vue'

const router = useRouter()
const content = ref('')
const selectedTag = ref('')
const deletePassword = ref('')
const imageUrl = ref('')
const submitting = ref(false)
const errorMsg = ref('')

const tags = [
  { label: '🌙 深夜emo', value: '深夜emo' },{ label: '💔 情感树洞', value: '情感树洞' },
  { label: '🏠 家庭琐事', value: '家庭琐事' },{ label: '💼 职场压力', value: '职场压力' },
  { label: '📚 学业烦恼', value: '学业烦恼' },{ label: '🌱 自我成长', value: '自我成长' },
  { label: '💬 日常吐槽', value: '日常吐槽' },{ label: '🤝 人际关系', value: '人际关系' },
]

const submit = async () => {
  if (!content.value.trim()) return
  submitting.value = true; errorMsg.value = ''
  try {
    const body = { content: content.value.trim() }
    if (selectedTag.value) body.tag = selectedTag.value
    if (imageUrl.value) body.imageUrl = imageUrl.value
    if (deletePassword.value) body.deletePassword = deletePassword.value
    const res = await createPost(body)
    const data = res.data || res
    router.push(`/post/${data.id}`)
  } catch (e) {
    errorMsg.value = e?.response?.data?.message || e?.message || '发布失败，请稍后再试'
  } finally { submitting.value = false }
}
</script>
