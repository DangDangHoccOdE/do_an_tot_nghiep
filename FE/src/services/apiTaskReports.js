import { del, get, post, put } from '@/utils/http'

const BASE_URL = '/task-reports'

export const createTaskReport = (data) => {
  return post(BASE_URL, data)
}

export const updateTaskReport = (reportId, data) => {
  return put(`${BASE_URL}/${reportId}`, data)
}

export const getTaskReport = (reportId) => {
  return get(`${BASE_URL}/${reportId}`)
}

export const getReportsByDailyTask = (dailyTaskId) => {
  return get(`${BASE_URL}/task/${dailyTaskId}`)
}

export const getReportsByReporter = (reporterId) => {
  return get(`${BASE_URL}/reporter/${reporterId}`)
}

export const getReportsByStatus = (status) => {
  return get(`${BASE_URL}/status/${status}`)
}

export const getReportsByDateRange = (reporterId, startDate, endDate) => {
  return get(`${BASE_URL}/reporter/${reporterId}/range`, {
    params: {
      startDate,
      endDate
    }
  })
}

export const deleteTaskReport = (reportId) => {
  return delete(`${BASE_URL}/${reportId}`)
}

export const getReportCount = (dailyTaskId) => {
  return get(`${BASE_URL}/task/${dailyTaskId}/count`)
}
