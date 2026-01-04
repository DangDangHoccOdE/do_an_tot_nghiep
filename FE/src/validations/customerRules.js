export const customerRules = {
    email: [
        { required: true, message: 'Email không được để trống', trigger: 'blur' },
        {
            pattern: /^((?!\.)[\w-_.]*[^.])(@\w+)(\.\w+(\.\w+)?[^.\W])$/,
            message: 'Định dạng email không hợp lệ',
            trigger: 'blur'
        }
    ],
    password: [
        {
            required: true,
            message: 'Mật khẩu không được để trống',
            trigger: 'blur',
            validator: (rule, value, callback) => {
                if (!value) {
                    callback(new Error('Mật khẩu không được để trống'))
                } else {
                    callback()
                }
            }
        }
    ],
    firstName: [
        { required: true, message: 'Tên không được để trống', trigger: 'blur' },
        { max: 50, message: 'Tên tối đa 50 ký tự', trigger: 'blur' }
    ],
    lastName: [
        { required: true, message: 'Họ không được để trống', trigger: 'blur' },
        { max: 50, message: 'Họ tối đa 50 ký tự', trigger: 'blur' }
    ],
    phone: [
        {
            pattern: /^0\d{9}$/,
            message: 'Số điện thoại phải bắt đầu bằng 0 và có 10 chữ số',
            trigger: 'blur'
        }
    ]
}
