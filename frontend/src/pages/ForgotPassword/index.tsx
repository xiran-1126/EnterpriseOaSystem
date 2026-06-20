import { useState } from 'react'
import { Steps, Result, Button } from 'antd'
import { ArrowLeftOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { Step1Account } from './components/Step1Account'
import { Step2SmsCode } from './components/Step2SmsCode'
import { Step3ResetPassword } from './components/Step3ResetPassword'
import './style.css'

const ForgotPasswordPage = () => {
  const navigate = useNavigate()
  const [current, setCurrent] = useState(0)
  const [verifyId, setVerifyId] = useState('')
  const [maskedPhone, setMaskedPhone] = useState('')
  const [smsCode, setSmsCode] = useState('')

  const handleStep1Next = (id: string, phone: string) => {
    setVerifyId(id)
    setMaskedPhone(phone)
    setCurrent(1)
  }

  const handleStep2Next = (code: string) => {
    setSmsCode(code)
    setCurrent(2)
  }

  const handleStep3Next = () => {
    setCurrent(3)
  }

  const handleBackToLogin = () => {
    navigate('/login')
  }

  return (
    <div className="forgot-password-page">
      <div className="forgot-password-card">
        <div className="forgot-header">
          <Button
            type="text"
            icon={<ArrowLeftOutlined />}
            onClick={handleBackToLogin}
            className="back-btn"
          >
            返回登录
          </Button>
          <h2 className="forgot-title">忘记密码</h2>
        </div>

        <Steps
          current={current}
          items={[
            { title: '账号验证' },
            { title: '短信验证' },
            { title: '重置密码' },
            { title: '完成' },
          ]}
          className="forgot-steps"
        />

        <div className="forgot-content">
          {current === 0 && (
            <Step1Account onNext={handleStep1Next} />
          )}
          {current === 1 && (
            <Step2SmsCode
              verifyId={verifyId}
              maskedPhone={maskedPhone}
              onNext={handleStep2Next}
              onPrev={() => setCurrent(0)}
            />
          )}
          {current === 2 && (
            <Step3ResetPassword
              verifyId={verifyId}
              smsCode={smsCode}
              onNext={handleStep3Next}
              onPrev={() => setCurrent(1)}
            />
          )}
          {current === 3 && (
            <Result
              status="success"
              title="密码重置成功"
              subTitle="您的密码已成功重置，请使用新密码登录"
              extra={
                <Button type="primary" onClick={handleBackToLogin}>
                  返回登录
                </Button>
              }
            />
          )}
        </div>
      </div>
    </div>
  )
}

export default ForgotPasswordPage
