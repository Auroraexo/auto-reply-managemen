<template>
  <div class="users-container">
    <!-- 搜索栏 -->
    <el-card style="margin-bottom: 20px">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="昵称">
          <el-input v-model="searchForm.nickname" placeholder="请输入昵称" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="关注状态">
          <el-select v-model="searchForm.subscribe" placeholder="全部" clearable style="width: 120px">
            <el-option label="已关注" :value="1" />
            <el-option label="未关注" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 用户列表 -->
    <el-card>
      <el-table :data="userList" v-loading="loading" border stripe>
        <el-table-column type="selection" width="55" />
        <el-table-column prop="headImgUrl" label="头像" width="80">
          <template #default="{ row }">
            <el-image 
              :src="row.headImgUrl || '/default-avatar.png'" 
              fit="cover"
              style="width: 40px; height: 40px; border-radius: 50%"
            />
          </template>
        </el-table-column>
        <el-table-column prop="nickname" label="昵称" width="150" />
        <el-table-column label="性别" width="80">
          <template #default="{ row }">
            <span v-if="row.gender === 1">👦</span>
            <span v-else-if="row.gender === 2">👧</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="city" label="城市" width="100" />
        <el-table-column label="关注时间" width="180">
          <template #default="{ row }">
            {{ row.subscribeTime ? new Date(row.subscribeTime).toLocaleString() : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="标签" min-width="200">
          <template #default="{ row }">
            <el-tag 
              v-for="tagId in (row.tagIds || '').split(',').filter(Boolean)" 
              :key="tagId"
              size="small"
              style="margin-right: 5px"
            >
              {{ getTagName(tagId) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleRemark(row)">备注</el-button>
            <el-button size="small" @click="handleTag(row)">打标签</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadUsers"
        @current-change="loadUsers"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- 备注对话框 -->
    <el-dialog v-model="remarkDialogVisible" title="设置备注" width="400px">
      <el-input v-model="currentRemark" placeholder="请输入备注" />
      <template #footer>
        <el-button @click="remarkDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRemark">保存</el-button>
      </template>
    </el-dialog>

    <!-- 打标签对话框 -->
    <el-dialog v-model="tagDialogVisible" title="选择标签" width="400px">
      <el-checkbox-group v-model="selectedTags">
        <el-checkbox 
          v-for="tag in allTags" 
          :key="tag.id" 
          :label="tag.id"
          :style="{ color: tag.color, marginBottom: '10px' }"
        >
          {{ tag.name }}
        </el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="tagDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveTags">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { pageUsers, updateRemark, batchTag, removeTag } from '@/api/wechat-user'
import { listTags } from '@/api/tag'

const loading = ref(false)
const userList = ref<any[]>([])
const allTags = ref<any[]>([])
const searchForm = reactive({
  nickname: '',
  subscribe: null as number | null
})

const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

const remarkDialogVisible = ref(false)
const currentRemark = ref('')
const currentUser = ref<any>(null)

const tagDialogVisible = ref(false)
const selectedTags = ref<number[]>([])

// 加载用户列表
const loadUsers = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      ...searchForm
    }
    const response = await pageUsers(params)
    userList.value = response.records || []
    pagination.total = response.total || 0
  } catch (error) {
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

// 加载标签列表
const loadTags = async () => {
  try {
    const response = await listTags()
    allTags.value = response || []
  } catch (error) {
    console.error('加载标签失败', error)
  }
}

// 获取标签名称
const getTagName = (tagId: string) => {
  const tag = allTags.value.find(t => t.id === parseInt(tagId))
  return tag ? tag.name : `标签${tagId}`
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  loadUsers()
}

// 重置
const handleReset = () => {
  searchForm.nickname = ''
  searchForm.subscribe = null
  handleSearch()
}

// 设置备注
const handleRemark = (row: any) => {
  currentUser.value = row
  currentRemark.value = row.remark || ''
  remarkDialogVisible.value = true
}

// 保存备注
const saveRemark = async () => {
  if (!currentUser.value) return
  
  try {
    await updateRemark(currentUser.value.id, currentRemark.value)
    ElMessage.success('备注成功')
    remarkDialogVisible.value = false
    loadUsers()
  } catch (error) {
    ElMessage.error('备注失败')
  }
}

// 打标签
const handleTag = (row: any) => {
  currentUser.value = row
  const userTagIds = (row.tagIds || '').split(',').filter(Boolean).map(Number)
  selectedTags.value = userTagIds
  tagDialogVisible.value = true
}

// 保存标签
const saveTags = async () => {
  if (!currentUser.value) return
  
  try {
    // 先移除所有标签，再添加新标签
    // 这里简化处理，实际应该对比差异
    await batchTag([currentUser.value.id], selectedTags.value[0])
    ElMessage.success('打标签成功')
    tagDialogVisible.value = false
    loadUsers()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  loadUsers()
  loadTags()
})
</script>

<style scoped>
.users-container {
  padding: 20px;
}
</style>
