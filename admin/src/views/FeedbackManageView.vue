<template>
  <div>
    <h2 class="text-xl font-bold text-gray-800 mb-4">💬 用户反馈</h2>
    <div class="space-y-3">
      <div v-for="fb in feedbacks" :key="fb.id" class="bg-white rounded-xl p-5 shadow-sm">
        <div class="flex items-start justify-between mb-2">
          <div>
            <span :class="['text-xs px-2 py-0.5 rounded-full', fb.status === 0 ? 'bg-red-100 text-red-600' : 'bg-green-100 text-green-600']">
              {{ fb.status === 0 ? '未读' : '已读' }}
            </span>
            <span v-if="fb.contact" class="text-sm text-gray-400 ml-2">联系方式：{{ fb.contact }}</span>
          </div>
          <div class="flex items-center gap-2">
            <span class="text-xs text-gray-400">{{ formatTime(fb.created_at) }}</span>
            <button
              v-if="fb.status === 0"
              @click="markRead(fb.id)"
              class="px-2 py-0.5 text-xs text-blue-500 hover:bg-blue-50 rounded transition-colors"
            >标记已读</button>
          </div>
        </div>
        <p class="text-sm text-gray-700 whitespace-pre-wrap leading-relaxed">{{ fb.content }}</p>
      </div>
      <div v-if="feedbacks.length === 0" class="text-center text-gray-400 py-10">暂无用户反馈</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '@/api'

const feedbacks = ref([])

const formatTime = (t) => t ? new Date(t).toLocaleString('zh-CN') : ''

const fetchFeedbacks = async () => {
  try {
    const res = await api.get('/feedbacks')
    feedbacks.value = (res.data || res) || []
  } catch (e) {
    console.error(e)
  }
}

const markRead = async (id) => {
  try {
    await api.put(`/feedbacks/${id}/read`)
    fetchFeedbacks()
  } catch (e) {
    alert('操作失败')
  }
}

onMounted(fetchFeedbacks)
</script>
