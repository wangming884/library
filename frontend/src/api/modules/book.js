import request from '../request'

// 图书搜索（公开）
export const searchBooks = (params) => request.get('/book/search', { params })
// 图书详情
export const getBook = (id) => request.get(`/book/${id}`)
// 可借副本
export const getAvailableCopies = (id) => request.get(`/book/${id}/available-copies`)
// 借阅排行
export const getBorrowRank = (limit = 10) => request.get('/book/rank', { params: { limit } })
// 新书推荐
export const getNewBooks = (limit = 10) => request.get('/book/new', { params: { limit } })
// 管理员图书列表
export const getBooks = (params) => request.get('/admin/books', { params })
// 添加图书
export const addBook = (data) => request.post('/admin/books', data)
// 更新图书
export const updateBook = (id, data) => request.put(`/admin/books/${id}`, data)
// 上传图书封面
export const uploadBookCover = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/admin/books/cover', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
// 删除图书
export const deleteBook = (id) => request.delete(`/admin/books/${id}`)
// 副本管理
export const getCopies = (params) => request.get('/admin/copies', { params })
export const addCopy = (data) => request.post('/admin/copies', data)
export const updateCopyStatus = (id, status) => request.put(`/admin/copies/${id}/status`, null, { params: { status } })
export const getCopyByBarcode = (barcode) => request.get(`/admin/copies/barcode/${barcode}`)
// 分类管理
export const getCategories = () => request.get('/categories')
export const addCategory = (data) => request.post('/admin/categories', data)
export const updateCategory = (id, data) => request.put(`/admin/categories/${id}`, data)
export const deleteCategory = (id) => request.delete(`/admin/categories/${id}`)
// 标签管理
export const getTags = () => request.get('/admin/tags')
export const addTag = (data) => request.post('/admin/tags', data)
export const deleteTag = (id) => request.delete(`/admin/tags/${id}`)
