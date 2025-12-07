import { createCommonValidator } from "@/utils/validatorRules";

export const createRegisterRules = (
    i18n,
    apiRegister
  ) => ({
    email: createCommonValidator({
      field: i18n('email'),
      isEmail: true,
      maxLength: 50,
      apiCheckFn: async (value) => {
        return await apiRegister.checkEmailExists(value);
      },
    }),
  
    firstName: createCommonValidator({
      field: i18n('firstName'),
      maxLength: 50,
      required: true,
    }),
  
    lastName: createCommonValidator({
      field: i18n('lastName'),
      maxLength: 50,
      required: true,
    }),

    password: createCommonValidator({
      field: i18n('password'),
      maxLength: 20,
      required: true,
      isPassword: true
    }),

    confirmPassword: createCommonValidator({
      field: i18n('confirmPassword'),
      maxLength: 20,
      required: true,
      isPassword: true
    }),
  });
  