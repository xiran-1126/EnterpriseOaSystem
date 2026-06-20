import { ReactNode, useEffect } from 'react'
import { Navigate, useLocation } from 'react-router-dom'
import { useUserStore } from '@/store/userStore'

interface AuthGuardProps {
  children: ReactNode
}

export const AuthGuard = ({ children }: AuthGuardProps) => {
  const { isLoggedIn, initAuth } = useUserStore()
  const location = useLocation()

  useEffect(() => {
    initAuth()
  }, [initAuth])

  if (!isLoggedIn) {
    return <Navigate to="/login" state={{ from: location.pathname }} replace />
  }

  return <>{children}</>
}
