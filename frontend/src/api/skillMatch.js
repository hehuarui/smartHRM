import request from './request'

// 技能匹配
export function matchBySkills(requiredSkills) {
  return request({
    url: '/skillmatch/',
    method: 'post',
    params: { requiredSkills }
  })
}

// 获取所有技能（用于下拉选择）
export function getAllSkills() {
  return request({
    url: '/skillmatch/skills',
    method: 'get'
  })
}

// 获取所有项目
export function getAllProjects() {
  return request({
    url: '/skillmatch/projects',
    method: 'get'
  })
}

// 获取所有部门
export function getAllDepartments() {
  return request({
    url: '/skillmatch/departments',
    method: 'get'
  })
}

