import request from '../request'

// 借阅统计
export const getBorrowStats = (params) => request.get('/admin/reports/borrow', { params })
// 读者统计
export const getReaderStats = () => request.get('/admin/reports/reader')
// 馆藏统计
export const getCollectionStats = () => request.get('/admin/reports/collection')
// 逾期统计
export const getOverdueStats = () => request.get('/admin/reports/overdue')
// 院系借阅占比
export const getDeptRatio = () => request.get('/admin/reports/dept-ratio')
// 分类藏书数量
export const getCategoryCount = () => request.get('/admin/reports/category-count')
