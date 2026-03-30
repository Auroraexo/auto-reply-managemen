<template>
  <el-card>
    <template #header>
      <span>消息记录</span>
    </template>

    <!-- 搜索栏 -->
    <el-form :inline="true" :model="searchForm" style="margin-bottom: 16px">
      <el-form-item label="OpenID">
        <el-input v-model="searchForm.openId" placeholder="请输入 OpenID" clearable style="width: 200px" />
      </el-form-item>
      <el-form-item label="消息类型">
        <el-select v-model="searchForm.msgType" placeholder="全部" clearable style="width: 120px">
          <el-option label="文本" value="text" />
          <el-option label="图片" value="image" />
          <el-option label="语音" value="voice" />
          <el-option label="事件" value="event" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
        <el-button type="success" @click="sendDialogVisible = true">发送消息</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="tableData" v-loading="loading" border stripe>
      <el-table-column prop="openId" label="用户 OpenID" show-overflow-tooltip />
      <el-table-column prop="msgType" label="消息类型" width="100">
        <template #default="{ row }">{{ getMsgTypeText(row.msgType) }}</template>
      </el-table-column>
      <el-table-column prop="content" label="消息内容" show-overflow-tooltip />
      <el-table-column prop="replyContent" label="回复内容" show-overflow-tooltip />
      <el-table-column prop="replyRuleId" label="匹配规则" width="100">
        <template #default="{ row }">
          <span v-if="row.replyRuleId">已匹配</span>
          <span v-else style="color:#ccc">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="replyStatus" label="回复状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.replyStatus === 1 ? 'success' : 'danger'">
            {{ row.replyStatus === 1 ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180">
        <template #default="{ row }">
          {{ row.createTime?.replace('T', ' ') }}
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="pagination.current"
      v-model:page-size="pagination.size"
      :total="pagination.total"
      :page-sizes="[20, 50, 100]"
      layout="total, sizes, prev, pager, next"
      @size-change="loadData"
      @current-change="loadData"
      style="margin-top: 16px; justify-content: flex-end"
    />
  </el-card>

  <!-- 发送消息弹窗 -->
  <el-dialog v-model="sendDialogVisible" title="主动发送消息" width="440px">
    <el-form :model="sendForm" label-width="80px">
      <el-form-item label="OpenID">
        <el-input v-model="sendForm.openId" placeholder="用户 openId" />
      </el-form-item>
      <el-form-item label="消息内容">
        <el-input v-model="sendForm.content" type="textarea" :rows="3" placeholder="请输入消息内容" />
      </el-form-item>
    </el-form>
    <div style="color:#999;font-size:12px;padding: 0 20px 10px">
      演示模式：消息不会真实发送，需配置真实公众号后生效
    </div>
    <template #footer>
      <el-button @click="sendDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="sending" @click="handleSend">发送</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const tableData = ref<any[]>([])
const searchForm = reactive({ openId: '', msgType: '' })
const pagination = reactive({ current: 1, size: 20, total: 0 })

const sendDialogVisible = ref(false)
const sending = ref(false)
const sendForm = reactive({ openId: '', content: '' })

const loadData = async () => {
  loading.value = true
  try {
    const res = await request({
      url: '/admin/messages/page',
      method: 'get',
      params: {
        current: pagination.current,
        size: pagination.size,
        ...searchForm
      }
    }) as any
    tableData.value = res.records || []
    pagination.total = res.total || 0
  } catch {
    ElMessage.error('加载消息记录失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pagination.current = 1; loadData() }
const handleReset = () => { searchForm.openId = ''; searchForm.msgType = ''; handleSearch() }

const handleSend = async () => {
  if (!sendForm.openId || !sendForm.content) {
    ElMessage.warning('请填写 OpenID 和消息内容')
    return
  }
  sending.value = true
  try {
    await request({ url: '/admin/messages/send', method: 'post', data: sendForm })
    ElMessage.success('发送成功（演示模式，消息未真实送达）')
    sendDialogVisible.value = false
    sendForm.openId = ''
    sendForm.content = ''
  } catch {
    // 错误已由拦截器处理
  } finally {
    sending.value = false
  }
}

const getMsgTypeText = (type: string) => {
  const map: Record<string, string> = { text: '文本', image: '图片', voice: '语音', video: '视频', location: '位置', link: '链接', event: '事件' }
  return map[type] || type
}

onMounted(loadData)
</script>
