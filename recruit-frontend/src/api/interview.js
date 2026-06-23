import request from '../utils/request'

export function getInterviewPage(params) {
  return request({ url: '/api/interview/page', method: 'get', params })
}
export function getInterviewById(id) {
  return request({ url: `/api/interview/${id}`, method: 'get' })
}
export function arrangeInterview(data) {
  return request({ url: '/api/interview', method: 'post', data })
}
export function cancelInterview(id) {
  return request({ url: `/api/interview/${id}/cancel`, method: 'post' })
}
export function evaluateInterview(id, evaluation, result) {
  return request({ url: `/api/interview/${id}/evaluate`, method: 'post', data: { evaluation, result } })
}
// 应聘者专用API
export function getMyInterviews(params) {
  return request({ url: '/api/interview/page', method: 'get', params })
}