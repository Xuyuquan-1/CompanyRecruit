import request from '../utils/request'

/**
 * 通用文件上传到OSS
 * @param {File} file 上传的文件
 */
export function uploadFile(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/api/osscommon/upload',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/**
 * 获取OSS文件签名下载链接
 * @param {string} objectName OSS对象名称
 */
export function getDownloadUrl(objectName) {
  return request({
    url: '/api/osscommon/downloadUrl',
    method: 'get',
    params: { objectName }
  })
}
