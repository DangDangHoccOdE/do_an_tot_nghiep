import { get, post } from '@/utils/http'

const basePath = '/projects/revenue'

export const apiRevenue = {
  // Get revenue statistics
  getStats: (params) => get(`${basePath}/stats`, params),
  
  // Get monthly revenue chart data
  getMonthlyRevenue: (year, params) => get(`${basePath}/monthly/${year}`, params),
  
  // Get top projects by revenue
  getTopProjectsByRevenue: (limit = 5, params) => get(`${basePath}/top/revenue?limit=${limit}`, params),
  
  // Get projects completed earliest
  getTopProjectsByCompletionDate: (limit = 5, params) => get(`${basePath}/top/completed?limit=${limit}`, params),
  
  // Export revenue data
  exportRevenue: (year) => post(`${basePath}/export`, { year }, { responseType: 'blob' })
}
