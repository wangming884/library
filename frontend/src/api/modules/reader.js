import request from '../request'

// 读者列表
export const getReaders = (params) => request.get('/admin/readers', { params })
// 读者详情
export const getReader = (id) => request.get(`/admin/readers/${id}`)
// 添加读者
export const addReader = (data) => request.post('/admin/readers', data)
// 更新读者
export const updateReader = (id, data) => request.put(`/admin/readers/${id}`, data)
// 挂失
export const reportLoss = (id) => request.put(`/admin/readers/${id}/loss`)
// 补办
export const reissue = (id, data) => request.put(`/admin/readers/${id}/reissue`, data)
// 冻结
export const freezeReader = (id) => request.put(`/admin/readers/${id}/freeze`)
// 解冻
export const unfreezeReader = (id) => request.put(`/admin/readers/${id}/unfreeze`)
// 黑名单
export const addBlacklist = (id) => request.put(`/admin/readers/${id}/blacklist`)
export const removeBlacklist = (id) => request.put(`/admin/readers/${id}/remove-blacklist`)
// 读者类型
export const getReaderTypes = () => request.get('/admin/reader-types')
export const addReaderType = (data) => request.post('/admin/reader-types', data)
// 读者注册
export const register = (data) => request.post('/reader/register', data)
// 读者个人中心
export const getProfile = () => request.get('/reader/profile')
export const updateProfile = (data) => request.put('/reader/profile', data)
export const changeReaderPassword = (data) => request.put('/reader/password', data)
