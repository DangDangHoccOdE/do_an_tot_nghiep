export const createTeamRules = (t) => {
  const field = (key) => t(`admin.form.${key}`)
  const msg = (key, params) => t(`admin.validations.${key}`, params)

  return {
    projectId: [
      { required: true, message: msg('required', { field: field('projectName') }), trigger: ['blur', 'change'] }
    ],
    name: [
      { required: true, message: msg('required', { field: field('teamName') }), trigger: ['blur', 'change'] },
      { max: 150, message: msg('max', { field: field('teamName'), max: 150 }), trigger: ['blur', 'change'] }
    ],
    description: [
      { max: 1000, message: msg('max', { field: field('teamDescription'), max: 1000 }), trigger: ['blur', 'change'] }
    ]
  }
}
