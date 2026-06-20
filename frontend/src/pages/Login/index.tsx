import { BrandPanel } from './components/BrandPanel'
import { LoginForm } from './components/LoginForm'
import './style.css'
import './components/brandPanel.css'

const LoginPage = () => {
  return (
    <div className="login-page">
      <BrandPanel />
      <LoginForm />
    </div>
  )
}

export default LoginPage
