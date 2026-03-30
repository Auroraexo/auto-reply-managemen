<template>
  <div class="rules-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>回复规则列表</span>
          <el-button type="primary" @click="handleAdd">新增规则</el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="规则名称">
          <el-input v-model="searchForm.ruleName" placeholder="请输入规则名称" clearable />
        </el-form-item>
        <el-form-item label="规则类型">
          <el-select v-model="searchForm.ruleType" placeholder="请选择规则类型" clearable style="width: 200px;">
            <el-option label="关键词" value="KEYWORD" />
            <el-option label="关注" value="SUBSCRIBE" />
            <el-option label="默认" value="DEFAULT" />
            <el-option label="消息类型" value="MESSAGE_TYPE" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.isEnabled" placeholder="请选择状态" clearable style="width: 200px;">
            <el-option label="启用" :value="true" />
            <el-option label="禁用" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadRules">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table :data="tableData" border stripe style="width: 100%">
        <el-table-column prop="ruleName" label="规则名称" />
        <el-table-column prop="ruleType" label="规则类型">
          <template #default="{ row }">
            {{ getRuleTypeText(row.ruleType) }}
          </template>
        </el-table-column>
        <el-table-column prop="matchMode" label="匹配模式">
          <template #default="{ row }">
            {{ getMatchModeText(row.matchMode) }}
          </template>
        </el-table-column>
        <el-table-column prop="matchContent" label="匹配内容" show-overflow-tooltip />
        <el-table-column prop="replyType" label="回复类型">
          <template #default="{ row }">
            {{ getReplyTypeText(row.replyType) }}
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="80" />
        <el-table-column prop="hitCount" label="命中次数" width="100" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.isEnabled ? 'success' : 'danger'">
              {{ row.isEnabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button 
              size="small" 
              :type="row.isEnabled ? 'warning' : 'success'"
              @click="handleToggleEnable(row)"
            >
              {{ row.isEnabled ? '禁用' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadRules"
        @current-change="loadRules"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
    >
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="规则名称" prop="ruleName">
          <el-input v-model="formData.ruleName" placeholder="请输入规则名称" />
        </el-form-item>
        <el-form-item label="规则类型" prop="ruleType">
          <el-select v-model="formData.ruleType" placeholder="请选择规则类型" @change="onRuleTypeChange">
            <el-option label="关键词" value="KEYWORD" />
            <el-option label="关注" value="SUBSCRIBE" />
            <el-option label="默认" value="DEFAULT" />
            <el-option label="消息类型" value="MESSAGE_TYPE" />
          </el-select>
        </el-form-item>
        <el-form-item label="匹配模式" prop="matchMode" v-if="formData.ruleType === 'KEYWORD'">
          <el-select v-model="formData.matchMode" placeholder="请选择匹配模式">
            <el-option label="完全匹配" value="EXACT" />
            <el-option label="包含" value="CONTAIN" />
            <el-option label="正则" value="REGEX" />
          </el-select>
        </el-form-item>
        <el-form-item label="匹配内容" prop="matchContent" v-if="formData.ruleType === 'KEYWORD'">
          <el-input v-model="formData.matchContent" placeholder="请输入关键词或正则表达式" />
        </el-form-item>
        <el-form-item label="消息类型" prop="messageType" v-if="formData.ruleType === 'MESSAGE_TYPE'">
          <el-select v-model="formData.messageType" placeholder="请选择消息类型">
            <el-option label="文本（演示）" value="text" />
            <el-option label="图片" value="image" />
            <el-option label="语音" value="voice" />
            <el-option label="视频" value="video" />
            <el-option label="位置" value="location" />
            <el-option label="链接" value="link" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-input-number v-model="formData.priority" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="回复类型" prop="replyType">
          <el-select v-model="formData.replyType" placeholder="请选择回复类型">
            <el-option label="文本（演示）" value="TEXT" />
            <el-option label="图片" value="IMAGE" />
            <el-option label="语音" value="VOICE" />
            <el-option label="视频" value="VIDEO" />
            <el-option label="音乐" value="MUSIC" />
            <el-option label="图文" value="NEWS" />
          </el-select>
        </el-form-item>
        <el-form-item label="回复内容" prop="replyContent" v-if="formData.replyType === 'TEXT'">
          <el-input 
            v-model="formData.replyContent" 
            type="textarea" 
            :rows="4"
            placeholder="请输入回复内容"
          />
        </el-form-item>
        <el-form-item label="是否启用" prop="isEnabled">
          <el-switch v-model="formData.isEnabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getReplyRules, createReplyRule, updateReplyRule, deleteReplyRule, setRuleEnabled, ReplyRule } from '@/api/rules'

const tableData = ref<any[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增规则')
const formRef = ref()

const searchForm = reactive({
  ruleName: '',
  ruleType: '',
  isEnabled: null as boolean | null
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const formData = reactive<ReplyRule>({
  ruleName: '',
  ruleType: 'KEYWORD',
  priority: 0,
  matchMode: 'EXACT',
  matchContent: '',
  replyType: 'TEXT',
  replyContent: '',
  isEnabled: true
})

const formRules = {
  ruleName: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  ruleType: [{ required: true, message: '请选择规则类型', trigger: 'change' }],
  replyType: [{ required: true, message: '请选择回复类型', trigger: 'change' }]
}

const loadRules = async () => {
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      ...searchForm
    }
    const res: any = await getReplyRules(params)
    tableData.value = res.records || []
    pagination.total = res.total || 0
  } catch (error) {
    console.error('加载规则失败', error)
  }
}

const resetSearch = () => {
  searchForm.ruleName = ''
  searchForm.ruleType = ''
  searchForm.isEnabled = null
  pagination.page = 1
  loadRules()
}

const handleAdd = () => {
  dialogTitle.value = '新增规则'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row: ReplyRule) => {
  dialogTitle.value = '编辑规则'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleDelete = (row: ReplyRule) => {
  ElMessageBox.confirm('确定要删除该规则吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteReplyRule(row.id!)
      ElMessage.success('删除成功')
      loadRules()
    } catch (error) {
      console.error('删除失败', error)
    }
  })
}

const handleToggleEnable = async (row: ReplyRule) => {
  try {
    await setRuleEnabled(row.id!, !row.isEnabled)
    ElMessage.success('操作成功')
    loadRules()
  } catch (error) {
    console.error('操作失败', error)
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    if (formData.id) {
      await updateReplyRule(formData.id, formData)
      ElMessage.success('更新成功')
    } else {
      await createReplyRule(formData)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadRules()
  } catch (error) {
    console.error('提交失败', error)
  }
}

const resetForm = () => {
  Object.assign(formData, {
    id: undefined,
    ruleName: '',
    ruleType: 'KEYWORD',
    priority: 0,
    matchMode: 'EXACT',
    matchContent: '',
    replyType: 'TEXT',
    replyContent: '',
    isEnabled: true
  })
  formRef.value?.clearValidate()
}

const onRuleTypeChange = () => {
  formData.matchContent = ''
  formData.messageType = ''
}

const getRuleTypeText = (type: string) => {
  const map: any = {
    KEYWORD: '关键词',
    SUBSCRIBE: '关注',
    DEFAULT: '默认',
    MESSAGE_TYPE: '消息类型'
  }
  return map[type] || type
}

const getMatchModeText = (mode: string) => {
  const map: any = {
    EXACT: '完全匹配',
    CONTAIN: '包含',
    REGEX: '正则'
  }
  return map[mode] || mode
}

const getReplyTypeText = (type: string) => {
  const map: any = {
    TEXT: '文本',
    IMAGE: '图片',
    VOICE: '语音',
    VIDEO: '视频',
    MUSIC: '音乐',
    NEWS: '图文'
  }
  return map[type] || type
}

onMounted(() => {
  loadRules()
})
</script>

<style scoped>
.rules-container {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}
</style>
