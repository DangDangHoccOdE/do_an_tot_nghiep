import { del, get, post, put } from '@/utils/http'

export const apiTasks = {
  byProject: (projectId) => get(`/projects/${projectId}/tasks`),
  detail: (id) => get(`/tasks/${id}`),
  create: (projectId, payload) => post(`/projects/${projectId}/tasks`, payload),
  update: (id, payload) => put(`/tasks/${id}`, payload),
  remove: (id) => del(`/tasks/${id}`)
}
