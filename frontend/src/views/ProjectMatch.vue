<template>
  <div class="project-match-container">
    <!-- 项目管理面板 -->
    <el-card class="box-card" style="margin-bottom: 20px">
      <template #header>
        <div class="card-header">
          <span class="card-title">
            <el-icon><FolderOpened /></el-icon>
            项目管理
          </span>
          <div class="header-actions">
            <el-button type="success" @click="showCreateProjectModal">
              <el-icon><Plus /></el-icon>
              新增项目
            </el-button>
            <el-button type="danger" @click="deleteSelectedProjects" :disabled="selectedProjects.length === 0">
              <el-icon><Delete /></el-icon>
              批量删除
            </el-button>
            <el-button @click="refreshData">
              <el-icon><Refresh /></el-icon>
              刷新数据
            </el-button>
          </div>
        </div>
      </template>
    </el-card>

    <!-- 项目匹配筛选 -->
    <el-card class="box-card" style="margin-bottom: 20px">
      <template #header>
        <div class="card-header">
          <span class="card-title">
            <el-icon><Connection /></el-icon>
            项目匹配筛选
          </span>
        </div>
      </template>
      
      <div class="match-tips">
        <el-alert
          title="使用说明"
          type="info"
          :closable="false"
          show-icon
        >
          <template #default>
            选择搜索类型后输入相应的关键词。支持按项目名称模糊搜索或按员工ID查找其参与的项目。
            可添加多个搜索条件进行组合搜索。
          </template>
        </el-alert>
      </div>

      <div class="filter-container">
        <div
          v-for="(filter, index) in filters"
          :key="index"
          class="filter-row"
        >
          <el-select v-model="filter.searchType" style="width: 150px">
            <el-option label="项目名称" value="projectName" />
            <el-option label="员工ID" value="empId" />
          </el-select>
          <el-input
            v-model="filter.searchValue"
            :placeholder="filter.searchType === 'projectName' ? '输入项目名称' : '输入员工ID'"
            style="flex: 1; margin: 0 10px"
            @keyup.enter="doSearch"
          />
          <el-button
            type="danger"
            :icon="Delete"
            circle
            @click="removeFilter(index)"
            v-if="filters.length > 1"
          />
        </div>
        <el-button type="primary" @click="addFilterRow" style="margin-top: 10px">
          <el-icon><Plus /></el-icon>
          增加条件
        </el-button>
        <el-button type="success" @click="doSearch" style="margin-top: 10px; margin-left: 10px">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
      </div>
    </el-card>

    <!-- 匹配结果 -->
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">
            <el-icon><List /></el-icon>
            匹配结果
          </span>
          <div v-if="selectedProjects.length > 0" class="selected-info">
            已选中 {{ selectedProjects.length }} 项
          </div>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        style="width: 100%"
        border
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="_id" label="项目ID" width="100" sortable />
        <el-table-column prop="projName" label="项目名称" width="200" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'warning'">
              {{ row.status === 1 ? '已归档' : '未归档' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startDate" label="启动时间" width="150">
          <template #default="{ row }">
            {{ formatDate(row.startDate) }}
          </template>
        </el-table-column>
        <el-table-column label="所需技能" min-width="200">
          <template #default="{ row }">
            <el-tag
              v-for="(skill, index) in row.requiredSkills"
              :key="index"
              type="info"
              style="margin-right: 5px; margin-bottom: 5px"
            >
              技能{{ skill.skillId }}（最低{{ skill.minProficiency }}级）
            </el-tag>
            <span v-if="!row.requiredSkills || row.requiredSkills.length === 0" class="text-muted">无</span>
          </template>
        </el-table-column>
        <el-table-column label="团队成员" min-width="150">
          <template #default="{ row }">
            <el-tag
              v-for="(member, index) in row.members"
              :key="index"
              type="success"
              style="margin-right: 5px; margin-bottom: 5px"
            >
              员工{{ member }}
            </el-tag>
            <span v-if="!row.members || row.members.length === 0" class="text-muted">无</span>
          </template>
        </el-table-column>
        <el-table-column label="任务状态" width="120">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="viewTasks(row)">
              查看任务
            </el-button>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
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
    </el-card>

    <!-- 新增/编辑项目对话框 -->
    <el-dialog
      v-model="projectDialogVisible"
      :title="projectDialogTitle"
      width="700px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="projectFormRef"
        :model="projectForm"
        :rules="projectFormRules"
        label-width="120px"
      >
        <el-form-item label="项目ID" v-if="projectForm._id">
          <el-input v-model="projectForm._id" disabled />
        </el-form-item>
        
        <el-form-item label="项目名称" prop="projName">
          <el-input v-model="projectForm.projName" placeholder="请输入项目名称" />
        </el-form-item>

        <el-form-item label="项目状态">
          <el-radio-group v-model="projectForm.status">
            <el-radio :label="0">未归档</el-radio>
            <el-radio :label="1">已归档</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="启动时间">
          <el-date-picker
            v-model="projectForm.startDate"
            type="datetime"
            placeholder="选择启动时间"
            style="width: 100%"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DDTHH:mm:ss"
          />
        </el-form-item>

        <el-form-item label="所需技能">
          <el-input
            v-model="projectForm.requiredSkillsStr"
            type="textarea"
            :rows="3"
            placeholder="格式：skillId:minProficiency,skillId:minProficiency（如：1:3,2:4）"
          />
          <div class="form-tip">多个技能用逗号分隔，格式：技能ID:最低熟练度（1-5级）</div>
        </el-form-item>

        <el-form-item label="团队成员">
          <el-input
            v-model="projectForm.membersStr"
            type="textarea"
            :rows="3"
            placeholder="多个员工ID用逗号分隔，如：101, 102, 103"
          />
          <div class="form-tip">输入员工ID，多个员工用英文逗号分隔</div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="projectDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveProject" :loading="submitting">
          保存
        </el-button>
      </template>
    </el-dialog>

    <!-- 任务查看对话框 -->
    <el-dialog
      v-model="tasksDialogVisible"
      :title="`项目任务 - ${currentProject?.projName || ''}`"
      width="900px"
      :close-on-click-modal="false"
    >
      <div class="tasks-content">
        <div class="tasks-summary" v-if="tasksSummary">
          <el-statistic-group>
            <el-statistic title="总任务数" :value="tasksSummary.total" />
            <el-statistic title="未完成" :value="tasksSummary.pending" />
            <el-statistic title="已完成" :value="tasksSummary.completed" />
          </el-statistic-group>
        </div>

        <div class="tasks-filters">
          <el-button
            :type="taskFilter === 'all' ? 'primary' : ''"
            @click="filterTasks('all')"
          >
            全部任务
          </el-button>
          <el-button
            :type="taskFilter === 'pending' ? 'primary' : ''"
            @click="filterTasks('pending')"
          >
            未完成
          </el-button>
          <el-button
            :type="taskFilter === 'completed' ? 'primary' : ''"
            @click="filterTasks('completed')"
          >
            已完成
          </el-button>
        </div>

        <el-table
          :data="filteredTasks"
          style="width: 100%"
          max-height="400"
        >
          <el-table-column prop="taskName" label="任务名称" />
          <el-table-column prop="responsibleEmpId" label="负责人ID" width="120" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'warning'">
                {{ row.status === 1 ? '已完成' : '未完成' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="editTask(row)">
                编辑
              </el-button>
              <el-button type="danger" size="small" @click="deleteTaskItem(row)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div v-if="filteredTasks.length === 0" class="no-tasks">
          暂无任务
        </div>
      </div>

      <template #footer>
        <el-button @click="tasksDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="showCreateTaskModal">
          <el-icon><Plus /></el-icon>
          新增任务
        </el-button>
      </template>
    </el-dialog>

    <!-- 新增任务对话框 -->
    <el-dialog
      v-model="taskDialogVisible"
      title="新增任务"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="taskFormRef"
        :model="taskForm"
        :rules="taskFormRules"
        label-width="100px"
      >
        <el-form-item label="任务名称" prop="taskName">
          <el-input v-model="taskForm.taskName" placeholder="请输入任务名称" />
        </el-form-item>

        <el-form-item label="负责人ID">
          <el-input-number
            v-model="taskForm.responsibleEmpId"
            :min="1"
            placeholder="请输入负责人员工ID"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="任务状态">
          <el-radio-group v-model="taskForm.status">
            <el-radio :label="0">未完成</el-radio>
            <el-radio :label="1">已完成</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="taskDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveNewTask" :loading="submitting">
          保存任务
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  FolderOpened,
  Connection,
  List,
  Plus,
  Delete,
  Refresh,
  Search,
  Edit
} from '@element-plus/icons-vue'
import {
  getAllProjects,
  createProject,
  updateProject,
  deleteProject,
  deleteProjectsBatch,
  matchByProjectName,
  matchByEmployee,
  getProjectTasks,
  getProjectPendingTasks,
  getProjectCompletedTasks,
  createTask,
  updateTask,
  deleteTask as deleteTaskApi
} from '@/api/project'

const loading = ref(false)
const submitting = ref(false)
const projectDialogVisible = ref(false)
const projectDialogTitle = ref('新增项目')
const tasksDialogVisible = ref(false)
const taskDialogVisible = ref(false)
const projectFormRef = ref(null)
const taskFormRef = ref(null)

const filters = ref([
  { searchType: 'projectName', searchValue: '' }
])

const tableData = ref([])
const selectedProjects = ref([])
const currentProject = ref(null)
const allTasks = ref([])
const taskFilter = ref('all')

const projectForm = reactive({
  _id: null,
  projName: '',
  status: 0,
  startDate: null,
  requiredSkillsStr: '',
  membersStr: ''
})

const projectFormRules = {
  projName: [
    { required: true, message: '请输入项目名称', trigger: 'blur' }
  ]
}

const taskForm = reactive({
  taskName: '',
  responsibleEmpId: null,
  status: 0
})

const taskFormRules = {
  taskName: [
    { required: true, message: '请输入任务名称', trigger: 'blur' }
  ]
}

const tasksSummary = computed(() => {
  if (allTasks.value.length === 0) return null
  return {
    total: allTasks.value.length,
    pending: allTasks.value.filter(t => t.status === 0).length,
    completed: allTasks.value.filter(t => t.status === 1).length
  }
})

const filteredTasks = computed(() => {
  if (taskFilter.value === 'all') return allTasks.value
  if (taskFilter.value === 'pending') return allTasks.value.filter(t => t.status === 0)
  if (taskFilter.value === 'completed') return allTasks.value.filter(t => t.status === 1)
  return allTasks.value
})

const addFilterRow = () => {
  filters.value.push({ searchType: 'projectName', searchValue: '' })
}

const removeFilter = (index) => {
  filters.value.splice(index, 1)
}

const doSearch = async () => {
  loading.value = true
  try {
    const results = []
    
    for (const filter of filters.value) {
      if (!filter.searchValue.trim()) continue
      
      let result = []
      if (filter.searchType === 'projectName') {
        result = await matchByProjectName(filter.searchValue)
      } else if (filter.searchType === 'empId') {
        result = await matchByEmployee(parseInt(filter.searchValue))
      }
      
      results.push(...result)
    }
    
    // 去重
    const uniqueResults = results.filter((project, index, self) =>
      index === self.findIndex(p => p._id === project._id)
    )
    
    tableData.value = uniqueResults
    ElMessage.success(`找到 ${uniqueResults.length} 个项目`)
  } catch (error) {
    ElMessage.error('搜索失败：' + error.message)
  } finally {
    loading.value = false
  }
}

const refreshData = async () => {
  loading.value = true
  try {
    const result = await getAllProjects()
    tableData.value = result || []
    ElMessage.success('数据刷新成功')
  } catch (error) {
    ElMessage.error('刷新失败：' + error.message)
  } finally {
    loading.value = false
  }
}

const handleSelectionChange = (selection) => {
  selectedProjects.value = selection.map(p => p._id)
}

const deleteSelectedProjects = async () => {
  if (selectedProjects.value.length === 0) return
  
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedProjects.value.length} 个项目吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteProjectsBatch(selectedProjects.value)
    selectedProjects.value = []
    refreshData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
    }
  }
}

const showCreateProjectModal = () => {
  projectDialogTitle.value = '新增项目'
  Object.assign(projectForm, {
    _id: null,
    projName: '',
    status: 0,
    startDate: null,
    requiredSkillsStr: '',
    membersStr: ''
  })
  projectDialogVisible.value = true
}

const handleEdit = (row) => {
  projectDialogTitle.value = '编辑项目'
  Object.assign(projectForm, {
    _id: row._id,
    projName: row.projName,
    status: row.status || 0,
    startDate: row.startDate,
    requiredSkillsStr: row.requiredSkills
      ? row.requiredSkills.map(s => `${s.skillId}:${s.minProficiency}`).join(',')
      : '',
    membersStr: row.members ? row.members.join(', ') : ''
  })
  projectDialogVisible.value = true
}

const saveProject = async () => {
  if (!projectFormRef.value) return
  
  await projectFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      // 解析所需技能
      const requiredSkills = projectForm.requiredSkillsStr
        ? projectForm.requiredSkillsStr.split(',').map(s => {
            const [skillId, minProficiency] = s.trim().split(':')
            return {
              skillId: parseInt(skillId),
              minProficiency: parseInt(minProficiency) || 1
            }
          }).filter(s => s.skillId)
        : []
      
      // 解析团队成员
      const members = projectForm.membersStr
        ? projectForm.membersStr.split(',').map(id => parseInt(id.trim())).filter(id => !isNaN(id))
        : []
      
      const data = {
        _id: projectForm._id,
        projName: projectForm.projName,
        status: projectForm.status,
        startDate: projectForm.startDate,
        requiredSkills,
        members
      }
      
      if (projectForm._id) {
        await updateProject(data)
      } else {
        await createProject(data)
      }
      
      projectDialogVisible.value = false
      refreshData()
    } catch (error) {
      console.error('保存失败:', error)
    } finally {
      submitting.value = false
    }
  })
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除项目「${row.projName}」吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteProject(row._id)
    refreshData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

const viewTasks = async (row) => {
  currentProject.value = row
  tasksDialogVisible.value = true
  await loadTasks(row._id)
}

const loadTasks = async (projId) => {
  try {
    allTasks.value = await getProjectTasks(projId) || []
  } catch (error) {
    ElMessage.error('加载任务失败：' + error.message)
  }
}

const filterTasks = (type) => {
  taskFilter.value = type
}

const showCreateTaskModal = () => {
  Object.assign(taskForm, {
    taskName: '',
    responsibleEmpId: null,
    status: 0
  })
  taskDialogVisible.value = true
}

const saveNewTask = async () => {
  if (!taskFormRef.value) return
  
  await taskFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      const data = {
        projectId: currentProject.value._id,
        taskName: taskForm.taskName,
        responsibleEmpId: taskForm.responsibleEmpId,
        status: taskForm.status
      }
      
      await createTask(data)
      taskDialogVisible.value = false
      await loadTasks(currentProject.value._id)
    } catch (error) {
      console.error('保存任务失败:', error)
    } finally {
      submitting.value = false
    }
  })
}

const editTask = (row) => {
  Object.assign(taskForm, {
    taskName: row.taskName,
    responsibleEmpId: row.responsibleEmpId,
    status: row.status
  })
  taskDialogVisible.value = true
}

const deleteTaskItem = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除任务「${row.taskName}」吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteTaskApi(row._id)
    await loadTasks(currentProject.value._id)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除任务失败:', error)
    }
  }
}

const formatDate = (date) => {
  if (!date) return '未设置'
  const d = new Date(date)
  return d.toLocaleString('zh-CN')
}

onMounted(() => {
  refreshData()
})
</script>

<style scoped>
.project-match-container {
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
  gap: 10px;
}

.selected-info {
  color: #409EFF;
  font-weight: 500;
}

.match-tips {
  margin-bottom: 20px;
}

.filter-container {
  padding: 10px 0;
}

.filter-row {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.tasks-content {
  padding: 10px 0;
}

.tasks-summary {
  margin-bottom: 20px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
}

.tasks-filters {
  margin-bottom: 20px;
}

.tasks-filters .el-button {
  margin-right: 10px;
}

.no-tasks {
  text-align: center;
  padding: 40px;
  color: #909399;
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
</style>

