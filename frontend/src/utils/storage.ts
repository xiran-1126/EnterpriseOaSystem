const TOKEN_KEY = 'oa_access_token'
const USER_INFO_KEY = 'oa_user_info'

export const getToken = (): string | null => {
  return localStorage.getItem(TOKEN_KEY)
}

export const setToken = (token: string): void => {
  localStorage.setItem(TOKEN_KEY, token)
}

export const removeToken = (): void => {
  localStorage.removeItem(TOKEN_KEY)
}

export const getUserInfo = <T = unknown>(): T | null => {
  const userInfo = localStorage.getItem(USER_INFO_KEY)
  return userInfo ? JSON.parse(userInfo) : null
}

export const setUserInfo = (userInfo: unknown): void => {
  localStorage.setItem(USER_INFO_KEY, JSON.stringify(userInfo))
}

export const removeUserInfo = (): void => {
  localStorage.removeItem(USER_INFO_KEY)
}

export const clearAuth = (): void => {
  removeToken()
  removeUserInfo()
}
