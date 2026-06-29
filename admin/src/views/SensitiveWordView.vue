<template>
  <div>
    <h2 class="text-xl font-bold text-gray-800 mb-4">敏感词管理</h2>
    <div class="flex gap-2 mb-4">
      <input v-model="newWord" @keyup.enter="addWord" placeholder="输入敏感词..."
        class="flex-1 px-4 py-2 rounded-lg border border-gray-200 text-sm" />
      <select v-model="newMatchType" class="px-3 py-2 rounded-lg border border-gray-200 text-sm">
        <option :value="1">精确匹配</option><option :value="2">正则匹配</option>
      </select>
      <button @click="addWord" class="px-4 py-2 bg-blue-500 text-white rounded-lg text-sm hover:bg-blue-600">添加</button>
    </div>
    <div class="bg-white rounded-xl shadow-sm overflow-hidden">
      <table class="w-full text-sm">
        <thead class="bg-gray-50 text-left text-gray-500">
          <tr><th class="px-4 py-3">ID</th><th class="px-4 py-3">敏感词</th><th class="px-4 py-3">匹配类型</th><th class="px-4 py-3">操作</th></tr>
        </thead>
        <tbody>
          <tr v-for="w in words" :key="w.id" class="border-t border-gray-100">
            <td class="px-4 py-3 text-gray-400">{{ w.id }}</td>
            <td class="px-4 py-3 font-mono">{{ w.word }}</td>
            <td class="px-4 py-3">{{ w.matchType === 1 ? '精确匹配' : '正则匹配' }}</td>
            <td class="px-4 py-3"><button @click="deleteWord(w.id)" class="text-red-500 hover:underline text-xs">删除</button></td>
          </tr>
        </tbody>
      </table>
    </div>
    <div v-if="words.length === 0" class="text-center text-gray-400 py-10">暂无敏感词</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '@/api'

const words = ref([])
const newWord = ref('')
const newMatchType = ref(1)

const fetchWords = async () => {
  try { const res = await api.get('/sensitive-words'); words.value = (res.data || res) || [] } catch (e) { console.error(e) }
}

const addWord = async () => {
  if (!newWord.value.trim()) return
  try {
    await api.post('/sensitive-words', { word: newWord.value.trim(), matchType: newMatchType.value })
    newWord.value = ''
    fetchWords()
  } catch (e) { alert('添加失败') }
}

const deleteWord = async (id) => {
  try { await api.delete(`/sensitive-words/${id}`); fetchWords() } catch (e) { alert('删除失败') }
}

onMounted(fetchWords)
</script>
