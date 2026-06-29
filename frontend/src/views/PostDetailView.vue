<template>
  <div class="min-h-screen flex flex-col">
    <header class="sticky top-0 z-10 bg-white/80 backdrop-blur-md border-b border-warm-100">
      <div class="max-w-3xl mx-auto px-4 h-14 flex items-center gap-3">
        <button @click="$router.back()" class="p-2 text-dusk-500 text-lg">←</button>
        <h1 class="text-lg font-medium text-dusk-800 flex-1">树洞详情</h1>
      </div>
    </header>

    <main class="flex-1 max-w-3xl mx-auto px-4 py-6 w-full">
      <div v-if="loading" class="post-card animate-pulse">
        <div class="h-4 bg-warm-100 rounded w-3/4 mb-3"></div>
        <div class="h-4 bg-warm-100 rounded w-1/2 mb-4"></div>
      </div>

      <div v-else-if="notFound" class="flex flex-col items-center justify-center py-20">
        <div class="text-6xl mb-4">🌙</div>
        <p class="text-dusk-400 text-lg">这条心事已经消失了</p>
        <router-link to="/" class="btn-primary mt-6 inline-block no-underline">回到首页</router-link>
      </div>

      <div v-else-if="post" class="post-card mb-6">
        <div class="flex items-center gap-2 mb-3">
          <span class="w-8 h-8 rounded-full inline-block" :style="{ backgroundColor: '#FF7F1A' }"></span>
          <span class="text-sm text-dusk-500">{{ nickname }}</span>
        </div>
        <p class="text-dusk-700 leading-relaxed whitespace-pre-wrap mb-4">{{ post.content }}</p>
        <div v-if="post.tag" class="mb-4"><span class="tag-chip text-xs">{{ post.tag }}</span></div>

        <div class="flex items-center gap-6 text-dusk-400 text-sm mb-4">
          <button @click="toggleLike" :class="['flex items-center gap-1 transition-colors', liked && 'text-red-500']">
            {{ liked ? '❤️' : '🤍' }} <span>{{ likeCount }}</span>
          </button>
          <button @click="toggleHug" :class="['flex items-center gap-1 transition-colors', hugged && 'text-warm-500']">
            🤗 <span>{{ hugCount }}</span>
          </button>
          <button @click="toggleFeel" :class="['flex items-center gap-1 transition-colors', felt && 'text-blue-500']">
            💡 <span>{{ feelCount }}</span>
          </button>
        </div>

        <div class="flex items-center justify-between text-sm text-dusk-400 pt-4 border-t border-warm-100">
          <span>{{ formatTime(post.createdAt) }}</span>
          <div class="flex gap-3">
            <button @click="showDelete = true" class="hover:text-red-500 transition-colors">删除</button>
            <button @click="showReport = true" class="hover:text-red-500 transition-colors">举报</button>
          </div>
        </div>
      </div>

      <!-- 删除弹窗 -->
      <div v-if="showDelete" class="fixed inset-0 z-50 flex items-center justify-center bg-black/40" @click.self="showDelete = false">
        <div class="bg-white rounded-2xl p-6 w-80 shadow-xl">
          <h3 class="font-medium text-dusk-800 mb-3">输入删除密码</h3>
          <input v-model="deletePwd" type="password" placeholder="发帖时设置的密码"
            class="w-full px-4 py-2 rounded-full border border-warm-200 focus:outline-none focus:border-warm-400 text-sm mb-2" />
          <p v-if="deleteError" class="text-red-500 text-xs mb-2">{{ deleteError }}</p>
          <div class="flex gap-2 mt-3">
            <button @click="showDelete = false" class="flex-1 py-2 rounded-full border border-warm-200 text-sm">取消</button>
            <button @click="doDelete" class="flex-1 py-2 rounded-full bg-red-500 text-white text-sm">确认删除</button>
          </div>
        </div>
      </div>

      <!-- 举报弹窗 -->
      <div v-if="showReport" class="fixed inset-0 z-50 flex items-center justify-center bg-black/40" @click.self="showReport = false">
        <div class="bg-white rounded-2xl p-6 w-80 shadow-xl">
          <h3 class="font-medium text-dusk-800 mb-3">举报内容</h3>
          <div class="space-y-2 mb-3">
            <button v-for="r in reportReasons" :key="r" @click="selectedReason = r"
              :class="['w-full text-left px-3 py-2 rounded-xl text-sm border transition-colors',
                selectedReason === r ? 'border-red-400 bg-red-50 text-red-600' : 'border-warm-200 text-dusk-600']">{{ r }}</button>
          </div>
          <div class="flex gap-2">
            <button @click="showReport = false" class="flex-1 py-2 rounded-full border border-warm-200 text-sm">取消</button>
            <button @click="doReport" :disabled="!selectedReason"
              class="flex-1 py-2 rounded-full bg-red-500 text-white text-sm disabled:opacity-50">提交举报</button>
          </div>
        </div>
      </div>

      <!-- 评论区 -->
      <section class="mb-20">
        <h3 class="text-lg font-medium text-dusk-800 mb-4">评论 ({{ comments.length }})</h3>
        <div v-if="comments.length === 0" class="text-center text-dusk-400 py-8 text-sm">还没有评论，来说点什么吧</div>
        <div class="space-y-3 mb-6">
          <div v-for="c in comments" :key="c.id" class="bg-white rounded-xl p-4 border border-warm-100">
            <p class="text-dusk-600 text-sm leading-relaxed mb-2">{{ c.content }}</p>
            <div class="flex items-center justify-between text-xs text-dusk-400">
              <span>{{ formatTime(c.createdAt) }}</span>
            </div>
          </div>
        </div>
      </section>

      <!-- 底部评论输入 -->
      <div class="fixed bottom-0 left-0 right-0 bg-white/95 backdrop-blur-md border-t border-warm-100 py-3 px-4 z-20">
        <div class="max-w-3xl mx-auto flex gap-2">
          <input v-model="commentText" placeholder="写下你的回应..." maxlength="500"
            class="flex-1 px-4 py-2.5 rounded-full border border-warm-200 focus:outline-none focus:border-warm-400 text-sm bg-white"
            @keyup.enter="submitComment" />
          <button @click="submitComment" :disabled="!commentText.trim() || commentSubmitting"
            class="btn-primary text-sm px-5 py-2.5 shrink-0">{{ commentSubmitting ? '...' : '发送' }}</button>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getPostDetail, getComments, createComment, likePost, hugPost, feelPost, deletePost } from '@/api/posts'
import { reportContent } from '@/api/posts'

const route = useRoute()
const post = ref(null)
const comments = ref([])
const commentText = ref('')
const commentSubmitting = ref(false)
const loading = ref(true)
const notFound = ref(false)
const liked = ref(false); const hugged = ref(false); const felt = ref(false)
const likeCount = ref(0); const hugCount = ref(0); const feelCount = ref(0)
const nickname = ref('')
const showDelete = ref(false); const deletePwd = ref(''); const deleteError = ref('')
const showReport = ref(false); const selectedReason = ref('')

const reportReasons = ['色情低俗', '暴力欺凌', '虚假信息', '侵犯隐私', '其他']
const formatTime = (t) => {
  if (!t) return ''
  const d = new Date(t), now = new Date()
  const diff = Math.floor((now - d) / 1000)
  if (diff < 60) return '刚刚'
  if (diff < 3600) return Math.floor(diff / 60) + '分钟前'
  if (diff < 86400) return Math.floor(diff / 3600) + '小时前'
  if (diff < 604800) return Math.floor(diff / 86400) + '天前'
  return d.toLocaleDateString('zh-CN')
}

const fetchPost = async () => {
  try {
    const res = await getPostDetail(route.params.id)
    const data = res.data || res
    if (!data || data.code === 404) { notFound.value = true; return }
    post.value = data
    likeCount.value = data.likeCount || 0
    hugCount.value = data.hugCount || 0
    feelCount.value = data.feelCount || 0
    nickname.value = '匿名用户'
  } catch (e) {
    notFound.value = true
  } finally { loading.value = false }
}

const fetchComments = async () => {
  try {
    const res = await getComments(route.params.id)
    comments.value = (res.data || res) || []
  } catch (e) { comments.value = [] }
}

const toggleLike = async () => {
  liked.value = !liked.value
  likeCount.value += liked.value ? 1 : -1
  try { await likePost(route.params.id) } catch { liked.value = !liked.value; likeCount.value += liked.value ? 1 : -1 }
}
const toggleHug = async () => {
  hugged.value = !hugged.value
  hugCount.value += hugged.value ? 1 : -1
  try { await hugPost(route.params.id) } catch { hugged.value = !hugged.value; hugCount.value += hugged.value ? 1 : -1 }
}
const toggleFeel = async () => {
  felt.value = !felt.value
  feelCount.value += felt.value ? 1 : -1
  try { await feelPost(route.params.id) } catch { felt.value = !felt.value; feelCount.value += felt.value ? 1 : -1 }
}

const submitComment = async () => {
  if (!commentText.value.trim()) return
  commentSubmitting.value = true
  try {
    await createComment(route.params.id, commentText.value.trim())
    commentText.value = ''
    await fetchComments()
  } catch (e) {
    alert(e?.response?.data?.message || '评论失败')
  } finally { commentSubmitting.value = false }
}

const doDelete = async () => {
  deleteError.value = ''
  try {
    await deletePost(route.params.id, deletePwd.value)
    notFound.value = true; post.value = null; showDelete.value = false
  } catch (e) {
    deleteError.value = e?.response?.data?.message || '密码错误'
  }
}

const doReport = async () => {
  try {
    await reportContent({ targetType: 'post', targetId: Number(route.params.id), reason: selectedReason.value })
    showReport.value = false
    alert('举报已提交，感谢你的反馈')
  } catch (e) {
    alert(e?.response?.data?.message || '举报失败')
  }
}

onMounted(() => { fetchPost(); fetchComments() })
</script>
