export const createTaskRules = (t, model) => {
  const field = (key) => t(`admin.form.${key}`) || t(`admin.table.${key}`)
  const msg = (key, params) => t(`admin.validations.${key}`, params)

  const validateDates = (rule, value, callback) => {
    if (value && rule?.field === 'dueDate' && model?.startDate) {
      const start = new Date(model.startDate)
      const end = new Date(value)
      if (!Number.isNaN(start.getTime()) && !Number.isNaN(end.getTime()) && end < start) {
        callback(new Error(msg('dueAfterStart')))
        return
      }
    }
    callback()
  }

  return {
    projectId: [
      { required: true, message: msg('required', { field: field('projectId') || 'Project' }), trigger: ['blur', 'change'] }
    ],
    title: [
      { required: true, message: msg('required', { field: field('title') }), trigger: ['blur', 'change'] },
      { max: 200, message: msg('max', { field: field('title'), max: 200 }), trigger: ['blur', 'change'] }
    ],
    description: [
      { max: 1000, message: msg('max', { field: field('description'), max: 1000 }), trigger: ['blur', 'change'] }
    ],
    startDate: [
      { validator: validateDates, trigger: ['change', 'blur'] }
    ],
    dueDate: [
      { validator: validateDates, trigger: ['change', 'blur'] }
    ]
  }
}
