import { post } from '@/utils/http'

export const apiAuth = {
  login: (credentials) => post('/auth/login', credentials),
  refresh: (refreshToken) => post('/auth/refresh', { refreshToken }),
  activateAccount: (payload) => post('/auth/activate', payload),
  resendActivation: (payload) => post('/auth/resend-activation', payload)
}
