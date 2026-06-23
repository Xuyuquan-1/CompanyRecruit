import request from '../utils/request'

export function getEmployeeList() {
  return request({ url: '/api/employee/list', method: 'get' })
}
export function getEmployeeById(id) {
  return request({ url: `/api/employee/${id}`, method: 'get' })
}
export function onboardEmployee(offerId, data) {
  return request({ url: `/api/employee/onboard/${offerId}`, method: 'post', data })
}
export function updateEmployee(id, data) {
  return request({ url: `/api/employee/${id}`, method: 'put', data })
}
export function updateEmployeeDocument(id, documentType, status) {
  return request({ url: `/api/employee/${id}/document`, method: 'post', data: { documentType, status } })
}
export function deleteEmployee(id) {
  return request({ url: `/api/employee/${id}`, method: 'delete' })
}
export function submitContract(id, contractUrl) {
  return request({ url: `/api/employee/${id}/submit-contract`, method: 'post', data: { contractUrl } })
}