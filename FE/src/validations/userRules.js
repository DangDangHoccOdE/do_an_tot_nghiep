export const createUserRules = (t, model) => {
  const field = (key) => t(`admin.form.${key}`)
  const msg = (key, params) => t(`admin.validations.${key}`, params)

  return {
    email: [
      { required: true, message: msg('required', { field: field('email') }), trigger: ['blur', 'change'] },
      { 
        pattern: /^[^\s@]+@[^\s@]+\.[^\s@]+$/, 
        message: msg('invalidEmail', { field: field('email') }), 
        trigger: ['blur', 'change'] 
      }
    ],
    firstName: [
      { max: 100, message: msg('max', { field: field('firstName'), max: 100 }), trigger: ['blur', 'change'] }
    ],
    lastName: [
      { max: 100, message: msg('max', { field: field('lastName'), max: 100 }), trigger: ['blur', 'change'] }
    ],
    phone: [
      { max: 20, message: msg('max', { field: field('phone'), max: 20 }), trigger: ['blur', 'change'] }
    ],
    roleId: [
      { required: true, message: msg('required', { field: field('role') }), trigger: ['blur', 'change'] }
    ],
    password: [
      { required: true, message: msg('required', { field: field('password') }), trigger: ['blur', 'change'] },
      { min: 6, message: msg('min', { field: field('password'), min: 6 }), trigger: ['blur', 'change'] }
    ]
  }
}
