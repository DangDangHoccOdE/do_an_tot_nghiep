import { del, get, post, put } from '@/utils/http'

const BASE_URL = '/api/v1/projects/metrics'

export const getMetricsForProject = (projectId, reportDate) => {
  return get(`${BASE_URL}/project/${projectId}/date/${reportDate}`)
}

export const getMetricsForProjectDateRange = (projectId, startDate, endDate) => {
  return get(`${BASE_URL}/project/${projectId}/range`, {
    params: {
      startDate,
      endDate
    }
  })
}

export const calculateAndSaveMetrics = (projectId, reportDate) => {
  return post(`${BASE_URL}/project/${projectId}/calculate/${reportDate}`)
}

export const getTopProjectsByCompletionRate = (reportDate, limit = 5) => {
  return get(`${BASE_URL}/top-projects`, {
    params: {
      reportDate,
      limit
    }
  })
}

export const getMetricsByDateRange = (startDate, endDate) => {
  return get(`${BASE_URL}/range`, {
    params: {
      startDate,
      endDate
    }
  })
}
