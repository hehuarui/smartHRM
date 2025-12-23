import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/layouts/Layout.vue'

const routes = [
  {
    path: '/',
    component: Layout,
    redirect: '/employees',
    children: [
      {
        path: 'employees',
        name: 'Employees',
        component: () => import('@/views/Employees.vue'),
        meta: { title: '员工管理', icon: 'User' }
      },
      {
        path: 'departments',
        name: 'Departments',
        component: () => import('@/views/Departments.vue'),
        meta: { title: '部门管理', icon: 'OfficeBuilding' }
      },
      {
        path: 'skills',
        name: 'Skills',
        component: () => import('@/views/Skills.vue'),
        meta: { title: '技能管理', icon: 'Trophy' }
      },
      {
        path: 'trainings',
        name: 'Trainings',
        component: () => import('@/views/Trainings.vue'),
        meta: { title: '培训管理', icon: 'Reading' }
      },
      {
        path: 'project-match',
        name: 'ProjectMatch',
        component: () => import('@/views/ProjectMatch.vue'),
        meta: { title: '项目匹配', icon: 'Connection' }
      },
      {
        path: 'skill-match',
        name: 'SkillMatch',
        component: () => import('@/views/SkillMatch.vue'),
        meta: { title: '技能匹配', icon: 'Search' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router

