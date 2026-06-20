declare module 'jsencrypt' {
  export default class JSEncrypt {
    constructor(options?: any)
    setPublicKey(key: string): void
    setPrivateKey(key: string): void
    encrypt(plaintext: string): string | false
    decrypt(ciphertext: string): string | false
  }
}
