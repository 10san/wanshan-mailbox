import api from './index'

// 帖子
export const getPosts = (params) => api.get('/posts', { params })
export const getPostDetail = (id) => api.get(`/posts/${id}`)
export const createPost = (data) => api.post('/posts', data)
export const deletePost = (id, password) => api.delete(`/posts/${id}`, { data: { password } })
export const likePost = (id) => api.post(`/posts/${id}/like`)
export const hugPost = (id) => api.post(`/posts/${id}/hug`)
export const feelPost = (id) => api.post(`/posts/${id}/feel`)

// 评论
export const getComments = (postId) => api.get(`/posts/${postId}/comments`)
export const createComment = (postId, content) => api.post(`/posts/${postId}/comments`, { content })

// 搜索
export const searchPosts = (q, page = 1) => api.get('/search', { params: { q, page } })

// 举报
export const reportContent = (data) => api.post('/reports', data)

// 通用
export const getMyInfo = () => api.get('/me')
export const getTags = () => api.get('/tags')
