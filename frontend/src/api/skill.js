import request from './request'

// 获取技能列表（分页）
export function getSkills(params) {
  return request({
    url: '/skill/list',
    method: 'get',
    params
  })
}

// 获取所有技能
export function getAllSkills() {
  return request({
    url: '/skill/list',
    method: 'get'
  })
}

// 搜索技能
export function searchSkills(name) {
  return request({
    url: '/skill/search',
    method: 'get',
    params: { name }
  })
}

// 获取技能详情
export function getSkillById(id) {
  return request({
    url: `/skill/${id}`,
    method: 'get'
  })
}

// 新增技能
export function addSkill(data) {
  return request({
    url: '/skill/add',
    method: 'post',
    data
  })
}

// 更新技能
export function updateSkill(data) {
  return request({
    url: '/skill/update',
    method: 'post',
    data
  })
}

// 删除技能
export function deleteSkill(id) {
  return request({
    url: `/skill/delete/${id}`,
    method: 'delete'
  })
}

