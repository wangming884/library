import request from '../request'

// 管理员登录
export const adminLogin = (data) => request.post('/auth/login', data)
// 读者登录
export const readerLogin = (data) => request.post('/auth/reader-login', data)
// 获取当前用户信息
export const getInfo = () => request.get('/auth/info')
// 修改密码
export const changePassword = (data) => request.put('/auth/password', data)
