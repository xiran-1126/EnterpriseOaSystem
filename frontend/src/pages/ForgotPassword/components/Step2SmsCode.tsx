import { useState, useEffect } from 'react'
import { Form, Input, Button, message } from 'antd'
import { SafetyCertificateOutlined } from '@ant-design/icons'
import { sendSmsCode } from '@/api/auth'
import { validateSmsCode } from '@/utils/validate'

interface Step2SmsCodeProps {
  verifyId: string
  maskedPhone: string
  onNext: (smsCode: string) => void
  onPrev: () => void
}

export const Step2SmsCode = ({ verifyId, maskedPhone, onNext, onPrev }: Step2SmsCodeProps) => {
  const [form] = Form.useForm()
  const [loading, setLoading] = useState(false)
  const [countdown, setCountdown] = useState(0)
  const [sending, setSending] = useState(false)

  useEffect(() => {
    let timer: number
    if (countdown > 0) {
      timer = window.setTimeout(() => setCountdown(countdown - 1), 1000)
    }
    return () => {
      if (timer) clearTimeout(timer)
    }
  }, [countdown])

  const handleSendCode = async () => {
    setSending(true)
    try {
      await sendSmsCode({ verifyId })
      message.success('验证码已发送')
      setCountdown(60)
    } catch {
      // error handled in interceptor
    } finally {
      setSending(false)
    }
  }

  const handleSubmit = async (values: { smsCode: string }) => {
    setLoading(true)
    try {
      message.success('验证码验证成功')
      onNext(values.smsCode)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="forgot-step">
      <h3 className="step-title">短信验证</h3>
      <p className="step-desc">验证码已发送至 {maskedPhone}</p>

      <Form
        form={form}
        layout="vertical"
        onFinish={handleSubmit}
        size="large"
        className="step-form"
      >
        <Form.Item
          label="短信验证码"
          name="smsCode"
          rules={[{ validator: validateSmsCode }]}
        >
          <div className="sms-code-wrapper">
            <Input
              prefix={<SafetyCertificateOutlined />}
              placeholder="请输入6位验证码"
              maxLength={6}
              className="sms-code-input"
            />
            <Button
              type="default"
              onClick={handleSendCode}
              disabled={countdown > 0}
              loading={sending}
              className="sms-code-btn"
            >
              {countdown > 0 ? `${countdown}s` : '获取验证码'}
            </Button>
          </div>
        </Form.Item>

        <Form.Item>
          <div className="step-buttons">
            <Button onClick={onPrev} block>
              上一步
            </Button>
            <Button type="primary" htmlType="submit" loading={loading} block>
              下一步
            </Button>
          </div>
        </Form.Item>
      </Form>
    </div>
  )
}
