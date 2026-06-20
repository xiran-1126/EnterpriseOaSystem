import { useState } from 'react'
import { Form, Input, Button, message } from 'antd'
import { UserOutlined } from '@ant-design/icons'
import { forgotPasswordStep1 } from '@/api/auth'
import { validateUsername } from '@/utils/validate'

interface Step1AccountProps {
  onNext: (verifyId: string, maskedPhone: string) => void
}

export const Step1Account = ({ onNext }: Step1AccountProps) => {
  const [form] = Form.useForm()
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (values: { username: string }) => {
    setLoading(true)
    try {
      const res = await forgotPasswordStep1({ username: values.username })
      message.success('账号验证成功')
      onNext(res.verifyId, res.maskedPhone)
    } catch {
      // error handled in interceptor
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="forgot-step">
      <h3 className="step-title">账号验证</h3>
      <p className="step-desc">请输入您的账号信息</p>

      <Form
        form={form}
        layout="vertical"
        onFinish={handleSubmit}
        size="large"
        className="step-form"
      >
        <Form.Item
          label="账号"
          name="username"
          rules={[{ validator: validateUsername }]}
        >
          <Input
            prefix={<UserOutlined />}
            placeholder="请输入工号/手机号"
          />
        </Form.Item>

        <Form.Item>
          <Button type="primary" htmlType="submit" loading={loading} block>
            下一步
          </Button>
        </Form.Item>
      </Form>
    </div>
  )
}
