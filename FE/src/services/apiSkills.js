import { del, get, post, put } from '@/utils/http';

const API_URL = '/api/v1/skills';

export const apiSkills = {
  // Skill management
  list: () => get(API_URL),

  getById: (id) => get(`${API_URL}/${id}`),

  create: (data) => post(API_URL, data),

  update: (id, data) => put(`${API_URL}/${id}`, data),
  delete: (id) => del(`${API_URL}/${id}`),

  // Employee skills
  addSkillToStaff: (staffId, skillData) => post(`/api/v1/staff/${staffId}/skills`, skillData),

  removeSkillFromStaff: (staffId, skillId) => del(`/api/v1/staff/${staffId}/skills/${skillId}`),
  getStaffSkills: (staffId) => get(`/api/v1/staff/${staffId}/skills`),

  updateStaffSkill: (staffId, skillId, data) => put(`/api/v1/staff/${staffId}/skills/${skillId}`, data),
};
