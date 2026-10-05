import api from '@/services/api'

export default {
  add(payload) {
    // payload: { amount, currency, description, date, userId, categoryId }
    return api.post('/transactions/add-transaction', payload).then((r) => r.data)
  },
  update(id, payload) {
    return api.put(`/transactions/update-transaction/${id}`, payload).then((r) => r.data)
  },
  getById(id) {
    return api.get(`/transactions/${id}`).then((r) => r.data)
  },
  getAll() {
    return api.get('/transactions/all-transactions').then((r) => r.data)
  },
  remove(id) {
    return api.delete(`/transactions/${id}`).then((r) => r.data)
  },
   // ✅ NEW — Import CSV / Excel / PDF
  importFile(file) {
    const form = new FormData()
    form.append('file', file)
    return api
      .post('/transactions/import', form, {
        headers: { 'Content-Type': 'multipart/form-data' },
      })
      .then((r) => r.data)
  },
}
