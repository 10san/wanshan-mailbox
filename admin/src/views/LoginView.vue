<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-100 px-4">
    <div class="bg-white rounded-2xl shadow-lg p-8 w-full max-w-sm">
      <h1 class="text-2xl font-bold text-gray-800 mb-6 text-center">🌙 晚山信箱</h1>
      <p class="text-sm text-gray-500 mb-6 text-center">管理后台</p>
      <div class="mb-4">
        <input v-model="username" placeholder="用户名" class="w-full px-4 py-2.5 rounded-xl border border-gray-200 focus:outline-none focus:border-blue-400" @keyup.enter="login" />
      </div>
      <div class="mb-4">
        <input v-model="password" type="password" placeholder="密码" class="w-full px-4 py-2.5 rounded-xl border border-gray-200 focus:outline-none focus:border-blue-400" @keyup.enter="login" />
      </div>
      <p v-if="error" class="text-red-500 text-sm mb-4">{{ error }}</p>
      <button @click="login" :disabled="loading" class="w-full py-2.5 bg-blue-500 text-white rounded-xl font-medium hover:bg-blue-600 disabled:opacity-50 transition-colors">
        {{ loading ? '登录中...' : '登录' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api'

const router = useRouter()
const username = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

const login = async () => {
  if (!username.value || !password.value) { error.value = '请输入用户名和密码'; return }
  loading.value = true; error.value = ''
  try {
    const res = await api.post('/login', { username: username.value, password: password.value })
    const data = res.data || res
    localStorage.setItem('admin_token', data.token)
    localStorage.setItem('admin_username', data.username)
    router.push('/')
  } catch (e) {
    error.value = e?.response?.data?.message || '登录失败'
  } finally { loading.value = false }
}
</script>
