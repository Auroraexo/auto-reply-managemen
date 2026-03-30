<template>
  <div class="backup-container">
    <el-card style="margin-bottom: 20px">
      <template #header><span>数据备份</span></template>
      <div style="display: flex; justify-content: space-between; align-items: center">
        <el-alert title="需要安装 mysqldump 才能执行备份操作" type="info" :closable="false" style="width: 500px" />
        <el-button type="primary" size="large" :loading="backingUp" @click="handleBackup">立即备份</el-button>
      </div>
    </el-card>

    <el-card>
      <template #header><span>备份记录</span></template>
      <el-table :data="backupList" v-loading="loading" border stripe>
        <el-table-column prop="filename" label="文件名" min-width="220" show-overflow-tooltip />
        <el-table-column prop="size" label="文件大小" width="120">
          <template #default="{ row }">{{ formatSize(row.size) }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="备份时间" width="180">
          <template #default="{ row }">{{ new Date(row.createTime).toLocaleString() }}</template>
        </el-table-column>
        <el-table-column prop="path" label="存储路径" min-width="260" show-overflow-tooltip />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleDownload(row)">下载</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const backingUp = ref(false)
const backupList = ref<any[]>([])

const loadBackups = async () => {
  loading.value = true
  try {
    const res = await request({ url: '/admin/backup/list', method: 'get' }) as any[]
    backupList.value = res || []
  } catch {
    ElMessage.error('加载备份列表失败')
  } finally {
    loading.value = false
  }
}

const handleBackup = async () => {
  await ElMessageBox.confirm('确定要立即备份数据库吗？', '提示', { type: 'warning' })
  backingUp.value = true
  try {
    await request({ url: '/admin/backup/manual', method: 'post' })
    ElMessage.success('备份成功')
    loadBackups()
  } catch {
    ElMessage.error('备份失败')
  } finally {
    backingUp.value = false
  }
}

const handleDownload = async (row: any) => {
  try {
    const res = await request({
      url: `/admin/backup/download/${row.filename}`,
      method: 'get',
      responseType: 'blob'
    }) as Blob
    const url = URL.createObjectURL(res)
    const a = document.createElement('a')
    a.href = url
    a.download = row.filename
    a.click()
    URL.revokeObjectURL(url)
  } catch {
    ElMessage.error('下载失败')
  }
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确定删除备份文件 "${row.filename}" 吗？`, '警告', { type: 'warning' })
    .then(async () => {
      await request({ url: `/admin/backup/${row.filename}`, method: 'delete' })
      ElMessage.success('删除成功')
      loadBackups()
    }).catch(() => {})
}

const formatSize = (bytes: number) => {
  if (!bytes) return '0 B'
  const k = 1024, sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return (bytes / Math.pow(k, i)).toFixed(2) + ' ' + sizes[i]
}

onMounted(loadBackups)
</script>

<style scoped>
.backup-container { padding: 20px; }
</style>
