<template>
  <div>
    <h2 class="text-xl font-bold text-gray-800 mb-4">IP 封禁管理</h2>
    <div class="flex gap-2 mb-4">
      <input v-model="newIpHash" @keyup.enter="addBlock" placeholder="IP 哈希值..."
        class="flex-1 px-4 py-2 rounded-lg border border-gray-200 text-sm font-mono" />
      <input v-model="reason" placeholder="封禁原因" class="w-40 px-4 py-2 rounded-lg border border-gray-200 text-sm" />
      <button @click="addBlock" class="px-4 py-2 bg-red-500 text-white rounded-lg text-sm hover:bg-red-600">封禁</button>
    </div>
    <div class="bg-white rounded-xl shadow-sm overflow-hidden">
      <table class="w-full text-sm">
        <thead class="bg-gray-50 text-left text-gray-500">
          <tr><th class="px-4 py-3">ID</th><th class="px-4 py-3">IP 哈希</th><th class="px-4 py-3">原因</th><th class="px-4 py-3">封禁时间</th><th class="px-4 py-3">操作</th></tr>
        </thead>
        <tbody>
          <tr v-for="b in blocks" :key="b.id" class="border-t border-gray-100">
            <td class="px-4 py-3 text-gray-400">{{ b.id }}</td>
            <td class="px-4 py-3 font-mono text-xs">{{ b.ipHash }}</td>
            <td class="px-4 py-3">{{ b.reason || '-' }}</td>
            <td class="px-4 py-3 text-xs text-gray-400">{{ formatTime(b.blockedAt) }}</td>
            <td class="px-4 py-3"><button @click="removeBlock(b.id)" class="text-green-500 hover:underline text-xs">解封</button></td>
          </tr>
        </tbody>
      </table>
    </div>
    <div v-if="blocks.length === 0" class="text-center text-gray-400 py-10">暂无封禁记录</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '@/api'

const blocks = ref([])
const newIpHash = ref('')
const reason = ref('')

const formatTime = (t) => t ? new Date(t).toLocaleString('zh-CN') : ''

const fetchBlocks = async () => {
  try { const res = await api.get('/ip-blocks'); blocks.value = (res.data || res) || [] } catch (e) { console.error(e) }
}

const addBlock = async () => {
  if (!newIpHash.value.trim()) return
  try {
    await api.post('/ip-blocks', { ipHash: newIpHash.value.trim(), reason: reason.value })
    newIpHash.value = ''; reason.value = ''
    fetchBlocks()
  } catch (e) { alert('封禁失败') }
}

const removeBlock = async (id) => {
  try { await api.delete(`/ip-blocks/${id}`); fetchBlocks() } catch (e) { alert('解封失败') }
}

onMounted(fetchBlocks)
</script>
