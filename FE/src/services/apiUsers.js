import { get, del, post, put } from '@/utils/http'

const basePath = '/users'

export const apiUsers = {
  // Get all users (for backward compatibility)
  list: (params) => get('/users', params),
  
  // Get customers (ROLE_USER)
  listCustomers: (params) => get('/users/customers', params),
  
  // Get staff (ROLE_STAFF)
  listStaff: (params) => get('/users/staff', params),
  
  detail: (id) => get(`${basePath}/${id}`),
  
  // Create user with FormData (multipart/form-data)
  create: (formData) => post(`/users`, formData),
  
  // Create customer (auto ROLE_USER)
  createCustomer: (formData) => post(`/users/customers`, formData),
  
  // Create staff (with skill and CV)
  createStaff: (formData) => post(`/users/staff`, formData),
  
  // Update user with FormData (multipart/form-data)
  update: (id, formData) => put(`/users/${id}`, formData),
  
  // Update customer
  updateCustomer: (id, formData) => put(`/users/customers/${id}`, formData),
  
  // Update staff
  updateStaff: (id, formData) => put(`/users/staff/${id}`, formData),
  
  // Soft delete - only marks as deleted
  remove: (id) => del(`${basePath}/${id}`),
  
  removeCustomer: (id) => del(`${basePath}/customers/${id}`),
  
  removeStaff: (id) => del(`${basePath}/staff/${id}`),
}
