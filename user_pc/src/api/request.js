import axios from 'axios'
import { useUserStore } from '../stores/user'
import router from '../router'
import { show } from '../utils/toast'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

request.interceptors.request.use(config => {
  const userStore = useUserStore()
  if (userStore.token) {
    config.headers.Authorization = `Bearer ${userStore.token}`
  }
  return config
})

request.interceptors.response.use(
  res => {
    const data = res.data
    if (data.code === 200) {
      return data.data
    }
    if (data.code === 401) {
      const userStore = useUserStore()
      userStore.logout()
      show('登录已过期，请重新登录')
      router.push({ path: '/login', query: { redirect: router.currentRoute.value.fullPath } })
    } else {
      show(data.message || '请求失败')
    }
    return Promise.reject(new Error(data.message || '请求失败'))
  },
  err => {
    const msg = err.response?.data?.message || '网络异常，请确认后端服务(8080)已启动'
    show(msg)
    return Promise.reject(new Error(msg))
  }
)

export default request