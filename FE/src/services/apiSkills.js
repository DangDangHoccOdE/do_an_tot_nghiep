import { del, get, post, put } from '@/utils/http';

export const apiSkills = {
  // Skill management
  list: () => get('/skills'),

  getById: (id) => get(`/skills/${id}`),

  create: (data) => post('/skills', data),

  update: (id, data) => put(`/skills/${id}`, data),
  delete: (id) => del(`/skills/${id}`),

  // Employee skills
  addSkillToStaff: (staffId, skillData) => post(`/staff/${staffId}/skills`, skillData),

  removeSkillFromStaff: (staffId, skillId) => del(`/staff/${staffId}/skills/${skillId}`),
  getStaffSkills: (staffId) => get(`/staff/${staffId}/skills`),

  updateStaffSkill: (staffId, skillId, data) => put(`/staff/${staffId}/skills/${skillId}`, data),
};
