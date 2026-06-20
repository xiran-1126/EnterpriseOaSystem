import { useState, useEffect } from 'react'
import { Modal, Form, Input, Radio, message, Space, Button, Typography } from 'antd'
import { KeyOutlined, CopyOutlined, ReloadOutlined } from '@ant-design/icons'
import { resetUserPassword } from '@/api/system'
import type { UserVO } from '@/types/system'

const { Text } = Typography

interface ResetPasswordModalProps {
  visible: boolean
  userData?: UserVO | null
  onCancel: () => void
  onSuccess: () => void
}

const ResetPasswordModal = ({ visible, userData, onCancel, onSuccess }: ResetPasswordModalProps) => {
  const [form] = Form.useForm()
  const [loading, setLoading] = useState(false)
  const [resetMode, setResetMode] = useState<'auto' | 'manual'>('auto')
  const [generatedPassword, setGeneratedPassword] = useState('')
  const [copied, setCopied] = useState(false)

  const generateRandomPassword = () => {
    const upperCase = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'
    const lowerCase = 'abcdefghijklmnopqrstuvwxyz'
    const number = '0123456789'
    const specialChar = '!@#$%^&*'
    const allChar = upperCase + lowerCase + number + specialChar

    let password = ''
    for (let i = 0; i < 2; i++) {
      password += upperCase.charAt(Math.floor(Math.random() * upperCase.length))
    }
    for (let i = 0; i < 2; i++) {
      password += lowerCase.charAt(Math.floor(Math.random() * lowerCase.length))
    }
    for (let i = 0; i < 2; i++) {
      password += number.charAt(Math.floor(Math.random() * number.length))
    }
    for (let i = 0; i < 2; i++) {
      password += specialChar.charAt(Math.floor(Math.random() * specialChar.length))
    }
    for (let i = 0; i < 4; i++) {
      password += allChar.charAt(Math.floor(Math.random() * allChar.length))
    }

    password = password.split('').sort(() => Math.random() - 0.5).join('')
    setGeneratedPassword(password)
    return password
  }

  const copyPassword = () => {
    if (generatedPassword) {
      navigator.clipboard.writeText(generatedPassword)
      setCopied(true)
      setTimeout(() => setCopied(false), 2000)
    }
  }

  useEffect(() => {
    if (visible) {
      form.resetFields()
      setResetMode('auto')
      setGeneratedPassword('')
      setCopied(false)
      if (resetMode === 'auto') {
        generateRandomPassword()
      }
    }
  }, [visible])

  const handleSubmit = async () => {
    try {
      if (resetMode === 'manual') {
        await form.validateFields()
      }

      setLoading(true)

      const newPassword = resetMode === 'auto' ? generatedPassword : form.getFieldValue('newPassword')

      const result = await resetUserPassword({
        userId: userData!.id,
        newPassword,
      })

      if (resetMode === 'auto') {
        setGeneratedPassword(result)
      }

      message.success('密码重置成功')
      onSuccess()
    } catch (error: any) {
      if (error.errorFields) {
        return
      }
      console.error('重置密码失败', error)
    } finally {
      setLoading(false)
    }
  }

  return (
    <Modal
      title="重置密码"
      open={visible}
      onCancel={onCancel}
      onOk={handleSubmit}
      confirmLoading={loading}
      width={500}
      okText="确认重置"
      cancelText="取消"
      destroyOnClose
    >
      <div style={{ marginBottom: 16 }}>
        <Text type="secondary">
          重置 <strong>{userData?.realName}</strong>（{userData?.username}）的登录密码
        </Text>
      </div>

      <Form form={form} layout="vertical">
        <Form.Item label="重置方式">
          <Radio.Group
            value={resetMode}
            onChange={(e) => {
              setResetMode(e.target.value)
              if (e.target.value === 'auto') {
                generateRandomPassword()
              }
            }}
          >
            <Radio value="auto">系统自动生成</Radio>
            <Radio value="manual">手动设置</Radio>
          </Radio.Group>
        </Form.Item>

        {resetMode === 'auto' && (
          <Form.Item label="新密码">
            <div
              style={{
                padding: '12px 16px',
                background: '#f5f5f5',
                borderRadius: '6px',
                fontFamily: 'monospace',
                fontSize: '16px',
                letterSpacing: '2px',
                fontWeight: 'bold',
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
              }}
            >
              <span style={{ color: '#1677ff' }}>{generatedPassword}</span>
              <Space>
                <Button
                  type="text"
                  icon={<ReloadOutlined />}
                  onClick={generateRandomPassword}
                >
                  换一个
                </Button>
                <Button
                  type="text"
                  icon={<CopyOutlined />}
                  onClick={copyPassword}
                >
                  {copied ? '已复制' : '复制'}
                </Button>
              </Space>
            </div>
            <Text type="secondary" style={{ fontSize: '12px', marginTop: '8px', display: 'block' }}>
              请妥善保管新密码，建议用户首次登录后修改密码
            </Text>
          </Form.Item>
        )}

        {resetMode === 'manual' && (
          <>
            <Form.Item
              name="newPassword"
              label="新密码"
              rules={[
                { required: true, message: '请输入新密码' },
                { min: 6, max: 20, message: '密码长度为6-20位' },
                {
                  pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d!@#$%^&*()_+\-=\[\]{}|;:,.<>?]{6,20}$/,
                  message: '密码需包含大小写字母和数字',
                },
              ]}
              hasFeedback
            >
              <Input.Password placeholder="请输入新密码" />
            </Form.Item>

            <Form.Item
              name="confirmPassword"
              label="确认密码"
              dependencies={['newPassword']}
              rules={[
                { required: true, message: '请再次输入新密码' },
                ({ getFieldValue }) => ({
                  validator(_, value) {
                    if (!value || getFieldValue('newPassword') === value) {
                      return Promise.resolve()
                    }
                    return Promise.reject(new Error('两次输入的密码不一致'))
                  },
                }),
              ]}
              hasFeedback
            >
              <Input.Password placeholder="请再次输入新密码" />
            </Form.Item>
          </>
        )}
      </Form>

      <div
        style={{
          marginTop: 16,
          padding: '12px',
          background: '#fffbe6',
          borderRadius: '6px',
          border: '1px solid #ffe58f',
        }}
      >
        <Text type="warning" style={{ fontSize: '12px' }}>
          <KeyOutlined /> 重置密码后，该用户所有已登录设备将被强制下线
        </Text>
      </div>
    </Modal>
  )
}

export default ResetPasswordModal
