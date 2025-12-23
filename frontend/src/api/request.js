import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
const service = axios.create({
  baseURL: '/api', // 通过vite代理到后端
  timeout: 10000
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    
    // 如果后端返回的是字符串（成功/错误消息）
    if (typeof res === 'string') {
      if (res.includes('错误') || res.includes('失败')) {
        ElMessage.error(res)
        return Promise.reject(new Error(res))
      } else {
        ElMessage.success(res)
        return res
      }
    }
    
    return res
  },
  error => {
    console.error('响应错误:', error)
    ElMessage.error(error.message || '请求失败')
    return Promise.reject(error)
  }
)

export default service

