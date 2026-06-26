import request from '../request'

// 系统配置
export const getConfigs = () => request.get('/admin/config')
export const updateConfigs = (data) => request.put('/admin/config', data)
// 操作日志
export const getLogs = (params) => request.get('/admin/logs', { params })
// 公告
export const getAnnouncements = (params) => request.get('/announcements', { params })
export const getAnnouncement = (id) => request.get(`/announcements/${id}`)
export const publishAnnouncement = (data) => request.post('/admin/announcements', data)
export const toggleTop = (id) => request.put(`/admin/announcements/${id}/top`)
export const deleteAnnouncement = (id) => request.delete(`/admin/announcements/${id}`)
// 反馈
export const submitFeedback = (data) => request.post('/reader/feedback', data)
export const myFeedback = (params) => request.get('/reader/feedback', { params })
export const getFeedbackList = (params) => request.get('/admin/feedback', { params })
export const replyFeedback = (id, data) => request.post(`/admin/feedback/${id}/reply`, data)
