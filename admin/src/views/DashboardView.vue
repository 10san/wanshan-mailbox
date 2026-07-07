<template>
  <div>
    <h2 class="text-xl font-bold text-gray-800 mb-6">数据看板</h2>
    <div class="grid grid-cols-2 lg:grid-cols-5 gap-4 mb-8">
      <div class="bg-white rounded-xl p-5 shadow-sm"><p class="text-sm text-gray-500">今日发帖</p><p class="text-3xl font-bold text-blue-500 mt-1">{{ stats.todayPosts }}</p></div>
      <div class="bg-white rounded-xl p-5 shadow-sm"><p class="text-sm text-gray-500">今日评论</p><p class="text-3xl font-bold text-green-500 mt-1">{{ stats.todayComments }}</p></div>
      <div class="bg-white rounded-xl p-5 shadow-sm"><p class="text-sm text-gray-500">累计帖子</p><p class="text-3xl font-bold text-purple-500 mt-1">{{ stats.totalPosts }}</p></div>
      <div class="bg-white rounded-xl p-5 shadow-sm"><p class="text-sm text-gray-500">累计浏览</p><p class="text-3xl font-bold text-indigo-500 mt-1">{{ stats.totalViews }}</p></div>
      <div class="bg-white rounded-xl p-5 shadow-sm"><p class="text-sm text-gray-500">待处理举报</p><p class="text-3xl font-bold text-red-500 mt-1">{{ stats.pendingReports }}</p></div>
    </div>
    <div class="bg-white rounded-xl p-6 shadow-sm">
      <h3 class="text-sm font-medium text-gray-600 mb-4">近 7 天趋势</h3>
      <div ref="chartEl" style="height: 260px"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import api from '@/api'

const stats = ref({ todayPosts: 0, todayComments: 0, totalPosts: 0, pendingReports: 0, totalViews: 0, trend: [] })
const chartEl = ref(null)

onMounted(async () => {
  try {
    const res = await api.get('/dashboard')
    stats.value = res.data || res
    // 兼容旧返回格式
    if (!stats.value.trend) stats.value.trend = []
    if (stats.value.totalViews === undefined) stats.value.totalViews = 0
  } catch (e) {}
  await nextTick()
  if (chartEl.value) renderChart()
})

const renderChart = () => {
  const canvas = document.createElement('canvas')
  canvas.width = chartEl.value.offsetWidth
  canvas.height = 260
  chartEl.value.innerHTML = ''
  chartEl.value.appendChild(canvas)
  const ctx = canvas.getContext('2d')
  const w = canvas.width, h = canvas.height

  // 使用真实趋势数据，没有则回退到模拟数据
  let days, posts, comments
  const trend = stats.value.trend || []
  if (trend.length > 0) {
    days = trend.map(t => t.label)
    posts = trend.map(t => t.posts)
    comments = trend.map(t => t.comments)
  } else {
    const now = new Date()
    days = Array.from({length: 7}, (_, i) => {
      const d = new Date(now); d.setDate(d.getDate() - 6 + i)
      return (d.getMonth()+1) + '/' + d.getDate()
    })
    posts = [3,5,2,8,6,4,7]
    comments = [8,12,6,18,14,9,16]
  }

  const maxVal = Math.max(...posts, ...comments, 1)
  const pad = { top: 20, right: 20, bottom: 40, left: 40 }
  const cw = w - pad.left - pad.right, ch = h - pad.top - pad.bottom

  // 网格
  ctx.strokeStyle = '#eee'; ctx.lineWidth = 1
  for (let i = 0; i <= 4; i++) {
    const y = pad.top + (ch / 4) * i
    ctx.beginPath(); ctx.moveTo(pad.left, y); ctx.lineTo(w - pad.right, y); ctx.stroke()
    ctx.fillStyle = '#999'; ctx.font = '11px sans-serif'; ctx.textAlign = 'right'
    ctx.fillText(Math.round(maxVal * (4-i) / 4), pad.left - 8, y + 4)
  }

  const drawLine = (data, color) => {
    ctx.strokeStyle = color; ctx.lineWidth = 2.5; ctx.beginPath()
    data.forEach((v, i) => {
      const x = pad.left + (cw / (data.length - 1)) * i
      const y = pad.top + ch - (v / maxVal) * ch
      i === 0 ? ctx.moveTo(x, y) : ctx.lineTo(x, y)
    }); ctx.stroke()
    data.forEach((v, i) => {
      const x = pad.left + (cw / (data.length - 1)) * i
      const y = pad.top + ch - (v / maxVal) * ch
      ctx.fillStyle = color; ctx.beginPath(); ctx.arc(x, y, 4, 0, Math.PI*2); ctx.fill()
    })
  }
  drawLine(posts, '#3B82F6')
  drawLine(comments, '#10B981')

  ctx.fillStyle = '#999'; ctx.font = '11px sans-serif'; ctx.textAlign = 'center'
  days.forEach((d, i) => ctx.fillText(d, pad.left + (cw/(days.length-1))*i, h - 10))

  ctx.fillStyle = '#3B82F6'; ctx.fillRect(w - 180, 10, 12, 12)
  ctx.fillStyle = '#333'; ctx.font = '12px sans-serif'; ctx.textAlign = 'left'
  ctx.fillText('发帖', w - 164, 21)
  ctx.fillStyle = '#10B981'; ctx.fillRect(w - 100, 10, 12, 12)
  ctx.fillText('评论', w - 84, 21)
}
</script>
