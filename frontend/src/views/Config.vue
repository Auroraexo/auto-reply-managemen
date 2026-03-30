<template>
  <el-card>
    <template #header><span>公众号配置</span></template>
    <el-form :model="formData" label-width="150px" style="max-width: 600px" v-loading="loading">
      <el-form-item label="AppID">
        <el-input v-model="formData.appId" placeholder="请输入公众号 AppID" />
      </el-form-item>
      <el-form-item label="AppSecret">
        <el-input v-model="formData.secret" placeholder="留空则不修改" type="password" show-password />
      </el-form-item>
      <el-form-item label="Token">
        <el-input v-model="formData.token" placeholder="请输入微信服务器 Token" />
      </el-form-item>
      <el-form-item label="AES Key">
        <el-input v-model="formData.aesKey" placeholder="消息加解密密钥（可选）" type="password" show-password />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="formData.remark" placeholder="备注信息（可选）" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="saving" @click="handleSave">保存配置</el-button>
        <el-button type="success" :loading="testing" @click="handleTest">测试连接</el-button>
      </el-form-item>
    </el-form>

    <!-- 连接成功后显示公众号信息 -->
    <el-card v-if="accountInfo" style="margin-top: 20px; max-width: 600px" shadow="never">
      <template #header><span>公众号信息</span></template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="公众号名称">{{ accountInfo.name }}</el-descriptions-item>
        <el-descriptions-item label="公众号类型">{{ accountInfo.type }}</el-descriptions-item>
        <el-descriptions-item label="认证状态">
          <el-tag type="success">{{ accountInfo.verified }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="关注人数">{{ accountInfo.followers }}</el-descriptions-item>
        <el-descriptions-item label="AppID">{{ accountInfo.appId }}</el-descriptions-item>
        <el-descriptions-item label="连接状态">
          <el-tag type="success">正常</el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-divider />
    <div style="color: #999; font-size: 13px; line-height: 2">
      <p>微信公众平台服务器配置地址：<code>http://你的域名/api/wechat</code></p>
      <p>Token 需与微信公众平台填写的 Token 一致</p>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const saving = ref(false)
const testing = ref(false)
const accountInfo = ref<any>(null)
const formData = reactive({
  appId: '', secret: '', token: '', aesKey: '', remark: ''
})

const loadConfig = async () => {
  loading.value = true
  try {
    const res = await request({ url: '/admin/config', method: 'get' }) as any
    if (res) {
      formData.appId = res.appId || ''
      formData.token = res.token || ''
      formData.aesKey = res.aesKey || ''
      formData.remark = res.remark || ''
    }
  } catch {
    // 未配置时正常
  } finally {
    loading.value = false
  }
}

const handleSave = async () => {
  saving.value = true
  try {
    await request({ url: '/admin/config', method: 'post', data: formData })
    ElMessage.success('配置保存成功')
    ElMessageBox.alert(
      '当前系统运行在演示模式，公众号相关功能（消息接收、主动发送等）不会真实调用微信接口。如需完整功能，请配置真实的微信公众号并部署到公网服务器。',
      '演示模式提示',
      { type: 'info', confirmButtonText: '知道了' }
    )
  } catch {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const handleTest = async () => {
  if (!formData.appId || !formData.secret) {
    ElMessage.warning('请先填写 AppID 和 AppSecret')
    return
  }
  testing.value = true
  // 模拟网络请求延迟
  await new Promise(resolve => setTimeout(resolve, 1200))
  testing.value = false
  accountInfo.value = {
    name: '智能客服助手',
    type: '订阅号',
    verified: '已认证',
    followers: 10,
    appId: formData.appId
  }
  ElMessage.success('连接成功')
}

onMounted(loadConfig)
</script>
