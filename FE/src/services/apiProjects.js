import { get, post, put, del } from '@/utils/http'

const basePath = '/projects'

export const apiProjects = {
  list: (params) => get(basePath, params),
  detail: (id) => get(`${basePath}/${id}`),
  create: (payload) => post(basePath, payload),
  createFuture: (payload) => post(`${basePath}/future`, payload),
  update: (id, payload) => put(`${basePath}/${id}`, payload),
  remove: (id) => del(`${basePath}/${id}`),
  myProjects: (params) => get(`${basePath}/my-projects`, params)
}
