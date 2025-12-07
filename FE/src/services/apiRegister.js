import { get, post } from '@/utils/http';

export const apiRegister = {
  register: (userData) => post('/users/register', userData),
  checkEmailExists: (email) => get('/users/check-duplicate', { email })
};
