<template>
  <div>
    <h2 class="text-xl font-bold text-gray-800 mb-4">内容管理</h2>
    <div class="flex gap-3 mb-4">
      <select v-model="filterStatus" @change="fetchPosts" class="px-3 py-2 rounded-lg border border-gray-200 text-sm">
        <option :value="null">全部状态</option><option :value="1">正常</option><option :value="2">用户删除</option><option :value="3">管理员删除</option>
      </select>
      <input v-model="keyword" @keyup.enter="fetchPosts" placeholder="搜索内容..." class="flex-1 px-4 py-2 rounded-lg border border-gray-200 text-sm" />
      <button @click="fetchPosts" class="px-4 py-2 bg-blue-500 text-white rounded-lg text-sm hover:bg-blue-600">搜索</button>
    </div>

    <div class="bg-white rounded-xl shadow-sm overflow-hidden">
      <table class="w-full text-sm">
        <thead class="bg-gray-50 text-left text-gray-500">
          <tr><th class="px-4 py-3">ID</th><th class="px-4 py-3">内容</th><th class="px-4 py-3">标签</th><th class="px-4 py-3">互动</th><th class="px-4 py-3">状态</th><th class="px-4 py-3">时间</th><th class="px-4 py-3">操作</th></tr>
        </thead>
        <tbody>
          <tr v-for="p in posts" :key="p.id" class="border-t border-gray-100 hover:bg-gray-50">
            <td class="px-4 py-3 text-gray-400">{{ p.id }}</td>
            <td class="px-4 py-3 max-w-xs truncate">{{ p.content }}</td>
            <td class="px-4 py-3">{{ p.tag || '-' }}</td>
            <td class="px-4 py-3">❤️{{ p.likeCount }} 💬{{ p.commentCount }}</td>
            <td class="px-4 py-3"><span :class="statusClass(p.status)">{{ statusText(p.status) }}</span></td>
            <td class="px-4 py-3 text-gray-400 text-xs">{{ formatTime(p.createdAt) }}</td>
            <td class="px-4 py-3">
              <button v-if="p.status === 1" @click="deletePost(p.id)" class="text-red-500 hover:underline text-xs">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <div class="flex justify-center gap-2 mt-4">
      <button @click="page--; fetchPosts()" :disabled="page <= 1" class="px-3 py-1 rounded border text-sm disabled:opacity-30">上一页</button>
      <span class="px-3 py-1 text-sm text-gray-500">第 {{ page }} 页</span>
      <button @click="page++; fetchPosts()" class="px-3 py-1 rounded border text-sm">下一页</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '@/api'

const posts = ref([])
const page = ref(1)
const filterStatus = ref(null)
const keyword = ref('')

const statusClass = (s) => ({ 1: 'text-green-500', 2: 'text-yellow-500', 3: 'text-red-500' }[s] || '')
const statusText = (s) => ({ 1: '正常', 2: '用户删除', 3: '管理员删除' }[s] || '未知')
const formatTime = (t) => t ? new Date(t).toLocaleString('zh-CN') : ''

const fetchPosts = async () => {
  try {
    const params = { page: page.value, size: 20 }
    if (filterStatus.value) params.status = filterStatus.value
    if (keyword.value) params.keyword = keyword.value
    const res = await api.get('/posts', { params })
    const data = res.data || res
    posts.value = data.records || []
  } catch (e) { console.error(e) }
}

const deletePost = async (id) => {
  if (!confirm('确认删除该帖子？')) return
  try {
    await api.delete(`/posts/${id}`)
    fetchPosts()
  } catch (e) { alert('删除失败') }
}

onMounted(fetchPosts)
</script>
