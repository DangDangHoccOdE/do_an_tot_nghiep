import { post } from '@/utils/http'

export const apiAuth = {
  login: (credentials) => post('/auth/login', credentials)
}
