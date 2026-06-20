export const validateUsername = (_: unknown, value: string) => {
  if (!value) {
    return Promise.reject(new Error('请输入账号'))
  }
  return Promise.resolve()
}

export const validatePassword = (_: unknown, value: string) => {
  if (!value) {
    return Promise.reject(new Error('请输入密码'))
  }
  if (value.length < 6) {
    return Promise.reject(new Error('密码长度不能少于6位'))
  }
  return Promise.resolve()
}

export const validateCaptcha = (_: unknown, value: string) => {
  if (!value) {
    return Promise.reject(new Error('请输入验证码'))
  }
  if (value.length !== 4) {
    return Promise.reject(new Error('验证码为4位'))
  }
  return Promise.resolve()
}

export const validateConfirmPassword = (password: string) => {
  return (_: unknown, value: string) => {
    if (!value) {
      return Promise.reject(new Error('请确认密码'))
    }
    if (value !== password) {
      return Promise.reject(new Error('两次输入的密码不一致'))
    }
    return Promise.resolve()
  }
}

export const validateSmsCode = (_: unknown, value: string) => {
  if (!value) {
    return Promise.reject(new Error('请输入短信验证码'))
  }
  if (value.length !== 6) {
    return Promise.reject(new Error('短信验证码为6位'))
  }
  return Promise.resolve()
}
