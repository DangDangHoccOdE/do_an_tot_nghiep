import { get, post, put, del } from '@/utils/http'

const basePath = '/teams'

export const apiTeams = {
  list: (params) => get(basePath, params),
  detail: (id) => get(`${basePath}/${id}`),
  create: (payload) => post(basePath, payload),
  update: (id, payload) => put(`${basePath}/${id}`, payload),
  remove: (id) => del(`${basePath}/${id}`),
  removeBulk: (ids) => del(`${basePath}/bulk`, { ids })
}
