<template>
  <div class="mb-4">
    <div v-if="!imageUrl" class="border-2 border-dashed border-warm-200 rounded-2xl p-6 text-center cursor-pointer hover:border-warm-400 transition-colors" @click="triggerUpload">
      <span class="text-3xl">📷</span>
      <p class="text-sm text-dusk-400 mt-2">添加图片（可选，最大 5MB）</p>
    </div>
    <div v-else class="relative rounded-2xl overflow-hidden">
      <img :src="imageUrl" class="w-full max-h-64 object-cover rounded-2xl" />
      <button @click="removeImage" class="absolute top-2 right-2 w-8 h-8 bg-black/50 text-white rounded-full flex items-center justify-center hover:bg-black/70">✕</button>
    </div>
    <div v-if="uploading" class="mt-2 bg-warm-50 rounded-full h-2 overflow-hidden">
      <div class="h-full bg-warm-500 rounded-full animate-pulse" style="width: 100%"></div>
    </div>
    <p v-if="error" class="text-red-500 text-xs mt-1">{{ error }}</p>
    <input ref="fileInput" type="file" accept="image/jpeg,image/png,image/webp" class="hidden" @change="handleFile" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'

const emit = defineEmits(['update:imageUrl'])
const props = defineProps({ imageUrl: String })

const fileInput = ref(null)
const uploading = ref(false)
const error = ref('')

const triggerUpload = () => {
  fileInput.value?.click()
}

const handleFile = async (e) => {
  const file = e.target.files?.[0]
  if (!file) return
  if (file.size > 5 * 1024 * 1024) { error.value = '图片不能超过 5MB'; return }
  if (!['image/jpeg', 'image/png', 'image/webp'].includes(file.type)) { error.value = '仅支持 JPG/PNG/WebP'; return }

  // 前端压缩
  const compressed = await compressImage(file)

  uploading.value = true; error.value = ''
  try {
    const formData = new FormData()
    formData.append('file', compressed, file.name)
    const res = await axios.post('/api/v1/upload/image', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    const data = res.data?.data || res.data
    emit('update:imageUrl', data.url)
  } catch (e) {
    error.value = e?.response?.data?.message || '上传失败'
  } finally { uploading.value = false }
}

const removeImage = () => {
  emit('update:imageUrl', '')
}

const compressImage = (file) => {
  return new Promise((resolve) => {
    const img = new Image()
    img.onload = () => {
      const canvas = document.createElement('canvas')
      const maxW = 1200
      let w = img.width, h = img.height
      if (w > maxW) { h = h * maxW / w; w = maxW }
      canvas.width = w; canvas.height = h
      const ctx = canvas.getContext('2d')
      ctx.drawImage(img, 0, 0, w, h)
      canvas.toBlob((blob) => resolve(new File([blob], file.name, { type: 'image/jpeg' })), 'image/jpeg', 0.8)
    }
    img.src = URL.createObjectURL(file)
  })
}
</script>
