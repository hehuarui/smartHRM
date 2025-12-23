<template>
  <div class="trainings-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">
            <el-icon><Reading /></el-icon>
            培训管理
          </span>
          <div class="header-actions">
            <el-input
              v-model="searchForm.name"
              placeholder="输入课程名称搜索"
              style="width: 250px; margin-right: 10px"
              clearable
              @clear="handleSearch"
              @keyup.enter="handleSearch"
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
              新增培训
            </el-button>
            <el-button @click="loadData">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        style="width: 100%"
        border
      >
        <el-table-column prop="_id" label="课程ID" width="120" />
        <el-table-column prop="trainName" label="课程名称" width="200" />
        <el-table-column label="关联技能" width="150">
          <template #default="{ row }">
            <el-tag v-if="row.skillId" type="info">
              技能{{ row.skillId }}
            </el-tag>
            <span v-else class="text-muted">未设置</span>
          </template>
        </el-table-column>
        <el-table-column label="参与员工" min-width="200">
          <template #default="{ row }">
            <el-tag
              v-for="(memberId, index) in row.members"
              :key="index"
              type="success"
              style="margin-right: 5px; margin-bottom: 5px"
            >
              员工{{ memberId }}
            </el-tag>
            <span v-if="!row.members || row.members.length === 0" class="text-muted">无</span>
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

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="课程ID" v-if="formData._id">
          <el-input v-model="formData._id" disabled />
        </el-form-item>
        
        <el-form-item label="课程名称" prop="trainName">
          <el-input v-model="formData.trainName" placeholder="例如：Spring Boot 极速入门" />
        </el-form-item>

        <el-form-item label="关联技能ID" prop="skillId">
          <el-input-number
            v-model="formData.skillId"
            :min="1"
            placeholder="该课程提升哪项技能？"
            style="width: 100%"
          />
          <div class="form-tip">输入技能ID，该课程用于提升对应技能</div>
        </el-form-item>

        <el-form-item label="参与员工ID">
          <el-input
            v-model="formData.memberIdsStr"
            type="textarea"
            :rows="3"
            placeholder="多个员工ID用逗号分隔，如：1001, 1002, 1003"
          />
          <div class="form-tip">系统将自动校验员工ID是否存在</div>
        </el-form-item>
      </el-form>

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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Reading, Search, Plus, Edit, Delete, Refresh } from '@element-plus/icons-vue'
import {
  getTrainings,
  searchTrainings,
  addTraining,
  updateTraining,
  deleteTraining
} from '@/api/training'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增培训')
const formRef = ref(null)

const searchForm = reactive({
  name: ''
})

const pagination = reactive({
  page: 0,
  size: 10,
  total: 0
})

const tableData = ref([])

const formData = reactive({
  _id: null,
  trainName: '',
  skillId: null,
  memberIdsStr: ''
})

const formRules = {
  trainName: [
    { required: true, message: '请输入课程名称', trigger: 'blur' }
  ],
  skillId: [
    { required: true, message: '请选择关联技能', trigger: 'blur' }
  ]
}

const loadData = async () => {
  loading.value = true
  try {
    if (searchForm.name) {
      // 搜索模式
      const result = await searchTrainings(searchForm.name)
      tableData.value = result || []
      pagination.total = tableData.value.length
    } else {
      // 分页模式
      const result = await getTrainings({
        page: pagination.page,
        size: pagination.size
      })
      if (result && result.content) {
        tableData.value = result.content
        pagination.total = result.totalElements || 0
      } else {
        tableData.value = []
        pagination.total = 0
      }
    }
  } catch (error) {
    ElMessage.error('加载数据失败：' + error.message)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  if (searchForm.name) {
    pagination.page = 0
  }
  loadData()
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 0
  loadData()
}

const handlePageChange = (page) => {
  pagination.page = page - 1
  loadData()
}

const handleAdd = () => {
  dialogTitle.value = '新增培训'
  Object.assign(formData, {
    _id: null,
    trainName: '',
    skillId: null,
    memberIdsStr: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑培训'
  Object.assign(formData, {
    _id: row._id,
    trainName: row.trainName,
    skillId: row.skillId,
    memberIdsStr: row.members ? row.members.join(', ') : ''
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      // 解析员工ID字符串
      const members = formData.memberIdsStr
        ? formData.memberIdsStr.split(',').map(id => parseInt(id.trim())).filter(id => !isNaN(id))
        : []
      
      const data = {
        _id: formData._id,
        trainName: formData.trainName.trim(),
        skillId: formData.skillId,
        members: members.length > 0 ? members : null
      }
      
      if (formData._id) {
        await updateTraining(data)
      } else {
        await addTraining(data)
      }
      
      dialogVisible.value = false
      loadData()
    } catch (error) {
      console.error('提交失败:', error)
    } finally {
      submitting.value = false
    }
  })
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除培训课程「${row.trainName}」吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteTraining(row._id)
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.trainings-container {
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
</style>

