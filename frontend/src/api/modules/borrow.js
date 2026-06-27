import request from '../request'

// 借书
export const borrowBook = (data) => request.post('/admin/borrow', data)
// 还书
export const returnBook = (id) => request.post(`/admin/return/${id}`)
// 续借（管理员）
export const renewBook = (id) => request.post(`/admin/renew/${id}`)
// 借阅记录
export const getBorrowRecords = (params) => request.get('/admin/borrow-records', { params })
// 预约列表
export const getReservations = (params) => request.get('/admin/reservations', { params })
// 检测逾期
export const checkOverdue = () => request.post('/admin/check-overdue')
// 催还
export const sendReminders = () => request.post('/admin/send-reminders')
// 读者自助
export const readerBorrowBook = (data) => request.post('/reader/borrow', data)
export const reserveBook = (data) => request.post('/reader/reserve', data)
export const cancelReservation = (id) => request.delete(`/reader/reservations/${id}`)
export const readerRenew = (id) => request.post(`/reader/renew/${id}`)
export const readerReturnBook = (id) => request.post(`/reader/return/${id}`)
export const myBorrowRecords = (params) => request.get('/reader/borrow-records', { params })
export const myReservations = (params) => request.get('/reader/reservations', { params })
