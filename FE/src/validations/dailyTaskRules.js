export const createDailyTaskRules = (t) => {
  return {
    title: [
      { required: true, message: t('admin.daily.titleRequired'), trigger: ['blur', 'change'] },
      { min: 5, max: 255, message: t('admin.daily.titleLength'), trigger: ['blur', 'change'] }
    ],
    projectId: [
      { required: true, message: t('admin.daily.projectRequired'), trigger: ['blur', 'change'] }
    ],
    assignedTo: [
      { required: true, message: t('admin.daily.assignedToRequired'), trigger: ['blur', 'change'] }
    ],
    taskDate: [
      { required: true, message: t('admin.daily.dateRequired'), trigger: ['change', 'blur'] }
    ],
    priority: [
      { required: true, message: t('admin.daily.priorityRequired'), trigger: ['change', 'blur'] }
    ],
    estimatedHours: [
      { required: true, message: t('admin.daily.estimatedHoursRequired'), trigger: ['blur', 'change'] }
    ],
    description: [
      { max: 2000, message: t('admin.daily.descriptionMax'), trigger: ['blur', 'change'] }
    ]
  }
}
