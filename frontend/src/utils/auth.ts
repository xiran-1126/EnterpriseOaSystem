import CryptoJS from 'crypto-js'

const SECRET_KEY = 'oa-system-secret-key-2024'

const getKey = () => {
  const keyBytes = new Uint8Array(16)
  const temp = new TextEncoder().encode(SECRET_KEY)
  const len = Math.min(temp.length, 16)
  keyBytes.set(temp.slice(0, len))
  return CryptoJS.lib.WordArray.create(keyBytes as any)
}

export const encryptPassword = (password: string): string => {
  const key = getKey()
  const encrypted = CryptoJS.AES.encrypt(password, key, {
    mode: CryptoJS.mode.ECB,
    padding: CryptoJS.pad.Pkcs7
  })
  return encrypted.toString()
}

export const decryptPassword = (ciphertext: string): string => {
  const key = getKey()
  const bytes = CryptoJS.AES.decrypt(ciphertext, key, {
    mode: CryptoJS.mode.ECB,
    padding: CryptoJS.pad.Pkcs7
  })
  return bytes.toString(CryptoJS.enc.Utf8)
}
