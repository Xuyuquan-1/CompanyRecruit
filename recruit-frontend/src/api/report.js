import request from '../utils/request'

export function getProgressStats() {
  return request({ url: '/api/report/progress', method: 'get' })
}
export function getEffectAnalysis(params) {
  return request({ url: '/api/report/effect', method: 'get', params })
}
export function getStatsByJob() {
  return request({ url: '/api/report/by-job', method: 'get' })
}
export function getStatsByChannel() {
  return request({ url: '/api/report/by-channel', method: 'get' })
}
export function exportReportExcel() {
  return request({ url: '/api/report/export', method: 'get', responseType: 'blob' })
}