import request from '../utils/request'

export function getResumePage(params) {
  return request({ url: '/api/resume/page', method: 'get', params })
}
export function getResumeById(id) {
  return request({ url: `/api/resume/${id}`, method: 'get' })
}
export function uploadResume(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({ url: '/api/resume/upload', method: 'post', data: formData, headers: { 'Content-Type': 'multipart/form-data' } })
}
export function parseResume(id) {
  return request({ url: `/api/resume/${id}/parse`, method: 'post' })
}
export function updateParsedData(id, data) {
  return request({ url: `/api/resume/${id}/parsed-data`, method: 'put', data })
}
export function deleteResume(id) {
  return request({ url: `/api/resume/${id}`, method: 'delete' })
}

// 下载简历文件
export function downloadResume(id) {
  return request({ 
    url: `/api/resume/${id}/download`, 
    method: 'get',
    responseType: 'blob'
  })
}