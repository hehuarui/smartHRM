<template>
  <div class="skill-match-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">
            <el-icon><Search /></el-icon>
            技能匹配
          </span>
          <div class="header-actions">
            <el-button @click="loadOptions">
              <el-icon><Refresh /></el-icon>
              刷新选项
            </el-button>
          </div>
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
            根据所需技能及熟练度要求，匹配符合条件的员工。格式：技能ID:最低熟练度（如：1:3,2:5）
          </template>
        </el-alert>
      </div>

      <div class="match-form">
        <el-form :model="matchForm" label-width="150px">
          <el-form-item label="所需技能">
            <el-input
              v-model="matchForm.requiredSkills"
              type="textarea"
              :rows="4"
              placeholder="格式：skillId:proficiency,skillId:proficiency（如：1:3,2:5）&#10;多个技能用逗号分隔，每个技能格式：技能ID:最低熟练度（1-5级）"
            />
            <div class="form-tip">
              示例：1:3,2:5 表示需要技能1（最低3级）和技能2（最低5级）
            </div>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="doMatch" :loading="loading" size="large">
              <el-icon><Search /></el-icon>
              开始匹配
            </el-button>
            <el-button @click="clearResults" v-if="tableData.length > 0">
              <el-icon><Delete /></el-icon>
              清空结果
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 快速选择技能 -->
      <el-card class="box-card" style="margin-top: 20px">
        <template #header>
          <span class="card-title">
            <el-icon><Trophy /></el-icon>
            快速选择技能
          </span>
        </template>
        <div class="skill-selector">
          <el-checkbox-group v-model="selectedSkills" @change="updateSkillsFromSelector">
            <el-checkbox
              v-for="skill in allSkills"
              :key="skill._id"
              :label="skill._id"
              style="margin-right: 20px; margin-bottom: 10px"
            >
              {{ skill.skillName }}
            </el-checkbox>
          </el-checkbox-group>
        </div>
        <div style="margin-top: 15px">
          <el-input-number
            v-model="defaultProficiency"
            :min="1"
            :max="5"
            label="默认熟练度"
            style="width: 150px"
          />
          <el-button type="primary" @click="applyDefaultProficiency" style="margin-left: 10px">
            应用默认熟练度
          </el-button>
        </div>
      </el-card>

      <!-- 匹配结果 -->
      <el-card class="box-card" style="margin-top: 20px" v-if="tableData.length > 0">
        <template #header>
          <div class="card-header">
            <span class="card-title">
              <el-icon><User /></el-icon>
              匹配结果（{{ tableData.length }}人）
            </span>
          </div>
        </template>

        <el-table
          :data="tableData"
          stripe
          style="width: 100%"
          border
        >
          <el-table-column prop="_id" label="员工ID" width="100" />
          <el-table-column prop="empName" label="员工姓名" width="120" />
          <el-table-column label="所属部门" width="150">
            <template #default="{ row }">
              <el-tag>{{ row.deptName || '未分配' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="技能列表" min-width="300">
            <template #default="{ row }">
              <div class="skills-display">
                <el-tag
                  v-for="(skill, index) in row.skillList"
                  :key="index"
                  :type="getSkillTagType(skill)"
                  style="margin-right: 5px; margin-bottom: 5px"
                >
                  技能{{ skill.skillId }}（{{ skill.proficiency }}级）
                </el-tag>
                <span v-if="!row.skillList || row.skillList.length === 0" class="text-muted">无</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="参与项目" min-width="150">
            <template #default="{ row }">
              <el-tag
                v-for="(proj, index) in row.projects"
                :key="index"
                type="success"
                style="margin-right: 5px; margin-bottom: 5px"
              >
                项目{{ proj.projId }}
              </el-tag>
              <span v-if="!row.projects || row.projects.length === 0" class="text-muted">无</span>
            </template>
          </el-table-column>
          <el-table-column prop="joinDate" label="加入时间" width="120">
            <template #default="{ row }">
              {{ formatDate(row.joinDate) }}
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 空状态 -->
      <el-empty
        v-if="!loading && tableData.length === 0 && hasSearched"
        description="未找到符合条件的员工"
        :image-size="200"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Delete, Trophy, User } from '@element-plus/icons-vue'
import { matchBySkills, getAllSkills } from '@/api/skillMatch'

const loading = ref(false)
const hasSearched = ref(false)

const matchForm = reactive({
  requiredSkills: ''
})

const tableData = ref([])
const allSkills = ref([])
const selectedSkills = ref([])
const defaultProficiency = ref(3)

const doMatch = async () => {
  if (!matchForm.requiredSkills.trim()) {
    ElMessage.warning('请输入所需技能')
    return
  }

  loading.value = true
  hasSearched.value = true
  
  try {
    const result = await matchBySkills(matchForm.requiredSkills)
    tableData.value = result || []
    
    if (tableData.value.length === 0) {
      ElMessage.info('未找到符合条件的员工')
    } else {
      ElMessage.success(`找到 ${tableData.value.length} 名符合条件的员工`)
    }
  } catch (error) {
    ElMessage.error('匹配失败：' + error.message)
  } finally {
    loading.value = false
  }
}

const clearResults = () => {
  tableData.value = []
  matchForm.requiredSkills = ''
  selectedSkills.value = []
  hasSearched.value = false
}

const loadOptions = async () => {
  try {
    allSkills.value = await getAllSkills() || []
    ElMessage.success('选项刷新成功')
  } catch (error) {
    ElMessage.error('加载选项失败：' + error.message)
  }
}

const updateSkillsFromSelector = () => {
  if (selectedSkills.value.length === 0) {
    matchForm.requiredSkills = ''
    return
  }

  const skillsStr = selectedSkills.value
    .map(skillId => {
      // 查找是否已有该技能的熟练度设置
      const existing = matchForm.requiredSkills
        .split(',')
        .find(s => s.trim().startsWith(skillId + ':'))
      
      if (existing) {
        return existing.trim()
      } else {
        return `${skillId}:${defaultProficiency.value}`
      }
    })
    .join(',')

  matchForm.requiredSkills = skillsStr
}

const applyDefaultProficiency = () => {
  if (selectedSkills.value.length === 0) {
    ElMessage.warning('请先选择技能')
    return
  }

  const skillsArray = matchForm.requiredSkills
    ? matchForm.requiredSkills.split(',').map(s => s.trim())
    : []

  // 更新已选技能的熟练度
  selectedSkills.value.forEach(skillId => {
    const index = skillsArray.findIndex(s => s.startsWith(skillId + ':'))
    if (index !== -1) {
      skillsArray[index] = `${skillId}:${defaultProficiency.value}`
    } else {
      skillsArray.push(`${skillId}:${defaultProficiency.value}`)
    }
  })

  matchForm.requiredSkills = skillsArray.join(',')
  ElMessage.success('已应用默认熟练度')
}

const getSkillTagType = (skill) => {
  // 根据熟练度显示不同颜色
  if (skill.proficiency >= 4) return 'success'
  if (skill.proficiency >= 3) return ''
  return 'warning'
}

const formatDate = (date) => {
  if (!date) return '未设置'
  const d = new Date(date)
  return d.toLocaleDateString('zh-CN')
}

onMounted(() => {
  loadOptions()
})
</script>

<style scoped>
.skill-match-container {
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

.match-tips {
  margin-bottom: 20px;
}

.match-form {
  padding: 20px 0;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.skill-selector {
  max-height: 200px;
  overflow-y: auto;
  padding: 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.skills-display {
  display: flex;
  flex-wrap: wrap;
}

.text-muted {
  color: #909399;
  font-size: 12px;
}
</style>


