<template>
  <div>
    <h2 class="text-xl font-bold text-gray-800 mb-6">数据看板</h2>
    <div class="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
      <div class="bg-white rounded-xl p-5 shadow-sm"><p class="text-sm text-gray-500">今日发帖</p><p class="text-3xl font-bold text-blue-500 mt-1">{{ stats.todayPosts }}</p></div>
      <div class="bg-white rounded-xl p-5 shadow-sm"><p class="text-sm text-gray-500">今日评论</p><p class="text-3xl font-bold text-green-500 mt-1">{{ stats.todayComments }}</p></div>
      <div class="bg-white rounded-xl p-5 shadow-sm"><p class="text-sm text-gray-500">累计帖子</p><p class="text-3xl font-bold text-purple-500 mt-1">{{ stats.totalPosts }}</p></div>
      <div class="bg-white rounded-xl p-5 shadow-sm"><p class="text-sm text-gray-500">待处理举报</p><p class="text-3xl font-bold text-red-500 mt-1">{{ stats.pendingReports }}</p></div>
    </div>
    <div class="bg-white rounded-xl p-6 shadow-sm">
      <h3 class="text-sm font-medium text-gray-600 mb-4">近 7 天趋势</h3>
      <div ref="chartEl" style="height: 260px"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue'
import api from '@/api'

const stats = ref({ todayPosts: 0, todayComments: 0, totalPosts: 0, pendingReports: 0 })
const chartEl = ref(null)

onMounted(async () => {
  try { const res = await api.get('/dashboard'); stats.value = res.data || res } catch (e) {}
  await nextTick()
  if (chartEl.value) renderChart()
})

const renderChart = () => {
  // 简化版：用纯 HTML Canvas 替代 ECharts（避免额外依赖）
  const canvas = document.createElement('canvas')
  canvas.width = chartEl.value.offsetWidth
  canvas.height = 260
  chartEl.value.innerHTML = ''
  chartEl.value.appendChild(canvas)
  const ctx = canvas.getContext('2d')
  const w = canvas.width, h = canvas.height

  // 模拟 7 天数据
  const days = ['6/23','6/24','6/25','6/26','6/27','6/28','6/29']
  const posts = [3,5,2,8,6,4,7]
  const comments = [8,12,6,18,14,9,16]

  const maxVal = Math.max(...posts, ...comments)
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

  // 折线
  const drawLine = (data, color) => {
    ctx.strokeStyle = color; ctx.lineWidth = 2.5; ctx.beginPath()
    data.forEach((v, i) => {
      const x = pad.left + (cw / (data.length - 1)) * i
      const y = pad.top + ch - (v / maxVal) * ch
      i === 0 ? ctx.moveTo(x, y) : ctx.lineTo(x, y)
    }); ctx.stroke()
    // 点
    data.forEach((v, i) => {
      const x = pad.left + (cw / (data.length - 1)) * i
      const y = pad.top + ch - (v / maxVal) * ch
      ctx.fillStyle = color; ctx.beginPath(); ctx.arc(x, y, 4, 0, Math.PI*2); ctx.fill()
    })
  }
  drawLine(posts, '#3B82F6')
  drawLine(comments, '#10B981')

  // X 轴标签
  ctx.fillStyle = '#999'; ctx.font = '11px sans-serif'; ctx.textAlign = 'center'
  days.forEach((d, i) => ctx.fillText(d, pad.left + (cw/(days.length-1))*i, h - 10))

  // 图例
  ctx.fillStyle = '#3B82F6'; ctx.fillRect(w - 180, 10, 12, 12)
  ctx.fillStyle = '#333'; ctx.font = '12px sans-serif'; ctx.textAlign = 'left'
  ctx.fillText('发帖', w - 164, 21)
  ctx.fillStyle = '#10B981'; ctx.fillRect(w - 100, 10, 12, 12)
  ctx.fillText('评论', w - 84, 21)
}
</script>
