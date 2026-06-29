import { createRouter, createWebHistory } from 'vue-router'
import Layout from '../layout/index.vue'

const publicRoutes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/index.vue'),
    meta: { title: '登录' }
  }
]

const protectedRoutes = [
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/dashboard/index.vue'),
        meta: { title: '首页', icon: 'HomeFilled' }
      }
    ]
  },
  {
    path: '/job',
    component: Layout,
    redirect: '/job/list',
    meta: { title: '岗位管理', icon: 'Briefcase' },
    children: [
      {
        path: 'list',
        name: 'JobList',
        component: () => import('../views/job/index.vue'),
        meta: { title: '岗位列表', icon: 'List', permission: 'job:list' }
      }
    ]
  },
  {
    path: '/resume',
    component: Layout,
    redirect: '/resume/list',
    meta: { title: '简历管理', icon: 'Document' },
    children: [
      {
        path: 'list',
        name: 'ResumeList',
        component: () => import('../views/resume/index.vue'),
        meta: { title: '简历列表', icon: 'List', permission: 'resume:list' }
      }
    ]
  },
  {
    path: '/application',
    component: Layout,
    redirect: '/application/list',
    meta: { title: '应聘筛选', icon: 'Filter', roles: ['ROLE_HR', 'ROLE_ADMIN'] },
    children: [
      {
        path: 'list',
        name: 'ApplicationList',
        component: () => import('../views/application/index.vue'),
        meta: { title: '应聘列表', icon: 'List', permission: 'application:list' }
      }
    ]
  },
  {
    path: '/interview',
    component: Layout,
    redirect: '/interview/list',
    meta: { title: '面试管理', icon: 'ChatDotSquare' },
    children: [
      {
        path: 'list',
        name: 'InterviewList',
        component: () => import('../views/interview/index.vue'),
        meta: { title: '面试列表', icon: 'List', permission: 'interview:list' }
      }
    ]
  },
  {
    path: '/offer',
    component: Layout,
    redirect: '/offer/list',
    meta: { title: '录用管理', icon: 'CircleCheck' },
    children: [
      {
        path: 'list',
        name: 'OfferList',
        component: () => import('../views/offer/index.vue'),
        meta: { title: '录用列表', icon: 'List', permission: 'offer:list' }
      }
    ]
  },
  {
    path: '/employee',
    component: Layout,
    redirect: '/employee/list',
    meta: { title: '员工管理', icon: 'Avatar' },
    children: [
      {
        path: 'list',
        name: 'EmployeeList',
        component: () => import('../views/employee/index.vue'),
        meta: { title: '员工列表', icon: 'List', permission: 'employee:list' }
      }
    ]
  },
  {
    path: '/report',
    component: Layout,
    redirect: '/report/dashboard',
    meta: { title: '报表中心', icon: 'DataAnalysis' },
    children: [
      {
        path: 'dashboard',
        name: 'ReportDashboard',
        component: () => import('../views/report/index.vue'),
        meta: { title: '招聘进度统计', icon: 'PieChart', permission: 'report:list' }
      },
      {
        path: 'analysis',
        name: 'ReportAnalysis',
        component: () => import('../views/report/analysis.vue'),
        meta: { title: '招聘效果分析', icon: 'TrendCharts', permission: 'report:list' }
      },
      {
        path: 'export',
        name: 'ReportExport',
        component: () => import('../views/report/export.vue'),
        meta: { title: '报表导出', icon: 'Download', permission: 'report:export' }
      }
    ]
  },
  {
    path: '/aichat',
    component: Layout,
    redirect: '/aichat/chat',
    meta: { title: 'AI助手', icon: 'ChatLineRound' },
    children: [
      {
        path: 'chat',
        name: 'AiChat',
        component: () => import('../views/aichat/index.vue'),
        meta: { title: '智能问答', icon: 'ChatDotSquare', permission: 'ai' }
      }
    ]
  },
  {
    path: '/system',
    component: Layout,
    redirect: '/system/user',
    meta: { title: '系统管理', icon: 'Setting', permission: 'system:user' },
    children: [
      {
        path: 'user',
        name: 'SystemUser',
        component: () => import('../views/system/user.vue'),
        meta: { title: '用户管理', icon: 'UserFilled', permission: 'system:user' }
      },
      {
        path: 'role',
        name: 'SystemRole',
        component: () => import('../views/system/role.vue'),
        meta: { title: '角色管理', icon: 'Lock', permission: 'system:role' }
      },
      {
        path: 'menu',
        name: 'SystemMenu',
        component: () => import('../views/system/menu.vue'),
        meta: { title: '菜单管理', icon: 'Menu', permission: 'system:role' }
      }
    ]
  },
  // 应聘者专属页面
  {
    path: '/candidate/jobs',
    component: Layout,
    name: 'CandidateJobs',
    meta: { title: '浏览岗位', icon: 'Briefcase', roles: ['ROLE_CANDIDATE'] },
    redirect: '/candidate/jobs/index',
    children: [
      {
        path: 'index',
        component: () => import('../views/candidate/jobs.vue'),
        meta: { title: '浏览岗位', icon: 'List' }
      }
    ]
  },
  {
    path: '/candidate/applications',
    component: Layout,
    name: 'CandidateApplications',
    meta: { title: '我的应聘', icon: 'Document', roles: ['ROLE_CANDIDATE'] },
    redirect: '/candidate/applications/index',
    children: [
      {
        path: 'index',
        component: () => import('../views/candidate/applications.vue'),
        meta: { title: '我的应聘', icon: 'Document' }
      }
    ]
  },
  {
    path: '/candidate/interviews',
    component: Layout,
    name: 'CandidateInterviews',
    meta: { title: '我的面试', icon: 'ChatDotSquare', roles: ['ROLE_CANDIDATE'] },
    redirect: '/candidate/interviews/index',
    children: [
      {
        path: 'index',
        component: () => import('../views/candidate/interviews.vue'),
        meta: { title: '我的面试', icon: 'ChatDotSquare' }
      }
    ]
  },
  {
    path: '/candidate/offers',
    component: Layout,
    name: 'CandidateOffers',
    meta: { title: '我的Offer', icon: 'CircleCheck', roles: ['ROLE_CANDIDATE'] },
    redirect: '/candidate/offers/index',
    children: [
      {
        path: 'index',
        component: () => import('../views/candidate/offers.vue'),
        meta: { title: '我的Offer', icon: 'CircleCheck' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes: [...publicRoutes, ...protectedRoutes]
})

export default router