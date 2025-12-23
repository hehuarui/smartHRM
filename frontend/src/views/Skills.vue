<template>
  <div class="skills-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">
            <el-icon><Trophy /></el-icon>
            技能管理
          </span>
          <div class="header-actions">
            <el-input
              v-model="searchForm.name"
              placeholder="输入技能名称搜索"
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
              新增技能
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
        <el-table-column prop="_id" label="技能ID" width="120" />
        <el-table-column prop="skillName" label="技能名称" width="200" />
        <el-table-column prop="skillKind" label="技能分类" width="150" />
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
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="技能ID" v-if="formData._id">
          <el-input v-model="formData._id" disabled />
        </el-form-item>
        
        <el-form-item label="技能名称" prop="skillName">
          <el-input v-model="formData.skillName" placeholder="例如：Java, Python, 沟通技巧" />
        </el-form-item>

        <el-form-item label="技能分类">
          <el-input v-model="formData.skillKind" placeholder="例如：后端开发、前端开发、管理" />
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
import { Trophy, Search, Plus, Edit, Delete, Refresh } from '@element-plus/icons-vue'
import {
  getSkills,
  searchSkills,
  addSkill,
  updateSkill,
  deleteSkill
} from '@/api/skill'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增技能')
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
  skillName: '',
  skillKind: ''
})

const formRules = {
  skillName: [
    { required: true, message: '请输入技能名称', trigger: 'blur' }
  ]
}

const loadData = async () => {
  loading.value = true
  try {
    if (searchForm.name) {
      // 搜索模式
      const result = await searchSkills(searchForm.name)
      tableData.value = result || []
      pagination.total = tableData.value.length
    } else {
      // 分页模式
      const result = await getSkills({
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
  pagination.page = page - 1 // Element Plus页码从1开始，后端从0开始
  loadData()
}

const handleAdd = () => {
  dialogTitle.value = '新增技能'
  Object.assign(formData, {
    _id: null,
    skillName: '',
    skillKind: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑技能'
  Object.assign(formData, {
    _id: row._id,
    skillName: row.skillName,
    skillKind: row.skillKind || ''
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      const data = {
        _id: formData._id,
        skillName: formData.skillName.trim(),
        skillKind: formData.skillKind.trim() || null
      }
      
      if (formData._id) {
        await updateSkill(data)
      } else {
        await addSkill(data)
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
      `确定要删除技能「${row.skillName}」吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteSkill(row._id)
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
.skills-container {
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
</style>

