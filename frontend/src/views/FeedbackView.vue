<template>
  <div class="min-h-screen flex flex-col">
    <header class="sticky top-0 z-10 bg-white/80 backdrop-blur-md border-b border-warm-100">
      <div class="max-w-3xl mx-auto px-4 h-14 flex items-center gap-3">
        <button @click="$router.back()" class="p-2 text-dusk-500 text-lg">←</button>
        <h1 class="text-lg font-medium text-dusk-800 flex-1">意见反馈</h1>
        <button @click="submit" :disabled="!content.trim() || submitting" class="btn-primary text-sm px-5 py-2">
          {{ submitting ? '提交中...' : '提交' }}
        </button>
      </div>
    </header>

    <main class="flex-1 max-w-3xl mx-auto px-4 py-6 w-full">
      <p class="text-sm text-dusk-400 mb-4">欢迎提出任何建议或反馈，帮助我们让晚山信箱变得更好 🌙</p>

      <textarea v-model="content" placeholder="写下你的建议或反馈..." maxlength="1000"
        class="w-full h-48 p-4 rounded-2xl border border-warm-200 focus:outline-none focus:border-warm-400 resize-none text-dusk-700 leading-relaxed bg-white" />

      <div class="flex items-center justify-between text-sm mt-2 mb-6">
        <span :class="content.length > 900 ? 'text-red-400' : 'text-dusk-400'">{{ content.length }} / 1000</span>
      </div>

      <div class="mb-6">
        <h3 class="text-sm font-medium text-dusk-600 mb-3">联系方式（可选）</h3>
        <input v-model="contact" type="text" placeholder="邮箱或微信号，方便我们回复你"
          autocomplete="off" autocapitalize="off" autocorrect="off" spellcheck="false"
          class="w-full px-4 py-3 rounded-full border border-warm-200 focus:outline-none focus:border-warm-400 text-sm bg-white" />
      </div>

      <div v-if="successMsg" class="bg-green-50 text-green-600 px-4 py-3 rounded-xl text-sm mb-4">{{ successMsg }}</div>
      <div v-if="errorMsg" class="bg-red-50 text-red-500 px-4 py-3 rounded-xl text-sm mb-4">{{ errorMsg }}</div>
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const content = ref('')
const contact = ref('')
const submitting = ref(false)
const successMsg = ref('')
const errorMsg = ref('')

const submit = async () => {
  if (!content.value.trim()) return
  submitting.value = true; errorMsg.value = ''; successMsg.value = ''
  try {
    await axios.post('/api/v1/feedback', {
      content: content.value.trim(),
      contact: contact.value.trim()
    })
    successMsg.value = '感谢你的反馈！我们会认真对待每一条建议 🌙'
    content.value = ''
    contact.value = ''
    setTimeout(() => router.replace('/'), 2000)
  } catch (e) {
    errorMsg.value = e?.response?.data?.message || '提交失败，请稍后再试'
  } finally { submitting.value = false }
}
</script>
