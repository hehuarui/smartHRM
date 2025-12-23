import request from './request'

/**
 * 获取培训列表（分页）
 * 对应后端：GET /training/list
 * 参数：page（从0开始），size
 */
export function getTrainings(params) {
  return request({
    url: '/training/list',
    method: 'get',
    params
  })
}

/**
 * 搜索培训
 * 对应后端：GET /training/search
 * 参数：name 培训名称关键词
 */
export function searchTrainings(name) {
  return request({
    url: '/training/search',
    method: 'get',
    params: { name }
  })
}

/**
 * 根据技能ID获取培训
 * 对应后端：GET /training/bySkill/{skillId}
 */
export function getTrainingsBySkill(skillId) {
  return request({
    url: `/training/bySkill/${skillId}`,
    method: 'get'
  })
}

/**
 * 新增培训
 * 对应后端：POST /training/add
 * 说明：后端使用 @RequestBody Training，因此这里用 data(JSON) 传输
 */
export function addTraining(data) {
  return request({
    url: '/training/add',
    method: 'post',
    data
  })
}

/**
 * 更新培训
 * 对应后端：POST /training/update
 * 说明：同样使用 JSON 体提交 Training 对象
 */
export function updateTraining(data) {
  return request({
    url: '/training/update',
    method: 'post',
    data
  })
}

/**
 * 删除培训
 * 对应后端：DELETE /training/delete/{id}
 */
export function deleteTraining(id) {
  return request({
    url: `/training/delete/${id}`,
    method: 'delete'
  })
}

