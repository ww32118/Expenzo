// src/services/userService.js
import api from '@/services/api'

export default {
  register(payload) {
    return api.post('/users/add-user', payload).then((r) => r.data)
  },

  login(payload) {
    return api.post('/users/login', payload).then((r) => r.data)
  },

  getById(id) {
    return api.get(`/users/${id}`).then((r) => r.data)
  },

  getAll() {
    return api.get('/users/all-users').then((r) => r.data)
  },

  update(payload) {
    return api.put('/users/update', payload).then((r) => r.data)
  },

  changePassword(payload) {
    return api.put('/users/change-password', payload).then((r) => r.data)
  },

  changeProfileImage(id, image) {
    return api
      .put(`/users/change-profile-image/${id}`, null, { params: { image } })
      .then((r) => r.data)
  },

  remove(id) {
    return api.delete(`/users/${id}`).then((r) => r.data)
  },

  me() {
    return api.get('/users/me').then((r) => r.data)
  },

  forgotPassword(email) {
    return api.post('/auth/forgot-password', { email }).then((r) => r.data)
  },

  verifyResetToken(token) {
    return api.get('/auth/verify-reset-token', { params: { token } }).then((r) => r.data)
  },

  resetPassword(token, newPassword) {
    return api.post('/auth/reset-password', { token, newPassword }).then((r) => r.data)
  },
}