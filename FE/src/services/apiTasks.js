import { get } from '@/utils/http'

export const apiTasks = {
  byProject: (projectId) => get(`/projects/${projectId}/tasks`)
}
