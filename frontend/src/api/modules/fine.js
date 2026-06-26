import request from '../request'

// 罚款列表
export const getFines = (params) => request.get('/admin/fines', { params })
// 缴费
export const payFine = (id, data) => request.post(`/admin/fines/${id}/pay`, data)
// 财务统计
export const getFinancialSummary = (params) => request.get('/admin/fines/summary', { params })
// 欠费台账
export const getDebtList = (params) => request.get('/admin/fines/debt-list', { params })
// 读者自己的罚款
export const myFines = (params) => request.get('/reader/fines', { params })
export const myUnpaidAmount = () => request.get('/reader/fines/unpaid')
