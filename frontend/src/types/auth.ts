export interface LoginRequest {
  username: string
  password: string
  captcha: string
  captchaKey: string
}

export interface UserInfo {
  id: number
  username: string
  realName: string
  avatar: string
  roles: string[]
  deptId: number
  deptName: string
}

export interface LoginResponse {
  token: string
  userInfo: UserInfo
  isNewIp: boolean
}

export interface CaptchaResponse {
  captchaKey: string
  captchaImage: string
}

export interface ForgotPasswordStep1Request {
  username: string
}

export interface ForgotPasswordStep1Response {
  maskedPhone: string
  verifyId: string
}

export interface SendSmsCodeRequest {
  verifyId: string
}

export interface ResetPasswordRequest {
  verifyId: string
  smsCode: string
  newPassword: string
}

export interface ApiResponse<T = unknown> {
  code: number
  message: string
  data: T
}
