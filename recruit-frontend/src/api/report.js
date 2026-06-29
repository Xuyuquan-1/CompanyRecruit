import request from '../utils/request'

// 仪表盘数据（招聘进度统计）
export function getDashboard(params) {
  return request({ 
    url: '/api/report/dashboard', 
    method: 'get',
    params 
  })
}

// 招聘效果分析（多维度）
export function getAnalysis(params) {
  return request({ 
    url: '/api/report/analysis', 
    method: 'get',
    params 
  })
}

// 招聘进度统计（旧接口兼容）
export function getRecruitmentProgress(params) {
  return request({ 
    url: '/api/report/progress', 
    method: 'get',
    params 
  })
}

// 招聘效果分析（旧接口兼容）
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
