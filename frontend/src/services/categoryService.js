import axios from 'axios'

const API_URL = 'http://localhost:8080/category'

const api = axios.create({ baseURL: API_URL })

api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token && token !== 'null' && token !== 'undefined' && token.length > 20) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      localStorage.clear()
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default {
  // ✅ Both names work
  async getAll() {
    const response = await api.get('/all-categories')
    return Array.isArray(response.data) ? response.data : []
  },
  
  async getAllCategories() {
    return this.getAll()
  },

  async add(category) {
    const response = await api.post('/add-category', category)
    return response.data
  },

  async addCategory(category) {
    return this.add(category)
  },

  async delete(id) {
    await api.delete(`/${id}`)
  },

  async deleteCategory(id) {
    return this.delete(id)
  },

  async update(id, category) {
    const response = await api.put(`/${id}`, category)
    return response.data
  },

  async updateCategory(id, category) {
    return this.update(id, category)
  },
}