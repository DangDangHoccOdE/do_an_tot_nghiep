import { apiProjects } from '@/services/apiProjects'

export const createProjectRules = (t, model, projectId = null) => {
  const field = (key) => t(`admin.form.${key}`)
  const msg = (key, params) => t(`admin.validations.${key}`, params)

  const validateDates = (rule, value, callback) => {
    if (value && rule?.field === 'endDate' && model?.startDate) {
      const start = new Date(model.startDate)
      const end = new Date(value)
      if (!Number.isNaN(start.getTime()) && !Number.isNaN(end.getTime()) && end < start) {
        callback(new Error(msg('endAfterStart')))
        return
      }
    }
    callback()
  }

  const validateProjectName = async (rule, value, callback) => {
    if (!value || value.trim().length === 0) {
      callback()
      return
    }

    try {
      const response = await apiProjects.checkDuplicateName(value.trim(), projectId)
      if (response?.exists) {
        callback(new Error(msg('projectNameExists')))
      } else {
        callback()
      }
    } catch (error) {
      // Nếu API lỗi, vẫn cho phép tiếp tục (không block user)
      console.error('Error checking duplicate project name:', error)
      callback()
    }
  }

  return {
    projectName: [
      { required: true, message: msg('required', { field: field('projectName') }), trigger: ['blur', 'change'] },
      { max: 200, message: msg('max', { field: field('projectName'), max: 200 }), trigger: ['blur', 'change'] },
      { validator: validateProjectName, trigger: 'blur' }
    ],
    clientId: [
      { required: true, message: msg('required', { field: field('clientId') }), trigger: ['blur', 'change'] }
    ],
    teamId: [
      { max: 200, message: msg('max', { field: field('teamId'), max: 200 }), trigger: ['blur', 'change'] }
    ],
    description: [
      { max: 1000, message: msg('max', { field: field('description'), max: 1000 }), trigger: ['blur', 'change'] }
    ],
    budgetEstimated: [
      { type: 'number', min: 0, message: msg('nonNegative', { field: field('budgetEstimated') }), trigger: ['blur', 'change'] }
    ],
    timelineEstimated: [
      { type: 'number', min: 1, message: msg('minNumber', { field: field('timelineEstimated'), min: 1 }), trigger: ['blur', 'change'] }
    ],
    startDate: [
      { validator: validateDates, trigger: ['change', 'blur'] }
    ],
    endDate: [
      { validator: validateDates, trigger: ['change', 'blur'] }
    ]
  }
}
