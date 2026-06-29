<template>
  <div class="min-h-screen flex bg-gray-100">
    <aside class="w-56 bg-white shadow-sm p-4 flex flex-col shrink-0">
      <h1 class="text-lg font-bold text-gray-800 mb-6 px-2">🌙 管理后台</h1>
      <nav class="flex-1 space-y-1">
        <router-link to="/" class="block px-3 py-2 rounded-lg text-sm text-gray-600 hover:bg-blue-50 hover:text-blue-600 transition-colors">📊 数据看板</router-link>
        <router-link to="/posts" class="block px-3 py-2 rounded-lg text-sm text-gray-600 hover:bg-blue-50 hover:text-blue-600 transition-colors">📝 内容管理</router-link>
        <router-link to="/reports" class="block px-3 py-2 rounded-lg text-sm text-gray-600 hover:bg-blue-50 hover:text-blue-600 transition-colors">🚨 举报处理</router-link>
        <router-link to="/sensitive-words" class="block px-3 py-2 rounded-lg text-sm text-gray-600 hover:bg-blue-50 hover:text-blue-600 transition-colors">🔒 敏感词</router-link>
      </nav>
      <button @click="logout" class="mt-4 px-3 py-2 text-sm text-red-500 hover:bg-red-50 rounded-lg transition-colors">退出登录</button>
    </aside>
    <main class="flex-1 p-6 overflow-auto">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()
const logout = () => {
  localStorage.removeItem('admin_token')
  router.push('/login')
}
</script>
