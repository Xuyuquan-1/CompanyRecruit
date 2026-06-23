import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../store/user'
import router from '../router'

/**
 * 创建 Axios 实例
 */
const request = axios.create({
  baseURL: '',     // 已通过 Vite proxy 代理，不需要额外前缀
  timeout: 30000   // 超时时间 30 秒
})

/**
 * 请求拦截器
 * 在每个请求发出前，自动带上 Token
 */
request.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers['Authorization'] = 'Bearer ' + userStore.token
    }
    return config
  },
  error => Promise.reject(error)
)

/**
 * 响应拦截器
 * 统一处理后端返回结果
 */
request.interceptors.response.use(
  response => {
    // 二进制响应（如 Excel 导出）直接返回
    if (response.config.responseType === 'blob') {
      return response
    }

    const res = response.data

    // 后端返回的 code 不是 200，说明出错了
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')

      // 401: Token过期或未登录
      if (res.code === 401) {
        const userStore = useUserStore()
        userStore.logout()
        router.push('/login')
      }

      return Promise.reject(new Error(res.message || '请求失败'))
    }

    return res
  },
  error => {
    // HTTP 状态码错误（如 401, 403, 500 等）
    if (error.response) {
      const status = error.response.status
      if (status === 401) {
        ElMessage.error('登录已过期，请重新登录')
        const userStore = useUserStore()
        userStore.logout()
        router.push('/login')
      } else if (status === 403) {
        ElMessage.error('没有权限访问')
      } else {
        ElMessage.error(error.response.data?.message || '服务器错误')
      }
    } else {
      ElMessage.error('网络异常，请检查网络连接')
    }
    return Promise.reject(error)
  }
)

export default request