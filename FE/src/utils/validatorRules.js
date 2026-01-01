import { validateMessages } from "./validateMessages";

const EMAIL_REGEX = /^[\w-.]+@([\w-]+\.)+[\w-]{2,4}$/;

const PHONE_REGEX = /^(0|\+84)[1-9][0-9]{8,9}$/;

const PASSWORD_REGEX = /^(?=.*[!@#$%^&*(),.?":{}|<>_\-]).{8,}$/;

export function createCommonValidator ({
    field,
    required,
    minLength,
    maxLength,
    isEmail,
    isPhone,
    isPassword,
    pattern,
    apiCheckFn
}) {
    const rules = [];

    if (required) {
        rules.push({
            required: true,
            message: validateMessages.required(field),
            trigger: ["blur", "change"]
        });
    }
    if (minLength) {
        rules.push({
            min: minLength,
            message: validateMessages.min(field, minLength),
            trigger: ["blur", "change"]
        });
    }
    if (maxLength) {
        rules.push({
            max: maxLength,
            message: validateMessages.max(field, maxLength),
            trigger: ["blur", "change"]
        });
    }
    if (isEmail) {
        rules.push({
            pattern: EMAIL_REGEX,
            message: validateMessages.email(field),
            trigger: ["blur", "change"]
        });
    }
    if (isPhone) {
        rules.push({
            pattern: PHONE_REGEX,
            message: validateMessages.phone(field),
            trigger: ["blur", "change"]
        });
    }
    if (isPassword) {
        rules.push({
            pattern: PASSWORD_REGEX,
            message: validateMessages.password(field),
            trigger: ["blur", "change"]
        });
    }
    if (pattern) {
        rules.push({
            pattern,
            message: validateMessages.pattern(field),
            trigger: ["blur", "change"]
        });
    }
    if (apiCheckFn) {
        rules.push({
            async validator(rule, value, callback) {
                if (!value) {
                    return callback();
                }
                try {
                    const exists = await apiCheckFn(value);
                    if (exists) {
                        callback(new Error(validateMessages.duplicate(field)));
                    } else {
                        callback();
                    }
                } catch (error) {
                    console.error('API check error:', error);
                    console.error('Error details:', {
                        message: error?.message,
                        response: error?.response,
                        status: error?.response?.status,
                        data: error?.response?.data
                    });
                    callback(new Error(validateMessages.apiError()));
                }
            },
            trigger: ["blur", "change"],
        })
    }

    return rules;
}