import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import { message } from 'antd'
import { getToken, clearAuth } from '@/utils/storage'
import { ApiResponse } from '@/types/auth'

const request: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getToken()
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data as ApiResponse
    if (res.code !== 200) {
      message.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res.data as unknown as AxiosResponse
  },
  (error) => {
    if (error.response) {
      const { status } = error.response
      if (status === 401) {
        clearAuth()
        message.error('登录已过期，请重新登录')
        window.location.href = '/login'
      } else if (status === 403) {
        message.error('没有权限访问')
      } else if (status === 500) {
        message.error('服务器错误')
      } else {
        message.error(error.response.data?.message || '请求失败')
      }
    } else {
      message.error('网络错误，请稍后重试')
    }
    return Promise.reject(error)
  }
)

export const get = <T = unknown>(url: string, config?: AxiosRequestConfig): Promise<T> => {
  return request.get(url, config) as unknown as Promise<T>
}

export const post = <T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> => {
  return request.post(url, data, config) as unknown as Promise<T>
}

export const put = <T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> => {
  return request.put(url, data, config) as unknown as Promise<T>
}

export const del = <T = unknown>(url: string, config?: AxiosRequestConfig): Promise<T> => {
  return request.delete(url, config) as unknown as Promise<T>
}

export default request
