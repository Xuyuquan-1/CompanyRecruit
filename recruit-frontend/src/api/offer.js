import request from '../utils/request'

export function getOfferPage(params) {
  return request({ url: '/api/offer/page', method: 'get', params })
}
export function getOfferById(id) {
  return request({ url: `/api/offer/${id}`, method: 'get' })
}
export function sendOffer(data) {
  return request({ url: '/api/offer', method: 'post', data })
}
export function confirmOnboard(id, joinDate) {
  return request({ url: `/api/offer/${id}/onboard`, method: 'post', data: { joinDate } })
}
export function rejectOffer(id, reason) {
  return request({ url: `/api/offer/${id}/reject`, method: 'post', data: { reason } })
}
export function acceptOffer(id) {
  return request({ url: `/api/offer/${id}/accept`, method: 'post' })
}
export function submitDocuments(id, data) {
  return request({ url: `/api/offer/${id}/submit-documents`, method: 'post', data })
}
// 应聘者专用API
export function getMyOffers(params) {
  return request({ url: '/api/offer/page', method: 'get', params })
}