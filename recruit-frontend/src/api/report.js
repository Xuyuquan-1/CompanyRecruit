import request from '../utils/request'

// 招聘进度统计
export function getRecruitmentProgress(params) {
  return request({ 
    url: '/api/report/progress', 
    method: 'get',
    params 
  })
}

// 招聘效果分析
export function getRecruitmentEffect(params) {
  return request({ 
    url: '/api/report/effect', 
    method: 'get',
    params 
  })
}

// 导出Excel报表
export function exportReportExcel(params) {
  return request({ 
    url: '/api/report/export', 
    method: 'get', 
    params,
    responseType: 'blob' 
  })
}
