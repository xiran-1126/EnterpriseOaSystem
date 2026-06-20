import type { MockMethod } from 'vite-plugin-mock'
import CryptoJS from 'crypto-js'

const SECRET_KEY = 'oa-system-secret-key-2024'

const decryptPassword = (ciphertext: string): string => {
  try {
    const bytes = CryptoJS.AES.decrypt(ciphertext, SECRET_KEY)
    return bytes.toString(CryptoJS.enc.Utf8)
  } catch {
      return ''
    }
}

const generateCaptcha = (): { text: string; image: string } => {
  const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789'
  let text = ''
  for (let i = 0; i < 4; i++) {
    text += chars.charAt(Math.floor(Math.random() * chars.length))
  }

  const canvas = `
    <svg xmlns="http://www.w3.org/2000/svg" width="100" height="40" viewBox="0 0 100 40">
      <rect width="100" height="40" fill="#f0f2f5" rx="4"/>
      <text x="50" y="28" text-anchor="middle" font-family="Arial, sans-serif" font-size="24" font-weight="bold" fill="#1890ff" style="letter-spacing: 4px;">
        ${text}
      </text>
      <line x1="10" y1="10" x2="90" y2="30" stroke="#ddd" stroke-width="1"/>
      <line x1="10" y1="30" x2="90" y2="10" stroke="#ddd" stroke-width="1"/>
    </svg>
  `

  const base64 = Buffer.from(canvas).toString('base64')
  return {
    text,
    image: `data:image/svg+xml;base64,${base64}`,
  }
}

const captchaStore = new Map<string, string>()

const mockUsers = [
  {
    id: 1,
    username: 'admin',
    password: 'admin123',
    realName: '系统管理员',
    avatar: '',
    roles: ['admin'],
    deptId: 1,
    deptName: '技术部',
    phone: '13800138000',
    status: 1,
  },
  {
    id: 2,
    username: 'zhangsan',
    password: '123456',
    realName: '张三',
    avatar: '',
    roles: ['employee'],
    deptId: 2,
    deptName: '市场部',
    phone: '13900139000',
    status: 1,
  },
]

export default [
  {
    url: '/api/auth/captcha',
    method: 'get',
    response: () => {
      const captchaKey = `captcha_${Date.now()}_${Math.random().toString(36).slice(2)}`
      const { text, image } = generateCaptcha()
      captchaStore.set(captchaKey, text.toLowerCase())

      return {
        code: 200,
        message: 'success',
        data: {
          captchaKey,
          captchaImage: image,
        },
      }
    },
  },
  {
    url: '/api/auth/login',
    method: 'post',
    response: ({ body }: { body: { username: string; password: string; captcha: string; captchaKey: string } }) => {
      const { username, password, captcha, captchaKey } = body

      const storedCaptcha = captchaStore.get(captchaKey)
      if (!storedCaptcha || (storedCaptcha !== captcha.toLowerCase() && captcha !== '1234')) {
        captchaStore.delete(captchaKey)
        return {
          code: 401,
          message: '验证码错误',
          data: null,
        }
      }

      captchaStore.delete(captchaKey)

      const user = mockUsers.find((u) => u.username === username)
      if (!user) {
        return {
          code: 402,
          message: '账号不存在',
          data: null,
        }
      }

      if (user.status !== 1) {
        return {
          code: 403,
          message: '账号已禁用',
          data: null,
        }
      }

      if (user.password !== password) {
        return {
          code: 404,
          message: '密码错误',
          data: null,
        }
      }

      const token = `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.${Buffer.from(
        JSON.stringify({ userId: user.id, username: user.username, exp: Date.now() + 86400000 })
      ).toString('base64')}.signature`

      return {
        code: 200,
        message: '登录成功',
        data: {
          token,
          userInfo: {
            id: user.id,
            username: user.username,
            realName: user.realName,
            avatar: user.avatar,
            roles: user.roles,
            deptId: user.deptId,
            deptName: user.deptName,
          },
        },
      }
    },
  },
  {
    url: '/api/auth/forgot-password/step1',
    method: 'post',
    response: ({ body }: { body: { username: string } }) => {
      const { username } = body
      const user = mockUsers.find((u) => u.username === username)

      if (!user) {
        return {
          code: 402,
          message: '账号不存在',
          data: null,
        }
      }

      const maskedPhone = user.phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
      const verifyId = `verify_${Date.now()}`

      return {
        code: 200,
        message: 'success',
        data: {
          maskedPhone,
          verifyId,
        },
      }
    },
  },
  {
    url: '/api/auth/send-sms-code',
    method: 'post',
    response: () => {
      return {
        code: 200,
        message: '验证码已发送',
        data: null,
      }
    },
  },
  {
    url: '/api/auth/reset-password',
    method: 'post',
    response: ({ body }: { body: { verifyId: string; smsCode: string; newPassword: string } }) => {
      const { smsCode } = body

      if (smsCode !== '123456') {
        return {
          code: 406,
          message: '短信验证码错误或已过期',
          data: null,
        }
      }

      if (!body.newPassword || body.newPassword.length < 6) {
        return {
          code: 400,
          message: '密码长度不能少于6位',
          data: null,
        }
      }

      return {
        code: 200,
        message: '密码重置成功',
        data: null,
      }
    },
  },
  {
    url: '/api/auth/user-info',
    method: 'get',
    response: () => {
      return {
        code: 200,
        message: 'success',
        data: {
          id: 1,
          username: 'admin',
          realName: '系统管理员',
          avatar: '',
          roles: ['admin'],
          deptId: 1,
          deptName: '技术部',
        },
      }
    },
  },
] as MockMethod[]
