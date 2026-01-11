import { get, post, put, del } from '@/utils/http'

const BASE_URL = '/projects/metrics'

export const apiProjectMetrics = {
  getByProjectAndDate: (projectId, reportDate) =>
    get(`${BASE_URL}/project/${projectId}/date/${reportDate}`),

  getByProjectDateRange: (projectId, startDate, endDate) =>
    get(`${BASE_URL}/project/${projectId}/range`, {
      params: {
        startDate,
        endDate
      }
    }),

  calculateAndSave: (projectId, reportDate) =>
    post(`${BASE_URL}/project/${projectId}/calculate/${reportDate}`),

  getTopByCompletionRate: (reportDate, limit = 5) =>
    get(`${BASE_URL}/top-projects`, {
      params: {
        reportDate,
        limit
      }
    }),

  getByDateRange: (startDate, endDate) =>
    get(`${BASE_URL}/range`, {
      params: {
        startDate,
        endDate
      }
    }),

  // Aliases for backward compatibility
  getMetricsForProject: (projectId, reportDate) =>
    get(`${BASE_URL}/project/${projectId}/date/${reportDate}`),

  getMetricsForProjectDateRange: (projectId, startDate, endDate) =>
    get(`${BASE_URL}/project/${projectId}/range`, {
      params: {
        startDate,
        endDate
      }
    }),

  calculateAndSaveMetrics: (projectId, reportDate) =>
    post(`${BASE_URL}/project/${projectId}/calculate/${reportDate}`)
}
