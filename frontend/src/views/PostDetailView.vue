<template>
  <div class="min-h-screen flex flex-col">
    <header class="sticky top-0 z-10 bg-white/80 backdrop-blur-md border-b border-warm-100">
      <div class="max-w-3xl mx-auto px-4 h-14 flex items-center gap-3">
        <button @click="$router.back()" class="p-2 text-dusk-500">←</button>
        <h1 class="text-lg font-medium text-dusk-800 flex-1">帖子详情</h1>
      </div>
    </header>

    <main class="flex-1 max-w-3xl mx-auto px-4 py-6 w-full">
      <div v-if="loading" class="post-card animate-pulse">
        <div class="h-4 bg-warm-100 rounded w-3/4 mb-3"></div>
        <div class="h-4 bg-warm-100 rounded w-1/2 mb-4"></div>
      </div>

      <div v-else-if="!post" class="flex flex-col items-center justify-center py-20">
        <div class="text-6xl mb-4">🌙</div>
        <p class="text-dusk-400 text-lg">这条心事已经消失了</p>
      </div>

      <div v-else class="post-card mb-6">
        <p class="text-dusk-700 leading-relaxed whitespace-pre-wrap mb-4">{{ post.content }}</p>
        <div class="flex items-center gap-2 mb-4">
          <span v-if="post.tag" class="tag-chip text-xs">{{ post.tag }}</span>
        </div>
        <div class="flex items-center gap-6 text-dusk-400 text-sm mb-4">
          <button @click="toggleLike" :class="['flex items-center gap-1', liked && 'text-red-500']">
            {{ liked ? '❤️' : '🤍' }} {{ post.likeCount || 0 }}
          </button>
          <button @click="toggleHug" :class="['flex items-center gap-1', hugged && 'text-warm-500']">
            🤗 {{ post.hugCount || 0 }}
          </button>
          <button @click="toggleFeel" :class="['flex items-center gap-1', felt && 'text-blue-500']">
            💡 {{ post.feelCount || 0 }}
          </button>
        </div>
        <div class="flex items-center justify-between text-sm text-dusk-400 pt-4 border-t border-warm-100">
          <span>{{ post.createdAt }}</span>
          <div class="flex gap-3">
            <button @click="showDelete = true" class="hover:text-red-500">删除</button>
            <button @click="showReport = true" class="hover:text-red-500">举报</button>
          </div>
        </div>
      </div>

      <!-- 评论区 -->
      <section>
        <h3 class="text-lg font-medium text-dusk-800 mb-4">评论 ({{ comments.length }})</h3>
        <div class="space-y-4 mb-6">
          <div v-for="c in comments" :key="c.id" class="bg-white rounded-xl p-4 border border-warm-100">
            <p class="text-dusk-600 text-sm leading-relaxed mb-2">{{ c.content }}</p>
            <div class="flex items-center justify-between text-xs text-dusk-400">
              <span>{{ c.createdAt }}</span>
              <button @click="reportComment(c.id)" class="hover:text-red-500">举报</button>
            </div>
          </div>
        </div>

        <!-- 评论输入框 -->
        <div class="sticky bottom-0 bg-white/90 backdrop-blur-md border-t border-warm-100 py-3">
          <div class="flex gap-2">
            <input v-model="commentText" placeholder="写下你的回应..." maxlength="500"
              class="flex-1 px-4 py-2 rounded-full border border-warm-200 focus:outline-none focus:border-warm-400 text-sm" />
            <button @click="submitComment" :disabled="!commentText.trim()" class="btn-primary text-sm px-5 py-2">
              发送
            </button>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '@/api'

const route = useRoute()
const post = ref(null)
const comments = ref([])
const commentText = ref('')
const loading = ref(true)
const liked = ref(false)
const hugged = ref(false)
const felt = ref(false)
const showDelete = ref(false)
const showReport = ref(false)

const fetchPost = async () => {
  try {
    const res = await api.get(`/posts/${route.params.id}`)
    post.value = res.data
  } catch (e) {
    post.value = null
  } finally {
    loading.value = false
  }
}

const toggleLike = async () => {
  liked.value = !liked.value
  try { await api.post(`/posts/${route.params.id}/like`) } catch { liked.value = !liked.value }
}

const toggleHug = async () => {
  hugged.value = !hugged.value
  try { await api.post(`/posts/${route.params.id}/hug`) } catch { hugged.value = !hugged.value }
}

const toggleFeel = async () => {
  felt.value = !felt.value
  try { await api.post(`/posts/${route.params.id}/feel`) } catch { felt.value = !felt.value }
}

const submitComment = async () => {
  if (!commentText.value.trim()) return
  try {
    await api.post(`/posts/${route.params.id}/comments`, { content: commentText.value })
    commentText.value = ''
    // 重新加载评论
  } catch (e) {
    alert('评论失败，请稍后再试')
  }
}

const reportComment = (commentId) => {
  // TODO: 打开举报弹窗
}

onMounted(() => {
  fetchPost()
})
</script>
