<template>
  <div class="departments-container">
    <el-card class="box-card">
      <!-- 卡片头部：标题 + 搜索/新增操作区 -->
      <template #header>
        <div class="card-header">
          <span class="card-title">
            <el-icon><OfficeBuilding /></el-icon>
            部门管理
          </span>
          <div class="header-actions">
            <!-- 搜索输入框：支持清空触发搜索 -->
            <el-input
              v-model="searchForm.searchKey"
              placeholder="输入部门名称搜索"
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
              新增部门
            </el-button>
          </div>
        </div>
      </template>

      <!-- 部门列表表格：带加载状态、斑马纹、边框 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        style="width: 100%"
        border
      >
        <el-table-column prop="id" label="部门ID" width="100" />
        <el-table-column prop="depName" label="部门名称" width="150" />
        <!-- 负责人列：无数据时显示"未设置" -->
        <el-table-column label="负责人" width="120">
          <template #default="{ row }">
            {{ row.managerName || '未设置' }}
          </template>
        </el-table-column>
        <!-- 员工数量列：基于empList长度展示，兜底为0 -->
        <el-table-column label="员工数量" width="100">
          <template #default="{ row }">
            <el-tag type="info">{{ (row.empList?.length) || 0 }}人</el-tag>
          </template>
        </el-table-column>
        <!-- 员工列表列：多标签展示，无数据时显示"无" -->
        <el-table-column label="员工列表" min-width="200">
          <template #default="{ row }">
            <el-tag
              v-for="(emp, index) in row.empList"
              :key="index"
              type="success"
              style="margin-right: 5px; margin-bottom: 5px"
            >
              {{ emp.empName || `员工${emp.id}` }}
            </el-tag>
            <span v-if="!row.empList || row.empList.length === 0" class="text-muted">无</span>
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
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑部门对话框：点击模态框外部不关闭 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="700px"
      :close-on-click-modal="false"
    >
      <!-- 表单：包含部门基本信息、负责人、员工选择 -->
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <!-- 部门ID：编辑时禁用，新增时自动生成 -->
        <el-form-item label="部门ID" prop="id">
          <el-input
            v-model="formData.id"
            :disabled="!!formData.id"
            placeholder="新增时留空自动生成"
          />
        </el-form-item>
        
        <!-- 部门名称：必填项 -->
        <el-form-item label="部门名称" prop="depName">
          <el-input v-model="formData.depName" placeholder="请输入部门名称" />
        </el-form-item>

        <!-- 负责人选择：可清空、可搜索 -->
        <el-form-item label="负责人">
          <el-select
            v-model="formData.managerId"
            placeholder="请选择负责人"
            clearable
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="emp in allEmployees"
              :key="emp.id"
              :label="emp.empName"
              :value="emp.id"
            />
          </el-select>
        </el-form-item>

        <!-- 部门员工选择：多选、可搜索 -->
        <el-form-item label="部门员工">
          <el-select
            v-model="formData.employeeIds"
            multiple
            placeholder="请选择部门员工"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="emp in allEmployees"
              :key="emp.id"
              :label="emp.empName"
              :value="emp.id"
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
import { OfficeBuilding, Search, Plus, Edit, Delete } from '@element-plus/icons-vue'
// 导入部门相关接口
import { getDepartments, saveDepartment, deleteDepartment } from '@/api/department'
// 导入员工相关接口
import { getAllEmployees } from '@/api/employee'

// ===================== 基础状态控制 =====================
// 表格加载状态
const loading = ref(false)
// 表单提交加载状态
const submitting = ref(false)
// 新增/编辑对话框显示状态
const dialogVisible = ref(false)
// 对话框标题（区分新增/编辑）
const dialogTitle = ref('新增部门')
// 表单引用（用于校验）
const formRef = ref(null)

// ===================== 搜索/分页相关 =====================
// 搜索表单数据
const searchForm = reactive({
  searchKey: ''
})

// 分页参数
const pagination = reactive({
  pageNum: 1,         // 当前页码
  pageSize: 10,       // 每页条数
  total: 0            // 总记录数
})

// ===================== 数据存储 =====================
// 部门表格数据
const tableData = ref([])
// 所有员工列表（用于下拉选择）
const allEmployees = ref([])

// ===================== 表单相关 =====================
// 新增/编辑表单数据
const formData = reactive({
  id: null,           // 部门ID
  depName: '',        // 部门名称
  managerId: null,    // 负责人ID
  employeeIds: []     // 部门员工ID列表（多选）
})

// 表单校验规则
const formRules = {
  depName: [
    { required: true, message: '请输入部门名称', trigger: 'blur' }
  ]
}

// ===================== 辅助函数 =====================
/**
 * 填充表单数据（编辑部门时使用）
 * @param {Object} row - 选中的部门行数据
 */
const populateFormData = (row) => {
  // ========== 核心修改：穷尽所有可能的负责人ID来源 ==========
  let managerId = null;
  // 场景1：后端返回了 managerId 字段（数字/字符串）
  if (row.managerId) {
    managerId = Number(row.managerId); // 强制转数字，和下拉选项value类型一致
  }
  // 场景2：后端返回了 manager 对象（包含id/_id）
  else if (row.manager) {
    managerId = Number(row.manager.id || row.manager._id || null);
  }
  // 场景3：仅返回 managerName（兜底：根据姓名找ID）
  else if (row.managerName && allEmployees.value.length > 0) {
    const targetEmp = allEmployees.value.find(emp => emp.empName === row.managerName);
    managerId = targetEmp ? targetEmp.id : null;
  }

  // 员工ID处理（保持不变）
  const employeeIds = (row.empList || [])
    .map(emp => {
      const empId = emp._id || emp.id;
      return empId ? Number(empId) : null;
    })
    .filter(id => id !== null && id !== undefined);

  // 赋值到表单（managerId 现在一定是数字，和下拉选项value匹配）
  Object.assign(formData, {
    id: row.id,
    depName: row.depName,
    managerId: managerId, // 关键：这个值必须在 allEmployees 的 id 列表中
    employeeIds: employeeIds
  });

  // 调试日志（可选，方便排查）
  console.log('最终赋值的负责人ID：', managerId);
  console.log('员工列表中的ID：', allEmployees.value.map(emp => emp.id));
};

// ===================== 数据加载函数 =====================
/**
 * 加载所有员工列表（用于下拉选择）
 * 处理逻辑：兼容后端返回的_id字段，转为前端统一的数字ID
 */
const loadEmployees = async () => {
  try {
    const rawData = await getAllEmployees()
    // 提取员工数组并处理字段：确保id为数字类型，过滤无效数据
    const empList = rawData.employees || []
    allEmployees.value = empList
      .filter(emp => emp && (emp._id || emp.id) && emp.empName) // 兼容_id/id字段
      .map(emp => ({
        ...emp,
        id: Number(emp._id || emp.id), // 统一转为数字ID
        empName: emp.empName
      }))
  } catch (error) {
    ElMessage.warning('员工列表加载失败，下拉框暂时不可用：' + (error.message || '未知错误'))
    allEmployees.value = []
  }
}

/**
 * 加载部门列表数据（支持搜索、分页）
 */
const loadData = async () => {
  loading.value = true
  try {
    // 构造请求参数：空搜索关键词不传递，避免后端处理问题
    const params = {
      searchKey: searchForm.searchKey || undefined,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    
    // 调用接口获取数据
    const response = await getDepartments(params)
    // 赋值到表格和分页
    tableData.value = response.departments || []
    pagination.total = response.total || 0
    pagination.pageNum = response.pageNum || 1
    pagination.pageSize = response.pageSize || 10
    
  } catch (error) {
    ElMessage.error('加载数据失败：' + (error.response?.data?.message || error.message))
  } finally {
    loading.value = false
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
 * 处理新增部门操作（重置表单并打开对话框）
 */
const handleAdd = () => {
  dialogTitle.value = '新增部门'
  // 重置表单数据
  Object.assign(formData, {
    id: null,
    depName: '',
    managerId: null,
    employeeIds: []
  })
  dialogVisible.value = true
}

/**
 * 处理编辑部门操作（加载员工数据→填充表单→打开对话框）
 * @param {Object} row - 选中的部门行数据
 */
const handleEdit = async (row) => {
  dialogTitle.value = '编辑部门';
  // 1. 先加载员工列表（确保下拉选项已存在）
  await loadEmployees(); 
  // 2. 等待100ms（兜底：确保DOM渲染完成）
  await new Promise(resolve => setTimeout(resolve, 100));
  // 3. 再填充表单（此时下拉选项已渲染，ID匹配就会显示姓名）
  populateFormData(row);
  dialogVisible.value = true;
};

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
    // 构造提交参数：统一ID类型为数字
    const data = {
      id: formData.id ? Number(formData.id) : null,
      depName: formData.depName,
      managerId: formData.managerId,
      empIds: formData.employeeIds
    }
    
    // 调用保存接口
    const response = await saveDepartment(data)
    if (response.success) {
      ElMessage.success(response.message)
      dialogVisible.value = false
      loadData() // 提交成功后刷新列表
    } else {
      ElMessage.error(response.message)
    }
  } catch (error) {
    ElMessage.error('提交失败：' + (error.response?.data?.message || error.message))
  } finally {
    submitting.value = false
  }
}

/**
 * 处理删除部门操作（带确认提示）
 * @param {Object} row - 选中的部门行数据
 */
const handleDelete = async (row) => {
  try {
    // 确认删除
    await ElMessageBox.confirm(
      `确定要删除部门「${row.depName}」吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 调用删除接口
    const response = await deleteDepartment(row.id)
    if (response.success) {
      ElMessage.success(response.message)
      loadData() // 删除成功后刷新列表
    } else {
      ElMessage.error(response.message)
    }
  } catch (error) {
    // 排除取消操作的错误
    if (error !== 'cancel') {
      ElMessage.error('删除失败：' + (error.response?.data?.message || error.message))
    }
  }
}

// ===================== 生命周期 =====================
/**
 * 页面挂载完成后初始化数据
 */
onMounted(() => {
  loadData()       // 加载部门列表
  loadEmployees()  // 加载员工列表
})
</script>

<style scoped>
.departments-container {
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
</style>
