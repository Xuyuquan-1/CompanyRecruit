import request from '../utils/request'

export function getApplicationPage(params) {
  return request({ url: '/api/application/page', method: 'get', params })
}
export function getApplicationById(id) {
  return request({ url: `/api/application/${id}`, method: 'get' })
}
export function passApplication(id) {
  return request({ url: `/api/application/${id}/pass`, method: 'post' })
}
export function rejectApplication(id, refuseType) {
  return request({ url: `/api/application/${id}/reject`, method: 'post', data: { refuseType } })
}
export function updateApplicationRemark(id, tags, remark) {
  return request({ url: `/api/application/${id}/remark`, method: 'put', data: { tags, remark } })
}
// 应聘者专用API
export function submitApplication(data) {
  return request({ url: '/api/application/submit', method: 'post', data })
}
export function getMyApplications(params) {
  return request({ url: '/api/application/page', method: 'get', params })
}
export function getMyResumes() {
  return request({ url: '/api/resume/my', method: 'get' })
}