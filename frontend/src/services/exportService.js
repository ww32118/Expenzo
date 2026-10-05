import api from '@/services/api'

function download(blobData, filename, mime) {
  const blob = new Blob([blobData], { type: mime })
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  link.remove()
  window.URL.revokeObjectURL(url)
}

export default {
  async downloadCsv(userId) {
    const res = await api.get(`/export/transactions/csv/${userId}`, { responseType: 'blob' })
    download(res.data, 'transactions.csv', 'text/csv')
  },
  async downloadPdf(userId) {
    const res = await api.get(`/export/transactions/pdf/${userId}`, { responseType: 'blob' })
    download(res.data, 'transactions.pdf', 'application/pdf')
  },
}
