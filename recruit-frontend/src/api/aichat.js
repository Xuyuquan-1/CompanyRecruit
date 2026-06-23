import request from '../utils/request'

export function chat(question) {
  return request({ url: '/api/ai/chat', method: 'post', data: { question } })
}
export function getChatHistory() {
  return request({ url: '/api/ai/history', method: 'get' })
}
export function clearChatHistory() {
  return request({ url: '/api/ai/history', method: 'delete' })
}