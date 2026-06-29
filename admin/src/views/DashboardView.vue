<template>
  <div>
    <h2 class="text-xl font-bold text-gray-800 mb-6">数据看板</h2>
    <div class="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
      <div class="bg-white rounded-xl p-5 shadow-sm">
        <p class="text-sm text-gray-500">今日发帖</p>
        <p class="text-3xl font-bold text-blue-500 mt-1">{{ stats.todayPosts }}</p>
      </div>
      <div class="bg-white rounded-xl p-5 shadow-sm">
        <p class="text-sm text-gray-500">今日评论</p>
        <p class="text-3xl font-bold text-green-500 mt-1">{{ stats.todayComments }}</p>
      </div>
      <div class="bg-white rounded-xl p-5 shadow-sm">
        <p class="text-sm text-gray-500">累计帖子</p>
        <p class="text-3xl font-bold text-purple-500 mt-1">{{ stats.totalPosts }}</p>
      </div>
      <div class="bg-white rounded-xl p-5 shadow-sm">
        <p class="text-sm text-gray-500">待处理举报</p>
        <p class="text-3xl font-bold text-red-500 mt-1">{{ stats.pendingReports }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '@/api'

const stats = ref({ todayPosts: 0, todayComments: 0, totalPosts: 0, pendingReports: 0 })

onMounted(async () => {
  try {
    const res = await api.get('/dashboard')
    stats.value = res.data || res
  } catch (e) { console.error(e) }
})
</script>
