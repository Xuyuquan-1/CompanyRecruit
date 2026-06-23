import request from '../utils/request'

/**
 * 分页查询岗位列表
 */
export function getJobPage(params) {
  return request({
    url: '/api/job/page',
    method: 'get',
    params
  })
}

/**
 * 查询岗位详情
 */
export function getJobById(id) {
  return request({
    url: `/api/job/${id}`,
    method: 'get'
  })
}

/**
 * 新增岗位
 */
export function addJob(data) {
  return request({
    url: '/api/job',
    method: 'post',
    data
  })
}

/**
 * 修改岗位
 */
export function updateJob(id, data) {
  return request({
    url: `/api/job/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除岗位
 */
export function deleteJob(id) {
  return request({
    url: `/api/job/${id}`,
    method: 'delete'
  })
}

/**
 * 发布岗位
 */
export function publishJob(id) {
  return request({
    url: `/api/job/${id}/publish`,
    method: 'post'
  })
}

/**
 * 关闭岗位
 */
export function closeJob(id) {
  return request({
    url: `/api/job/${id}/close`,
    method: 'post'
  })
}

/**
 * 查询所有已发布岗位
 */
export function getPublishedJobs() {
  return request({
    url: '/api/job/published',
    method: 'get'
  })
}