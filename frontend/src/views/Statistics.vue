<template>
  <div class="stats-container">
    <!-- 概览卡片 -->
    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background-color: #409EFF">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ overview.totalUsers }}</div>
              <div class="stat-label">总粉丝数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background-color: #67C23A">
              <el-icon><CirclePlus /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ overview.newUsersToday }}</div>
              <div class="stat-label">今日新增</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background-color: #E6A23C">
              <el-icon><ChatDotRound /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ overview.messagesToday }}</div>
              <div class="stat-label">今日消息</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background-color: #F56C6C">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ overview.activeRules }}</div>
              <div class="stat-label">活跃规则</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 粉丝趋势图 -->
    <el-card style="margin-bottom: 20px">
      <template #header>
        <div class="card-header">
          <span>粉丝增长趋势</span>
          <el-radio-group v-model="userTimeRange" size="small" @change="loadUserStats">
            <el-radio-button label="week">近 7 天</el-radio-button>
            <el-radio-button label="month">近 30 天</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      <div ref="userChartRef" style="height: 400px"></div>
    </el-card>

    <!-- 消息统计图 -->
    <el-card style="margin-bottom: 20px">
      <template #header>
        <div class="card-header">
          <span>消息收发统计</span>
          <el-radio-group v-model="messageTimeRange" size="small" @change="loadMessageStats">
            <el-radio-button label="week">近 7 天</el-radio-button>
            <el-radio-button label="month">近 30 天</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      <div ref="messageChartRef" style="height: 400px"></div>
    </el-card>

    <!-- 规则触发占比 -->
    <el-card>
      <template #header>
        <span>规则触发 TOP10</span>
      </template>
      <div ref="ruleChartRef" style="height: 400px"></div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import * as echarts from 'echarts'
import type { EChartsOption } from 'echarts'
import { getOverview, getUserStats, getMessageStats, getRuleStats } from '@/api/stats'

const overview = reactive({
  totalUsers: 0,
  newUsersToday: 0,
  messagesToday: 0,
  activeRules: 0
})

const userTimeRange = ref('week')
const messageTimeRange = ref('week')
const userChartRef = ref<HTMLElement>()
const messageChartRef = ref<HTMLElement>()
const ruleChartRef = ref<HTMLElement>()

let userChart: echarts.ECharts | null = null
let messageChart: echarts.ECharts | null = null
let ruleChart: echarts.ECharts | null = null

// 加载概览数据
const loadOverview = async () => {
  try {
    const data = await getOverview()
    overview.totalUsers = data.totalUsers || 0
    overview.newUsersToday = data.newUsersToday || 0
    overview.messagesToday = data.messagesToday || 0
    overview.activeRules = data.activeRules || 0
  } catch (error) {
    console.error('加载概览数据失败', error)
  }
}

// 加载粉丝统计
const loadUserStats = async () => {
  try {
    const params = { range: userTimeRange.value }
    const data = await getUserStats(params)
    
    if (!userChartRef.value) return
    if (!userChart) {
      userChart = echarts.init(userChartRef.value)
    }
    
    const option: EChartsOption = {
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' }
      },
      legend: {
        data: ['新增粉丝', '流失粉丝', '净增长']
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: data.dates || []
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name: '新增粉丝',
          type: 'bar',
          stack: 'total',
          data: data.newUsers || [],
          itemStyle: { color: '#67C23A' }
        },
        {
          name: '流失粉丝',
          type: 'bar',
          stack: 'total',
          data: data.lostUsers || [],
          itemStyle: { color: '#F56C6C' }
        },
        {
          name: '净增长',
          type: 'line',
          data: data.netGrowth || [],
          itemStyle: { color: '#409EFF' }
        }
      ]
    }
    
    userChart.setOption(option)
  } catch (error) {
    console.error('加载粉丝统计失败', error)
  }
}

// 加载消息统计
const loadMessageStats = async () => {
  try {
    const params = { range: messageTimeRange.value }
    const data = await getMessageStats(params)
    
    if (!messageChartRef.value) return
    if (!messageChart) {
      messageChart = echarts.init(messageChartRef.value)
    }
    
    const option: EChartsOption = {
      tooltip: {
        trigger: 'axis'
      },
      legend: {
        data: ['用户发送', '自动回复']
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: data.dates || []
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name: '用户发送',
          type: 'line',
          data: data.userMessages || [],
          itemStyle: { color: '#409EFF' },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(64,158,255,0.5)' },
              { offset: 1, color: 'rgba(64,158,255,0.05)' }
            ])
          }
        },
        {
          name: '自动回复',
          type: 'line',
          data: data.autoReplies || [],
          itemStyle: { color: '#67C23A' },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(103,194,58,0.5)' },
              { offset: 1, color: 'rgba(103,194,58,0.05)' }
            ])
          }
        }
      ]
    }
    
    messageChart.setOption(option)
  } catch (error) {
    console.error('加载消息统计失败', error)
  }
}

// 加载规则统计
const loadRuleStats = async () => {
  try {
    const data = await getRuleStats()
    
    if (!ruleChartRef.value) return
    if (!ruleChart) {
      ruleChart = echarts.init(ruleChartRef.value)
    }
    
    const option: EChartsOption = {
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c}次 ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 'left',
        top: 'middle'
      },
      series: [
        {
          name: '规则触发次数',
          type: 'pie',
          radius: '60%',
          data: data.rules || [],
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }
      ]
    }
    
    ruleChart.setOption(option)
  } catch (error) {
    console.error('加载规则统计失败', error)
  }
}

// 窗口大小变化时重新渲染图表
const handleResize = () => {
  userChart?.resize()
  messageChart?.resize()
  ruleChart?.resize()
}

onMounted(() => {
  loadOverview()
  loadUserStats()
  loadMessageStats()
  loadRuleStats()
  
  window.addEventListener('resize', handleResize)
})
</script>

<style scoped>
.stats-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-card {
  display: flex;
  align-items: center;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  color: #fff;
  font-size: 28px;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 5px;
}
</style>
