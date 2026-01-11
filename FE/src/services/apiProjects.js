import { get, post, put, del } from '@/utils/http'

export const apiProjects = {
  list: (params) => get("/projects", params),
  detail: (id) => get(`/projects/${id}`),
  create: (payload) => post("/projects", payload),
  createFuture: (payload) => post("/projects/future", payload),
  update: (id, payload) => put(`/projects/${id}`, payload),
  remove: (id) => del(`/projects/${id}`),
  removeBulk: (ids) => del('/projects/bulk', { ids }),
  myProjects: (params) => get("/projects/my-projects", params),
  checkDuplicateName: (projectName, excludeId) => {
    const params = excludeId ? { excludeId } : {}
    return get(`/projects/check-name/${encodeURIComponent(projectName)}`, params)
  },
  getMembers: (projectId) => get(`/projects/${projectId}/members`)
}
