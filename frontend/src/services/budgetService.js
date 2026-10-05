import api from '@/services/api'

export default {
  add(payload) {
    // payload: { monthlyLimit, month, year, userId, categoryId }
    return api.post('/budget/add-budget', payload).then((r) => r.data)
  },
update(id, payload) {
  return api.put(`/budget/update-budget/${id}`, payload).then((r) => r.data)
},
  getById(id) {
    return api.get(`/budget/${id}`).then((r) => r.data)
  },
  getAll() {
    return api.get('/budget/all-budgets').then((r) => r.data)
  },
  remove(id) {
    return api.delete(`/budget/${id}`).then((r) => r.data)
  },
}
