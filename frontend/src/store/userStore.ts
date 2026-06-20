import { create } from 'zustand'
import { UserInfo } from '@/types/auth'
import { getToken, setToken, removeToken, getUserInfo, setUserInfo, removeUserInfo } from '@/utils/storage'

interface UserState {
  token: string | null
  userInfo: UserInfo | null
  isLoggedIn: boolean
  setAuth: (token: string, userInfo: UserInfo) => void
  clearAuth: () => void
  initAuth: () => void
}

export const useUserStore = create<UserState>((set) => ({
  token: null,
  userInfo: null,
  isLoggedIn: false,

  setAuth: (token: string, userInfo: UserInfo) => {
    setToken(token)
    setUserInfo(userInfo)
    set({ token, userInfo, isLoggedIn: true })
  },

  clearAuth: () => {
    removeToken()
    removeUserInfo()
    set({ token: null, userInfo: null, isLoggedIn: false })
  },

  initAuth: () => {
    const token = getToken()
    const userInfo = getUserInfo<UserInfo>()
    if (token && userInfo) {
      set({ token, userInfo, isLoggedIn: true })
    }
  },
}))
