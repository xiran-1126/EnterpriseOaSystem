import { useState, useEffect } from 'react'
import { Form, Input, Button, Checkbox, Modal, message } from 'antd'
import { UserOutlined, LockOutlined, SafetyOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { login, getCaptcha } from '@/api/auth'
import { useUserStore } from '@/store/userStore'
import { encryptPassword } from '@/utils/auth'
import { LoginRequest, LoginResponse } from '@/types/auth'

const REMEMBER_KEY = 'oa_remember_username'
const REMEMBER_EXPIRE_DAYS = 7

export const LoginForm = () => {
  const [form] = Form.useForm()
  const navigate = useNavigate()
  const { setAuth } = useUserStore()
  const [loading, setLoading] = useState(false)
  const [captchaKey, setCaptchaKey] = useState('')
  const [captchaImage, setCaptchaImage] = useState('')
  const [remember, setRemember] = useState(false)
  const [newIpModalVisible, setNewIpModalVisible] = useState(false)
  const [loginResult, setLoginResult] = useState<LoginResponse | null>(null)

  const fetchCaptcha = async () => {
    try {
      const res = await getCaptcha()
      setCaptchaKey(res.captchaKey)
      setCaptchaImage(res.captchaImage)
    } catch {
      message.error('获取验证码失败')
    }
  }

  const getRememberedUsername = (): string | null => {
    try {
      const stored = localStorage.getItem(REMEMBER_KEY)
      if (!stored) return null
      const data = JSON.parse(stored)
      const now = Date.now()
      if (now > data.expireAt) {
        localStorage.removeItem(REMEMBER_KEY)
        return null
      }
      return data.username
    } catch {
      localStorage.removeItem(REMEMBER_KEY)
      return null
    }
  }

  const saveRememberedUsername = (username: string) => {
    const expireAt = Date.now() + REMEMBER_EXPIRE_DAYS * 24 * 60 * 60 * 1000
    const data = { username, expireAt }
    localStorage.setItem(REMEMBER_KEY, JSON.stringify(data))
  }

  const clearRememberedUsername = () => {
    localStorage.removeItem(REMEMBER_KEY)
  }

  useEffect(() => {
    fetchCaptcha()
    const savedUsername = getRememberedUsername()
    if (savedUsername) {
      form.setFieldsValue({ username: savedUsername })
      setRemember(true)
    }
  }, [form])

  const handleSubmit = async (values: { username: string; password: string; captcha: string }) => {
    if (loading) return
    setLoading(true)
    try {
      const encryptedPassword = encryptPassword(values.password)
      const requestData: LoginRequest = {
        username: values.username,
        password: encryptedPassword,
        captcha: values.captcha,
        captchaKey,
      }
      const res = await login(requestData)

      if (remember) {
        saveRememberedUsername(values.username)
      } else {
        clearRememberedUsername()
      }

      if (res.isNewIp) {
        setLoginResult(res)
        setNewIpModalVisible(true)
      } else {
        completeLogin(res)
      }
    } catch {
      fetchCaptcha()
    } finally {
      setLoading(false)
    }
  }

  const completeLogin = (res: LoginResponse) => {
    setAuth(res.token, res.userInfo)
    message.success('登录成功')
    navigate('/dashboard')
  }

  const handleNewIpOk = () => {
    if (loginResult) {
      completeLogin(loginResult)
    }
    setNewIpModalVisible(false)
    setLoginResult(null)
  }

  const handleNewIpCancel = () => {
    setNewIpModalVisible(false)
    setLoginResult(null)
    fetchCaptcha()
  }

  const handleCaptchaClick = () => {
    if (!loading) {
      fetchCaptcha()
    }
  }

  const handleUsernameChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value
    form.setFieldsValue({ username: value.trim() })
  }

  return (
    <div className="login-form-container">
      <h2 className="login-title">欢迎登录</h2>
      <p className="login-subtitle">请输入您的账号信息</p>

      <Form
        form={form}
        name="login"
        onFinish={handleSubmit}
        size="large"
        className="login-form"
        validateTrigger="onBlur"
      >
        <Form.Item
          name="username"
          rules={[{ required: true, message: '请输入账号' }]}
          normalize={(value) => value?.trim() ?? ''}
        >
          <Input
            prefix={<UserOutlined />}
            placeholder="请输入工号/绑定手机号"
            autoComplete="username"
            onChange={handleUsernameChange}
          />
        </Form.Item>

        <Form.Item
          name="password"
          rules={[{ required: true, message: '请输入密码' }]}
        >
          <Input.Password
            prefix={<LockOutlined />}
            placeholder="请输入密码"
            autoComplete="current-password"
            iconRender={(visible) => (visible ? '🙈' : '👁️')}
          />
        </Form.Item>

        <Form.Item
          name="captcha"
          rules={[{ required: true, message: '请输入验证码' }]}
        >
          <div className="captcha-wrapper">
            <Input
              prefix={<SafetyOutlined />}
              placeholder="请输入验证码"
              maxLength={4}
              className="captcha-input"
            />
            <img
              src={captchaImage}
              alt="验证码"
              className="captcha-image"
              onClick={handleCaptchaClick}
              title="点击刷新验证码"
              style={{ cursor: loading ? 'not-allowed' : 'pointer', opacity: loading ? 0.6 : 1 }}
            />
          </div>
        </Form.Item>

        <Form.Item>
          <div className="login-options">
            <Checkbox checked={remember} onChange={(e) => setRemember(e.target.checked)}>
              记住账号
            </Checkbox>
            <a className="forgot-password-link" href="/forgot-password">
              忘记密码？
            </a>
          </div>
        </Form.Item>

        <Form.Item>
          <Button
            type="primary"
            htmlType="submit"
            loading={loading}
            disabled={loading}
            block
            className="login-button"
          >
            登录
          </Button>
        </Form.Item>
      </Form>

      <div className="login-footer">
        <a href="#privacy">隐私政策</a>
        <span className="divider">|</span>
        <a href="#terms">用户协议</a>
        <span className="divider">|</span>
        <a href="#support">技术支持</a>
      </div>

      <Modal
        title="安全提醒"
        open={newIpModalVisible}
        onOk={handleNewIpOk}
        onCancel={handleNewIpCancel}
        okText="确认登录"
        cancelText="取消"
        centered
      >
        <p>检测到您在新的设备或IP地址登录，请注意账号安全。</p>
        <p>如非本人操作，请及时修改密码。</p>
      </Modal>
    </div>
  )
}
