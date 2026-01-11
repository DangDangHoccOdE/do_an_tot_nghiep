import { get, post, put, del } from '@/utils/http'

export const apiDailyTasks = {
  create: (payload) => post("/daily-tasks", payload),
  createDailyTask: (payload) => post("/daily-tasks", payload),

  update: (taskId, payload) =>
    put(`/daily-tasks/${taskId}`, payload),
  updateDailyTask: (taskId, payload) =>
    put(`/daily-tasks/${taskId}`, payload),

  detail: (taskId) =>
    get(`/daily-tasks/${taskId}`),

  byProject: (projectId) =>
    get(`/daily-tasks/project/${projectId}`),

  byProjectAndDate: (projectId, taskDate) =>
    get(`/daily-tasks/project/${projectId}/date/${taskDate}`),

  byAssignedUser: (userId) =>
    get(`/daily-tasks/user/${userId}`),

  byAssignedUserAndDate: (userId, taskDate) =>
    get(`/daily-tasks/user/${userId}/date/${taskDate}`),

  byDateRange: (projectId, startDate, endDate) =>
    get(`/daily-tasks/project/${projectId}/range`, {
      params: {
        startDate,
        endDate
      }
    }),

  byStatus: (projectId, status) =>
    get(`/daily-tasks/project/${projectId}/status/${status}`),

  remove: (taskId) =>
    del(`/daily-tasks/${taskId}`),

  countByDate: (projectId, taskDate) =>
    get(`/daily-tasks/project/${projectId}/date/${taskDate}/count`)
}
