import request from '../utils/request'

export function getNotificationList() {
  return request({ url: '/api/notification/list', method: 'get' })
}
export function markAsRead(id) {
  return request({ url: `/api/notification/${id}/read`, method: 'post' })
}
export function markAllAsRead() {
  return request({ url: '/api/notification/read-all', method: 'post' })
}
export function getUnreadCount() {
  return request({ url: '/api/notification/unread-count', method: 'get' })
}