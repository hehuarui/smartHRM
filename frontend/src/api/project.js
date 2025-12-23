import request from './request'

// 获取所有项目
export function getAllProjects() {
  return request({
    url: '/projectmatch/projects',
    method: 'get'
  })
}

// 创建项目
export function createProject(data) {
  return request({
    url: '/projectmatch/create',
    method: 'post',
    data
  })
}

// 更新项目
export function updateProject(data) {
  return request({
    url: '/projectmatch/update',
    method: 'put',
    data
  })
}

// 删除项目
export function deleteProject(projectId) {
  return request({
    url: `/projectmatch/delete/${projectId}`,
    method: 'delete'
  })
}

// 批量删除项目
export function deleteProjectsBatch(projectIds) {
  return request({
    url: '/projectmatch/delete/batch',
    method: 'post',
    data: projectIds
  })
}

// 获取项目详情
export function getProjectDetail(projectId) {
  return request({
    url: `/projectmatch/detail/${projectId}`,
    method: 'get'
  })
}

// 项目匹配（按项目名称）
export function matchByProjectName(searchValue) {
  return request({
    url: '/projectmatch/',
    method: 'post',
    params: {
      searchType: 'projectName',
      searchValue
    }
  })
}

// 项目匹配（按员工ID）
export function matchByEmployee(empId) {
  return request({
    url: '/projectmatch/',
    method: 'post',
    params: {
      searchType: 'empId',
      searchValue: empId
    }
  })
}

// 获取项目任务
export function getProjectTasks(projId) {
  return request({
    url: `/projectmatch/tasks/${projId}`,
    method: 'get'
  })
}

// 获取项目未完成任务
export function getProjectPendingTasks(projId) {
  return request({
    url: `/projectmatch/tasks/${projId}/pending`,
    method: 'get'
  })
}

// 获取项目已完成任务
export function getProjectCompletedTasks(projId) {
  return request({
    url: `/projectmatch/tasks/${projId}/completed`,
    method: 'get'
  })
}

// 创建任务
export function createTask(data) {
  return request({
    url: '/projectmatch/tasks/create',
    method: 'post',
    data
  })
}

// 更新任务
export function updateTask(data) {
  return request({
    url: '/projectmatch/tasks/update',
    method: 'put',
    data
  })
}

// 删除任务
export function deleteTask(taskId) {
  return request({
    url: `/projectmatch/tasks/delete/${taskId}`,
    method: 'delete'
  })
}

