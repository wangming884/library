import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import router from '../router'

// 自动检测 baseURL：开发环境用 /api（由 vite proxy 转发），生产环境用 /library/api
const isDev = import.meta.env.DEV
const request = axios.create({
  baseURL: isDev ? '/api' : '/library/api',
  timeout: 15000,
})

// 请求拦截器
request.interceptors.request.use(config => {
  const userStore = useUserStore()
  if (userStore.token) {
    config.headers['Authorization'] = 'Bearer ' + userStore.token
  }
  return config
})

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 200) {
      return res
    }
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message))
  },
  error => {
    if (error.response) {
      if (error.response.status === 401) {
        const userStore = useUserStore()
        const loginPath = userStore.roleKey === 'reader' ? '/reader-login' : '/login'
        userStore.logout()
        router.push(loginPath)
        ElMessage.error('登录已过期，请重新登录')
      } else if (error.response.status === 403) {
        ElMessage.error('权限不足')
      } else {
        ElMessage.error(error.response.data?.message || '服务器错误')
      }
    } else {
      ElMessage.error('网络错误，请检查连接')
    }
    return Promise.reject(error)
  }
)

export default request
