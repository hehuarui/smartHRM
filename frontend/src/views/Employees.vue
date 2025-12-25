<template>
  <div class="employees-container">
    <el-card class="box-card">
      <!-- 卡片头部：标题 + 搜索/新增操作区 -->
      <template #header>
        <div class="card-header">
          <span class="card-title">
            <el-icon><User /></el-icon>
            员工管理
          </span>
          <div class="header-actions">
            <!-- 搜索输入框：支持清空触发搜索 -->
            <el-input
              v-model="searchForm.empName"
              placeholder="输入员工姓名搜索"
              style="width: 250px; margin-right: 10px"
              clearable
              @clear="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              查询
            </el-button>
            <el-button type="success" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              新增员工
            </el-button>
          </div>
        </div>
      </template>

      <!-- 员工列表表格：带加载状态、斑马纹、边框 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        style="width: 100%"
        border
      >
        <el-table-column prop="_id" label="员工ID" width="100" />
        <el-table-column prop="empName" label="员工姓名" width="120" />
        <!-- 所属部门列：根据部门类型显示不同样式标签 -->
        <el-table-column label="所属部门" width="150">
          <template #default="{ row }">
            <el-tag :type="getDeptTagType(row.deptType)">
              {{ row.deptName || '未分配' }}
            </el-tag>
          </template>
        </el-table-column>
        <!-- 技能列表列：多标签展示，显示技能名称+熟练度，无数据时显示"无" -->
        <el-table-column label="技能列表" min-width="200">
          <template #default="{ row }">
            <el-tag
              v-for="(skill, index) in (row.skillList || [])"
              :key="index"
              type="info"
              style="margin-right: 5px; margin-bottom: 5px"
            >
              {{ getSkillNameById(skill.skillId) }}（{{ skill.proficiency }}级）
            </el-tag>
            <span v-if="!row.skillList || row.skillList.length === 0" class="text-muted">无</span>
          </template>
        </el-table-column>
        <!-- 参与项目列：多标签展示，无数据时显示"无" -->
        <el-table-column label="参与项目" min-width="150">
          <template #default="{ row }">
            <el-tag
              v-for="(proj, index) in (row.projects || [])"
              :key="index"
              type="success"
              style="margin-right: 5px; margin-bottom: 5px"
            >
               {{ getProjectNameById(proj.projId || proj._id) }}
            </el-tag>
            <span v-if="!row.projects || row.projects.length === 0" class="text-muted">无</span>
          </template>
        </el-table-column>
        <!-- 加入时间列：格式化显示日期 -->
        <el-table-column prop="joinDate" label="加入时间" width="120">
          <template #default="{ row }">
            {{ formatDate(row.joinDate) }}
          </template>
        </el-table-column>
        <!-- 操作列：编辑/删除按钮，固定在右侧 -->
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页控件：支持页码/页大小切换 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :total="pagination.totalElements"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑员工对话框：点击模态框外部不关闭 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      :close-on-click-modal="false"
    >
      <!-- 表单：包含员工基本信息、部门、技能、项目等关联信息 -->
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <!-- 员工姓名：必填项 -->
        <el-form-item label="员工姓名" prop="name">
          <el-input v-model="formData.name" placeholder="请输入员工姓名" />
        </el-form-item>
        
        <!-- 所属部门选择：可清空 -->
        <el-form-item label="所属部门">
          <el-select
            v-model="formData.department"
            placeholder="请选择部门"
            clearable
            style="width: 100%"
          >
            <el-option
              v-for="dept in departments"
              :key="dept.id"
              :label="dept.depName"
              :value="dept.id ? dept.id.toString() : ''"
            />
          </el-select>
        </el-form-item>

        <!-- 加入时间选择：日期时间格式 -->
        <el-form-item label="加入时间">
          <el-date-picker
            v-model="formData.joinDate"
            type="datetime"
            placeholder="选择加入时间"
            style="width: 100%"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DDTHH:mm:ss"
          />
        </el-form-item>

        <!-- 技能列表：动态新增/删除技能项，关联隐藏输入框存储格式化字符串 -->
        <el-form-item label="技能列表">
          <!-- 动态技能项容器：整体靠右对齐 -->
          <div v-for="(skill, index) in formData.skillList" :key="index" class="skill-item">
            <el-row 
              :gutter="12" 
              align="middle" 
              justify="end"
              style="margin-bottom: 8px;"
            >
              <!-- 技能下拉框：加宽列宽 -->
              <el-col :span="12">
                <el-select
                  v-model="skill.skillId"
                  placeholder="请选择技能"
                  style="width: 100%"
                  @change="updateSkillsInput"
                  size="small"
                >
                  <el-option
                    v-for="s in allSkills"
                    :key="s._id || s.id"
                    :label="`${s.skillName || '未知技能'}（${s.skillKind || '通用'}）`"
                    :value="(s._id || s.id).toString()"
                  />
                </el-select>
              </el-col>
              <!-- 熟练度输入框：1-5级 -->
              <el-col :span="5">
                <el-input-number
                  v-model="skill.proficiency"
                  :min="1"
                  :max="5"
                  :step="1"
                  placeholder="熟练度"
                  style="width: 100%"
                  @change="updateSkillsInput"
                  size="small"
                  controls-position="right"
                />
              </el-col>
              <!-- 删除技能按钮 -->
              <el-col :span="5">
                <el-button
                  type="danger"
                  icon="el-icon-delete"
                  size="small"
                  @click="removeSkill(index)"
                  title="删除该技能"
                  style="padding: 2px 8px;"
                >
                  删除
                </el-button>
              </el-col>
            </el-row>
          </div>
          <!-- 新增技能按钮：靠右对齐 -->
          <div style="display: flex; justify-content: flex-end; margin-top: 8px;">
            <el-button
              type="primary"
              icon="el-icon-plus"
              size="small"
              @click="addSkill"
              style="padding: 4px 16px;"
            >
              新增技能
            </el-button>
          </div>
          <!-- 隐藏的技能字符串输入框：存储格式化后的技能数据 -->
          <el-input
            v-model="formData.skills"
            type="hidden"
          />
        </el-form-item>

        <!-- 关联项目选择：多选 -->
        <el-form-item label="关联项目">
          <el-select
            v-model="formData.newProjectIds"
            multiple
            placeholder="请选择项目"
            style="width: 100%"
          >
            <el-option
              v-for="proj in allProjects"
              :key="proj.id"
              :label="proj.projName"  
              :value="proj.id.toString()" 
            />
          </el-select>
        </el-form-item>

        <!-- 负责任务选择：多选 -->
        <el-form-item label="负责任务">
          <el-select
            v-model="formData.newManagerTaskIds"
            multiple
            placeholder="请选择任务"
            style="width: 100%"
          >
            <el-option
              v-for="task in allTasks"
              :key="task._id"
              :label="task.taskName || `任务${task._id}`"
              :value="task._id ? task._id.toString() : ''"
            />
          </el-select>
        </el-form-item>

        <!-- 参与培训选择：多选 -->
        <el-form-item label="参与培训">
          <el-select
            v-model="formData.newTrainingIds"
            multiple
            placeholder="请选择培训"
            style="width: 100%"
          >
            <el-option
              v-for="training in allTrainings"
              :key="training._id"
              :label="training.trainName"
              :value="training._id ? training._id.toString() : ''"
            />
          </el-select>
        </el-form-item>
      </el-form>

      <!-- 对话框底部按钮 -->
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
// 导入Vue核心API
import { ref, reactive, onMounted } from 'vue'
// 导入Element Plus消息提示/确认框组件
import { ElMessage, ElMessageBox } from 'element-plus'
// 导入Element Plus图标
import { User, Search, Plus, Edit, Delete, Close} from '@element-plus/icons-vue'
// 导入员工相关接口
import {
  getEmployees,
  addEmployee,
  updateEmployee,
  deleteEmployee,
  getEmployeeById,
  getEmployeeFormOptions
} from '@/api/employee'

// ===================== 基础状态控制 =====================
// 表格加载状态
const loading = ref(false)
// 表单提交加载状态
const submitting = ref(false)
// 新增/编辑对话框显示状态
const dialogVisible = ref(false)
// 对话框标题（区分新增/编辑）
const dialogTitle = ref('新增员工')
// 表单引用（用于校验）
const formRef = ref(null)

// ===================== 搜索/分页相关 =====================
// 搜索表单数据
const searchForm = reactive({
  empName: '' // 员工姓名搜索关键词
})

// 分页参数
const pagination = reactive({
  pageNum: 1,          // 当前页码
  pageSize: 10,        // 每页条数
  totalElements: 0,    // 总记录数
  totalPages: 0        // 总页数
})

// ===================== 数据存储 =====================
// 员工表格数据
const tableData = ref([])
// 部门列表（用于下拉选择）
const departments = ref([])
// 所有项目列表（用于下拉选择）
const allProjects = ref([])
// 所有任务列表（用于下拉选择）
const allTasks = ref([])
// 所有培训列表（用于下拉选择）
const allTrainings = ref([])
// 所有技能列表（用于下拉选择）
const allSkills = ref([])

// ===================== 表单相关 =====================
// 新增/编辑表单数据
const formData = reactive({
  id: null,                // 员工ID
  name: '',                // 员工姓名
  department: '',          // 所属部门ID（字符串类型）
  joinDate: null,          // 加入时间
  skills: '',              // 技能字符串（格式：skillId:proficiency,skillId:proficiency）
  skillList: [],           // 技能数组（用于表单展示/编辑）
  newProjectIds: [],       // 关联项目ID列表（字符串数组）
  newManagerTaskIds: [],   // 负责任务ID列表（字符串数组）
  newTrainingIds: []       // 参与培训ID列表（字符串数组）
})

// 表单校验规则
const formRules = {
  name: [
    { required: true, message: '请输入员工姓名', trigger: 'blur' }
  ]
}

// ===================== 工具函数 =====================
/**
 * 根据部门类型获取标签样式类型
 * @param {String} type - 部门类型（normal/deleted/unassigned）
 * @returns {String} 标签样式类型
 */
const getDeptTagType = (type) => {
  const typeMap = {
    normal: '',
    deleted: 'danger',
    unassigned: 'info'
  }
  return typeMap[type] || 'info'
}

/**
 * 格式化日期，避免无效日期报错
 * @param {String/Date} date - 原始日期值
 * @returns {String} 格式化后的日期字符串（YYYY-MM-DD）或提示文字
 */
const formatDate = (date) => {
  if (!date) return '未设置'
  try {
    const d = new Date(date)
    if (isNaN(d.getTime())) return '日期无效'
    return d.toLocaleDateString('zh-CN')
  } catch (e) {
    return '未设置'
  }
}

/**
 * 根据项目ID获取项目名称（兼容id/_id字段）
 * @param {String/Number} projId - 项目ID
 * @returns {String} 项目名称或兜底提示
 */
const getProjectNameById = (projId) => {
  if (!projId) return '未知项目';
  
  // 统一类型为字符串，避免类型不匹配
  const targetId = String(projId);
  
  // 匹配id/_id字段，兼容不同命名规则
  const project = allProjects.value.find(item => 
    String(item.id) === targetId || String(item._id) === targetId
  );
  
  // 匹配projName/name字段，兜底显示“项目+ID”
  return project ? (project.projName || project.name) : `项目${projId}`;
};

/**
 * 根据技能ID获取技能名称（兼容skillId/_id/id字段）
 * @param {String/Number} skillId - 技能ID
 * @returns {String} 技能名称或兜底提示
 */
const getSkillNameById = (skillId) => {
  if (!skillId) return '未知技能';
  
  // 统一类型为字符串，避免类型不匹配
  const targetId = String(skillId);
  
  // 匹配skillId/_id/id字段，兼容不同命名规则
  const skill = allSkills.value.find(item => 
    String(item.skillId) === targetId || String(item._id) === targetId || String(item.id) === targetId
  );
  
  // 匹配skillName/name字段，兜底显示“技能+ID”
  return skill ? (skill.skillName || skill.name) : `技能${skillId}`;
};

/**
 * 新增技能项到表单
 */
const addSkill = () => {
  formData.skillList.push({
    skillId: '',
    proficiency: null
  })
  updateSkillsInput()
}

/**
 * 删除指定索引的技能项
 * @param {Number} index - 技能项索引
 */
const removeSkill = (index) => {
  formData.skillList.splice(index, 1)
  updateSkillsInput()
}

/**
 * 格式化技能数组为字符串，存储到formData.skills
 * 格式：skillId:proficiency,skillId:proficiency
 */
const updateSkillsInput = () => {
  const skillList = []
  formData.skillList.forEach(item => {
    if (item.skillId && item.proficiency) {
      skillList.push(`${item.skillId}:${item.proficiency}`)
    }
  })
  formData.skills = skillList.join(',')
}

// ===================== 数据加载函数 =====================
/**
 * 加载员工列表数据（支持搜索、分页）
 */
const loadData = async () => {
  loading.value = true
  try {
    // 构造请求参数：空搜索关键词不传递
    const params = {
      empName: searchForm.empName || undefined,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    
    const data = await getEmployees(params)
    // 赋值到表格和分页
    tableData.value = data.employees || []
    pagination.pageNum = data.pageNum || 1
    pagination.pageSize = data.pageSize || 10
    pagination.totalElements = data.totalElements || 0
    pagination.totalPages = data.totalPages || 0
  } catch (error) {
    ElMessage.error('加载数据失败：' + error.message)
  } finally {
    loading.value = false
  }
}

/**
 * 加载表单下拉选项数据（部门、项目、任务、培训、技能）
 * 确保所有数据源为数组，避免undefined导致渲染异常
 */
const loadOptions = async () => {
  try {
    const options = await getEmployeeFormOptions()
    // 兼容处理：确保返回值为数组
    departments.value = Array.isArray(options.departments) ? options.departments : []
    allProjects.value = Array.isArray(options.allProjects) ? options.allProjects : []
    allTasks.value = Array.isArray(options.allTasks) ? options.allTasks : []
    allTrainings.value = Array.isArray(options.allTrainings) ? options.allTrainings : []
    allSkills.value = Array.isArray(options.allSkills) ? options.allSkills : []

    // 若员工列表已加载，重新加载确保数据匹配
    if (tableData.value.length > 0) {
      loadData();
    }
  } catch (error) {
    ElMessage.error('加载选项数据失败：' + error.message)
    // 兜底：设置为空数组
    departments.value = []
    allProjects.value = []
    allTasks.value = []
    allTrainings.value = []
  }
}

// ===================== 交互处理函数 =====================
/**
 * 处理搜索操作（重置页码为1后加载数据）
 */
const handleSearch = () => {
  pagination.pageNum = 1
  loadData()
}

/**
 * 处理每页条数变更
 * @param {Number} size - 新的每页条数
 */
const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.pageNum = 1
  loadData()
}

/**
 * 处理页码变更
 * @param {Number} page - 新的页码
 */
const handlePageChange = (page) => {
  pagination.pageNum = page
  loadData()
}

/**
 * 处理新增员工操作（重置表单并打开对话框）
 */
const handleAdd = () => {
  dialogTitle.value = '新增员工'
  // 重置表单数据
  Object.assign(formData, {
    id: null,
    name: '',
    department: '',
    joinDate: new Date().toISOString().slice(0, 19),
    skills: '',
    skillList: [],
    newProjectIds: [],
    newManagerTaskIds: [],
    newTrainingIds: []
  })
  dialogVisible.value = true
}

/**
 * 处理编辑员工操作（加载员工详情→填充表单→打开对话框）
 * @param {Object} row - 选中的员工行数据
 */
const handleEdit = async (row) => {
  // 校验员工ID是否存在
  if (!row || (!row._id && !row.id)) {
    ElMessage.warning('员工ID不存在，无法编辑！')
    return
  }
  const empId = row._id || row.id
  
  dialogTitle.value = '编辑员工'
  loading.value = true
  
  try {
    // 获取员工详情
    const data = await getEmployeeById(empId)
    const employee = data.employee || {}
    
    // 处理关联数据ID列表，确保为数组
    const existingProjectIds = Array.isArray(data.existingProjectIds) ? data.existingProjectIds : []
    const existingTaskIds = Array.isArray(data.existingTaskIds) ? data.existingTaskIds : []
    const existingTrainingIds = Array.isArray(data.existingTrainingIds) ? data.existingTrainingIds : []

    // 处理加入时间，格式化并兜底
    let joinDate = null
    if (employee.joinDate) {
      try {
        joinDate = new Date(employee.joinDate).toISOString().slice(0, 19)
      } catch (e) {
        joinDate = new Date().toISOString().slice(0, 19)
      }
    } else {
      joinDate = new Date().toISOString().slice(0, 19)
    }

    // 解析技能字符串为数组（格式：skillId:proficiency）
    const skillList = []
    if (data.existingSkillsStr && typeof data.existingSkillsStr === 'string') {
      const skillStrArray = data.existingSkillsStr.split(',')
      skillStrArray.forEach(skillStr => {
        const [skillId, proficiency] = skillStr.split(':')
        // 过滤无效数据
        if (skillId && proficiency && !isNaN(Number(proficiency))) {
          skillList.push({
            skillId: skillId.toString(),
            proficiency: Number(proficiency)
          })
        }
      })
    }

    // 填充表单数据
    Object.assign(formData, {
      id: employee._id || employee.id,
      name: employee.empName || '',
      department: employee.depId ? employee.depId.toString() : '',
      joinDate: joinDate,
      skills: data.existingSkillsStr || '',
      skillList: skillList,
      newProjectIds: existingProjectIds.map(id => id?.toString() || ''),
      newManagerTaskIds: existingTaskIds.map(id => id?.toString() || ''),
      newTrainingIds: existingTrainingIds.map(id => id?.toString() || '')
    })
    
    // 更新下拉选项数据
    departments.value = Array.isArray(data.departments) ? data.departments : departments.value
    allProjects.value = Array.isArray(data.allProjects) ? data.allProjects : allProjects.value
    allTasks.value = Array.isArray(data.allTasks) ? data.allTasks : allTasks.value
    allTrainings.value = Array.isArray(data.allTrainings) ? data.allTrainings : allTrainings.value
    allSkills.value = Array.isArray(data.allSkills) ? data.allSkills : allSkills.value
    
    dialogVisible.value = true
  } catch (error) {
    ElMessage.error('加载员工详情失败：' + error.message)
    dialogVisible.value = false
  } finally {
    loading.value = false
  }
}

/**
 * 处理表单提交（新增/编辑统一处理）
 */
const handleSubmit = async () => {
  if (!formRef.value) return
  
  // 表单校验
  const valid = await formRef.value.validate()
  if (!valid) return
  
  submitting.value = true
  try {
    // 构造提交参数，统一ID类型为数字
    const data = {
      id: formData.id,
      name: formData.name,
      department: formData.department ? Number(formData.department) : null,
      joinDate: formData.joinDate,
      skills: formData.skills,
      // 转换为数字数组并过滤无效值
      newProjectIds: formData.newProjectIds.map(id => Number(id)).filter(id => !isNaN(id)),
      newManagerTaskIds: formData.newManagerTaskIds.map(id => Number(id)).filter(id => !isNaN(id)),
      newTrainingIds: formData.newTrainingIds.map(id => Number(id)).filter(id => !isNaN(id))
    }
    
    // 区分新增/编辑接口
    if (formData.id) {
      await updateEmployee(data)
      ElMessage.success('员工更新成功')
    } else {
      await addEmployee(data)
      ElMessage.success('员工新增成功')
    }
    
    dialogVisible.value = false
    loadData() // 提交成功后刷新列表
  } catch (error) {
    ElMessage.error('提交失败：' + error.message)
  } finally {
    submitting.value = false
  }
}

/**
 * 处理删除员工操作（带确认提示）
 * @param {Object} row - 选中的员工行数据
 */
const handleDelete = async (row) => {
  // 校验员工ID是否存在
  if (!row || (!row._id && !row.id)) {
    ElMessage.warning('员工ID不存在，无法删除！')
    return
  }
  const empId = row._id || row.id
  
  try {
    // 确认删除
    await ElMessageBox.confirm(
      `确定要删除员工「${row.empName || '未知'}」吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 调用删除接口，传递分页和搜索参数
    await deleteEmployee(empId, {
      pageNum: pagination.pageNum,
      empName: searchForm.empName
    })
    
    ElMessage.success('员工删除成功')
    loadData() // 删除成功后刷新列表
  } catch (error) {
    // 排除取消操作的错误
    if (error !== 'cancel') {
      ElMessage.error('删除失败：' + error.message)
    }
  }
}

// ===================== 生命周期 =====================
/**
 * 页面挂载完成后初始化数据
 * 先加载下拉选项，再加载员工列表
 */
onMounted(async () => {
  await loadOptions();
  loadData();
});
</script>

<style scoped>
.employees-container {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
}

.header-actions {
  display: flex;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.text-muted {
  color: #909399;
  font-size: 12px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

/* 技能项整体样式 */
.skill-item {
  width: 100%;
  margin-bottom: 4px;
}

/* 穿透修改Element组件 - 统一缩小输入类组件尺寸 */
:deep(.el-select--small .el-input__wrapper),
:deep(.el-input-number--small .el-input__wrapper) {
  height: 32px;
  padding: 0 8px;
}

/* 按钮样式优化（整合所有小按钮样式） */
:deep(.el-button--small) {
  line-height: 1;
  padding: 2px 8px;
}

/* 熟练度输入框与删除按钮的间距 */
.skill-item :deep(.el-input-number) {
  margin-right: 4px;
}

/* 提示文字样式 */
.text-muted {
  color: #909399;
  font-size: 12px;
}
</style>
