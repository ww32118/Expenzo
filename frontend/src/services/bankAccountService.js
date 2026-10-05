import api from '@/services/api'

export const ACCOUNT_TYPES = ['SAVINGS', 'CURRENT', 'FIXED_DEPOSIT', 'RECURRING_DEPOSIT', 'SALARY', 'NRI']

export default {
  add(payload) {
    // payload: { bankName, bankAccountNumber, accountHolderName, ifscCode, accountType, userId }
    return api.post('/bank-account/add-bank-account', payload).then((r) => r.data)
  },
update(payload) {
  return api.put(`/bank-account/update-bank-account/${payload.id}`, payload).then((r) => r.data)
},
  getById(id) {
    return api.get(`/bank-account/${id}`).then((r) => r.data)
  },
  getAll() {
    return api.get('/bank-account/all-bank-accounts').then((r) => r.data)
  },
  changeAccountType(id, newAccountType) {
    return api.put(`/bank-account/change-account-type/${id}`, newAccountType).then((r) => r.data)
  },
  updateOwnerName(id, newOwnerName) {
    return api
      .put(`/bank-account/update-owner-name/${id}`, null, { params: { newOwnerName } })
      .then((r) => r.data)
  },
  remove(id) {
    return api.delete(`/bank-account/${id}`).then((r) => r.data)
  },
}
