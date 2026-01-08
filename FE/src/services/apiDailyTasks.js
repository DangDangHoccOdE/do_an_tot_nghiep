import { del, get, post, put } from '@/utils/http'

const BASE_URL = '/api/v1/daily-tasks'

export const createDailyTask = (data) => {
  return post(BASE_URL, data)
}

export const updateDailyTask = (taskId, data) => {
  return put(`${BASE_URL}/${taskId}`, data)
}

export const getDailyTask = (taskId) => {
  return get(`${BASE_URL}/${taskId}`)
}

export const getTasksByProject = (projectId) => {
  return get(`${BASE_URL}/project/${projectId}`)
}

export const getTasksByProjectAndDate = (projectId, taskDate) => {
  return get(`${BASE_URL}/project/${projectId}/date/${taskDate}`)
}

export const getTasksByAssignedUser = (userId) => {
  return get(`${BASE_URL}/user/${userId}`)
}

export const getTasksByAssignedUserAndDate = (userId, taskDate) => {
  return get(`${BASE_URL}/user/${userId}/date/${taskDate}`)
}

export const getTasksByDateRange = (projectId, startDate, endDate) => {
  return get(`${BASE_URL}/project/${projectId}/range`, {
    params: {
      startDate,
      endDate
    }
  })
}

export const getTasksByStatus = (projectId, status) => {
  return get(`${BASE_URL}/project/${projectId}/status/${status}`)
}

export const deleteDailyTask = (taskId) => {
  return delete(`${BASE_URL}/${taskId}`)
}

export const getTaskCount = (projectId, taskDate) => {
  return get(`${BASE_URL}/project/${projectId}/date/${taskDate}/count`)
}
