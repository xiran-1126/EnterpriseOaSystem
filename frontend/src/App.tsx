import { Routes, Route, Navigate } from 'react-router-dom'
import { AuthGuard } from '@/router/AuthGuard'
import LoginPage from '@/pages/Login'
import ForgotPasswordPage from '@/pages/ForgotPassword'
import MainLayout from '@/layouts/MainLayout'
import DashboardPage from '@/pages/Dashboard'
import UserManagementPage from '@/pages/UserManagement'

function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/forgot-password" element={<ForgotPasswordPage />} />
      <Route
        path="/"
        element={
          <AuthGuard>
            <MainLayout />
          </AuthGuard>
        }
      >
        <Route index element={<Navigate to="/dashboard" replace />} />
        <Route path="dashboard" element={<DashboardPage />} />
        <Route path="system/user" element={<UserManagementPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  )
}

export default App
