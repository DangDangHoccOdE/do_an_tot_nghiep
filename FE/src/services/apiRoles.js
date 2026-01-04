import { get } from '@/utils/http'

export const apiRoles = {
  list: () => get('/roles/get-all'), 
}
