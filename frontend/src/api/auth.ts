import { post, get } from './request'
import {
  LoginRequest,
  LoginResponse,
  CaptchaResponse,
  ForgotPasswordStep1Request,
  ForgotPasswordStep1Response,
  SendSmsCodeRequest,
  ResetPasswordRequest,
} from '@/types/auth'

export const login = (data: LoginRequest): Promise<LoginResponse> => {
  return post<LoginResponse>('/auth/login', data)
}

export const getCaptcha = (): Promise<CaptchaResponse> => {
  return get<CaptchaResponse>('/auth/captcha')
}

export const forgotPasswordStep1 = (data: ForgotPasswordStep1Request): Promise<ForgotPasswordStep1Response> => {
  return post<ForgotPasswordStep1Response>('/auth/forgot-password/step1', data)
}

export const sendSmsCode = (data: SendSmsCodeRequest): Promise<void> => {
  return post<void>('/auth/send-sms-code', data)
}

export const resetPassword = (data: ResetPasswordRequest): Promise<void> => {
  return post<void>('/auth/reset-password', data)
}

export const logout = (): Promise<void> => {
  return post<void>('/auth/logout')
}

export const getUserInfoApi = (): Promise<LoginResponse['userInfo']> => {
  return get<LoginResponse['userInfo']>('/auth/user-info')
}
