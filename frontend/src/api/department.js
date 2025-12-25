import request from './request'

/**
 * 获取部门列表（分页查询）
 * @param {Object} params - 查询参数
 * @param {String} [params.searchKey] - 模糊查询部门名称
 * @param {Number} params.pageNum - 当前页码
 * @param {Number} params.pageSize - 每页条数
 * @returns {Promise} 请求Promise对象
 */
export function getDepartments(params) {
  return request({
    url: '/departments/',
    method: 'get',
    params
  })
}

/**
 * 根据ID获取单个部门详情
 * @param {Number|String} id - 部门ID
 * @returns {Promise} 请求Promise对象
 */
export function getDepartmentById(id) {
  return request({
    url: `/departments/${id}`,
    method: 'get'
  })
}

/**
 * 获取指定部门下的员工列表
 * @param {Number|String} id - 部门ID
 * @returns {Promise} 请求Promise对象
 */
export function getDepartmentEmployees(id) {
  return request({
    url: `/departments/${id}/employees`,
    method: 'get'
  })
}

/**
 * 获取所有部门列表（无分页，用于下拉选择等场景）
 * @returns {Promise} 请求Promise对象
 */
export function getAllDepartments() {
  return request({
    url: '/departments/all',
    method: 'get'
  })
}

/**
 * 保存部门信息（新增/编辑统一处理）
 * @param {Object} data - 部门数据
 * @param {Number|String} [data.id] - 部门ID（新增不传/传空，编辑必传）
 * @param {String} data.depName - 部门名称（必填）
 * @param {Number|String} [data.managerId] - 部门负责人ID
 * @param {Array} [data.empIds] - 部门员工ID数组
 * @returns {Promise} 请求Promise对象
 */
export function saveDepartment(data) {
  return request({
    url: '/departments/save',
    method: 'post',
    data
  })
}

/**
 * 删除指定部门
 * @param {Number|String} id - 部门ID（必填）
 * @returns {Promise} 请求Promise对象
 */
export function deleteDepartment(id) {
  return request({
    url: '/departments/delete',
    method: 'post',
    params: { id }
  })
}
