import { useState } from 'react'
import { Form, Input, Button, message, Progress } from 'antd'
import { LockOutlined } from '@ant-design/icons'
import { resetPassword } from '@/api/auth'
import { validatePassword, validateConfirmPassword } from '@/utils/validate'

interface Step3ResetPasswordProps {
  verifyId: string
  smsCode: string
  onNext: () => void
  onPrev: () => void
}

const getPasswordStrength = (password: string): { level: number; text: string; color: string } => {
  if (!password) return { level: 0, text: '', color: '' }
  let level = 0
  if (password.length >= 6) level++
  if (/[a-z]/.test(password) && /[A-Z]/.test(password)) level++
  if (/\d/.test(password)) level++
  if (/[!@#$%^&*(),.?":{}|<>]/.test(password)) level++

  if (level <= 1) return { level: 1, text: '弱', color: '#ff4d4f' }
  if (level === 2) return { level: 2, text: '中', color: '#faad14' }
  if (level === 3) return { level: 3, text: '强', color: '#52c41a' }
  return { level: 4, text: '很强', color: '#1890ff' }
}

export const Step3ResetPassword = ({ verifyId, smsCode, onNext, onPrev }: Step3ResetPasswordProps) => {
  const [form] = Form.useForm()
  const [loading, setLoading] = useState(false)
  const [password, setPassword] = useState('')

  const strength = getPasswordStrength(password)

  const handleSubmit = async (values: { newPassword: string; confirmPassword: string }) => {
    setLoading(true)
    try {
      await resetPassword({
        verifyId,
        smsCode,
        newPassword: values.newPassword,
      })
      message.success('密码重置成功')
      onNext()
    } catch {
      // error handled in interceptor
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="forgot-step">
      <h3 className="step-title">重置密码</h3>
      <p className="step-desc">请设置您的新密码</p>

      <Form
        form={form}
        layout="vertical"
        onFinish={handleSubmit}
        size="large"
        className="step-form"
      >
        <Form.Item
          label="新密码"
          name="newPassword"
          rules={[{ validator: validatePassword }]}
        >
          <Input.Password
            prefix={<LockOutlined />}
            placeholder="请输入新密码"
            onChange={(e) => setPassword(e.target.value)}
          />
        </Form.Item>

        {password && (
          <div className="password-strength">
            <Progress
              percent={strength.level * 25}
              showInfo={false}
              strokeColor={strength.color}
              size="small"
            />
            <span className="strength-text" style={{ color: strength.color }}>
              密码强度：{strength.text}
            </span>
          </div>
        )}

        <Form.Item
          label="确认密码"
          name="confirmPassword"
          dependencies={['newPassword']}
          rules={[
            { validator: validateConfirmPassword(form.getFieldValue('newPassword')) },
          ]}
        >
          <Input.Password
            prefix={<LockOutlined />}
            placeholder="请再次输入新密码"
          />
        </Form.Item>

        <Form.Item>
          <div className="step-buttons">
            <Button onClick={onPrev} block>
              上一步
            </Button>
            <Button type="primary" htmlType="submit" loading={loading} block>
              确认重置
            </Button>
          </div>
        </Form.Item>
      </Form>
    </div>
  )
}
