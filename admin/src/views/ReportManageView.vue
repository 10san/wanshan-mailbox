<template>
  <div>
    <h2 class="text-xl font-bold text-gray-800 mb-4">举报处理</h2>
    <div class="space-y-3">
      <div v-for="r in reports" :key="r.id" class="bg-white rounded-xl p-5 shadow-sm">
        <div class="flex items-start justify-between mb-2">
          <div>
            <span :class="['text-xs px-2 py-0.5 rounded-full', statusClass(r.status)]">{{ statusText(r.status) }}</span>
            <span class="text-sm text-gray-500 ml-2">{{ r.targetType === 'post' ? '帖子' : '评论' }} #{{ r.targetId }}</span>
            <span class="text-sm text-gray-500 ml-2">原因：{{ r.reason }}</span>
          </div>
          <span class="text-xs text-gray-400">{{ formatTime(r.createdAt) }}</span>
        </div>
        <div v-if="r.status === 0" class="flex gap-2 mt-2">
          <button @click="handleReport(r.id, 'delete')" class="px-3 py-1 bg-red-500 text-white rounded-lg text-xs hover:bg-red-600">删除内容</button>
          <button @click="handleReport(r.id, 'dismiss')" class="px-3 py-1 bg-gray-200 text-gray-600 rounded-lg text-xs hover:bg-gray-300">驳回举报</button>
        </div>
      </div>
      <div v-if="reports.length === 0" class="text-center text-gray-400 py-10">暂无举报</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '@/api'

const reports = ref([])
const statusClass = (s) => ({ 0: 'bg-red-100 text-red-600', 1: 'bg-green-100 text-green-600', 2: 'bg-gray-100 text-gray-500' }[s] || '')
const statusText = (s) => ({ 0: '待处理', 1: '已处理(删除)', 2: '已驳回' }[s] || '')
const formatTime = (t) => t ? new Date(t).toLocaleString('zh-CN') : ''

const fetchReports = async () => {
  try { const res = await api.get('/reports'); reports.value = (res.data || res) || [] } catch (e) { console.error(e) }
}

const handleReport = async (id, action) => {
  try {
    await api.put(`/reports/${id}`, { action })
    fetchReports()
  } catch (e) { alert('操作失败') }
}

onMounted(fetchReports)
</script>
