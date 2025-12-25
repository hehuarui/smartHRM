import request from './request'

/**
 * 获取员工列表（分页查询）
 * @param {Object} params - 查询参数
 * @param {String} [params.empName] - 员工姓名（模糊查询，可选）
 * @param {Number} [params.pageNum=1] - 当前页码（默认1）
 * @param {Number} [params.pageSize=10] - 每页条数（默认10）
 * @returns {Promise<Object>} 包含分页员工数据的Promise对象
 * @throws {Error} 请求失败时抛出错误，包含错误提示信息
 */
export function getEmployees(params) {
  return request({
    url: '/employees/',
    method: 'get',
    params
  }).then(response => {
    if (response.success) {
      return response.data;
    } else {
      throw new Error(response.message || '获取员工列表失败');
    }
  });
}

/**
 * 获取所有员工（全量数据，无分页）
 * 注：通过设置超大pageSize实现全量查询，适配下拉选择等场景
 * @returns {Promise<Object>} 包含所有员工数据的Promise对象
 * @throws {Error} 请求失败时抛出错误，包含错误提示信息
 */
export const getAllEmployees = () => {
  return request({
    url: '/employees/',
    method: 'get',
    params: {
      pageNum: 1,
      pageSize: 9999
    }
  }).then(response => {
    if (response.success) {
      return response.data;
    } else {
      throw new Error(response.message || '获取员工列表失败');
    }
  });
}

/**
 * 根据ID获取单个员工详情
 * @param {Number|String} id - 员工ID（必填）
 * @returns {Promise<Object>} 包含员工详情的Promise对象
 * @throws {Error} 请求失败时抛出错误，包含错误提示信息
 */
export function getEmployeeById(id) {
  return request({
    url: `/employees/${id}`,
    method: 'get'
  }).then(response => {
    if (response.success) {
      return response.data;
    } else {
      throw new Error(response.message || '获取员工详情失败');
    }
  });
}

/**
 * 获取员工表单所需的下拉选项数据（部门、项目、任务、培训、技能等）
 * @returns {Promise<Object>} 包含各类选项数据的Promise对象
 * @throws {Error} 请求失败时抛出错误，包含错误提示信息
 */
export function getEmployeeFormOptions() {
  return request({
    url: '/employees/form-options',
    method: 'get'
  }).then(response => {
    if (response.success) {
      return response.data;
    } else {
      throw new Error(response.message || '获取表单选项失败');
    }
  });
}

/**
 * 新增员工
 * @param {Object} data - 员工新增数据
 * @param {String} data.name - 员工姓名（必填）
 * @param {Number|String} [data.department] - 所属部门ID
 * @param {String} [data.joinDate] - 加入时间（格式：YYYY-MM-DDTHH:mm:ss）
 * @param {String} [data.skills] - 技能字符串（格式：skillId:proficiency,skillId:proficiency）
 * @param {Array} [data.newProjectIds] - 关联项目ID数组
 * @param {Array} [data.newManagerTaskIds] - 负责任务ID数组
 * @param {Array} [data.newTrainingIds] - 参与培训ID数组
 * @returns {Promise<Object>} 请求响应Promise对象
 * @throws {Error} 请求失败时抛出错误，包含错误提示信息
 */
export function addEmployee(data) {
  return request({
    url: '/employees/add',
    method: 'post',
    data
  }).then(response => {
    if (response.success) {
      return response;
    } else {
      throw new Error(response.message || '新增员工失败');
    }
  });
}

/**
 * 编辑更新员工信息
 * @param {Object} data - 员工更新数据
 * @param {Number|String} data.id - 员工ID（必填）
 * @param {String} data.name - 员工姓名（必填）
 * @param {Number|String} [data.department] - 所属部门ID
 * @param {String} [data.joinDate] - 加入时间（格式：YYYY-MM-DDTHH:mm:ss）
 * @param {String} [data.skills] - 技能字符串（格式：skillId:proficiency,skillId:proficiency）
 * @param {Array} [data.newProjectIds] - 关联项目ID数组
 * @param {Array} [data.newManagerTaskIds] - 负责任务ID数组
 * @param {Array} [data.newTrainingIds] - 参与培训ID数组
 * @returns {Promise<Object>} 请求响应Promise对象
 * @throws {Error} 请求失败时抛出错误，包含错误提示信息
 */
export function updateEmployee(data) {
  return request({
    url: '/employees/mod',
    method: 'post',
    data
  }).then(response => {
    if (response.success) {
      return response;
    } else {
      throw new Error(response.message || '更新员工失败');
    }
  });
}

/**
 * 删除指定员工
 * @param {Number|String} id - 员工ID（必填）
 * @param {Object} [extraParams] - 额外参数（如分页、搜索条件，可选）
 * @returns {Promise<Object>} 请求响应Promise对象
 * @throws {Error} 请求失败时抛出错误，包含错误提示信息
 */
export function deleteEmployee(id, extraParams) {
  return request({
    url: '/employees/del',
    method: 'post',
    params: { id, ...(extraParams || {}) }
  }).then(response => {
    if (response.success) {
      return response;
    } else {
      throw new Error(response.message || '删除员工失败');
    }
  });
}
