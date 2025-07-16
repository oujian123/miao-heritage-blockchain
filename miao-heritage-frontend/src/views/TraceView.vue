<template>
  <div class="trace-container">
    <el-card class="trace-card">
      <template #header>
        <div class="card-header">
          <h2>苗族文化遗产区块链溯源</h2>
        </div>
      </template>
      
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="10" animated />
      </div>
      
      <div v-else-if="error" class="error-container">
        <el-result
          icon="error"
          title="查询失败"
          :sub-title="error"
        >
          <template #extra>
            <el-button type="primary" @click="fetchAssetData">重试</el-button>
          </template>
        </el-result>
      </div>
      
      <div v-else-if="!assetData" class="search-container">
        <el-form :model="form" label-width="120px">
          <el-form-item label="资产ID">
            <el-input v-model="form.assetId" placeholder="请输入资产ID" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="fetchAssetData">查询</el-button>
          </el-form-item>
        </el-form>
        
        <div class="qrcode-scanner">
          <h3>或者扫描二维码</h3>
          <el-button type="success" @click="startScan">扫描二维码</el-button>
        </div>
      </div>
      
      <div v-else class="asset-info">
        <el-descriptions title="资产基本信息" :column="1" border>
          <el-descriptions-item label="资产ID">{{ assetData.id }}</el-descriptions-item>
          <el-descriptions-item label="资产名称">{{ assetData.name }}</el-descriptions-item>
          <el-descriptions-item label="资产类型">{{ assetData.type }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDate(assetData.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="创建者">{{ assetData.creator }}</el-descriptions-item>
          <el-descriptions-item label="当前所有者">{{ assetData.owner }}</el-descriptions-item>
        </el-descriptions>
        
        <div class="asset-detail">
          <h3>资产详情</h3>
          <el-card class="detail-card">
            <div v-if="assetData.images && assetData.images.length > 0" class="asset-images">
              <el-carousel :interval="4000" type="card" height="300px">
                <el-carousel-item v-for="(image, index) in assetData.images" :key="index">
                  <img :src="image" alt="资产图片" class="asset-image" />
                </el-carousel-item>
              </el-carousel>
            </div>
            
            <div class="asset-description">
              <h4>工艺描述</h4>
              <p>{{ assetData.description }}</p>
            </div>
            
            <div class="asset-materials">
              <h4>材料信息</h4>
              <el-tag
                v-for="(material, index) in assetData.materials"
                :key="index"
                class="material-tag"
              >
                {{ material }}
              </el-tag>
            </div>
          </el-card>
        </div>
        
        <div class="asset-history">
          <h3>所有权历史</h3>
          <el-timeline>
            <el-timeline-item
              v-for="(history, index) in assetData.history"
              :key="index"
              :timestamp="formatDate(history.timestamp)"
              :type="getTimelineItemType(index)"
            >
              <el-card class="history-card">
                <h4>{{ getHistoryTitle(history.action) }}</h4>
                <p>{{ history.description }}</p>
                <p class="transaction-hash">交易哈希: {{ history.transactionId }}</p>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </div>
        
        <div class="asset-actions">
          <el-button @click="resetSearch">返回搜索</el-button>
          <el-button type="primary" @click="verifyOnBlockchain">在区块链上验证</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import axios from 'axios'

export default {
  name: 'TraceView',
  setup() {
    const loading = ref(false)
    const error = ref(null)
    const assetData = ref(null)
    const form = reactive({
      assetId: ''
    })
    
    const fetchAssetData = async () => {
      if (!form.assetId) {
        error.value = '请输入资产ID'
        return
      }
      
      loading.value = true
      error.value = null
      
      try {
        const response = await axios.get(`/api/blockchain/assets/${form.assetId}`)
        assetData.value = response.data
      } catch (err) {
        console.error('获取资产数据失败:', err)
        error.value = err.response?.data?.message || '获取资产数据失败，请检查资产ID是否正确'
        assetData.value = null
      } finally {
        loading.value = false
      }
    }
    
    const resetSearch = () => {
      assetData.value = null
      form.assetId = ''
      error.value = null
    }
    
    const startScan = () => {
      // 这里可以实现二维码扫描功能
      // 由于浏览器限制，可能需要使用第三方库或原生应用支持
      alert('二维码扫描功能开发中...')
    }
    
    const verifyOnBlockchain = () => {
      window.open(`https://explorer.example.com/asset/${assetData.value.id}`, '_blank')
    }
    
    const formatDate = (timestamp) => {
      if (!timestamp) return ''
      const date = new Date(timestamp)
      return date.toLocaleString('zh-CN')
    }
    
    const getTimelineItemType = (index) => {
      const types = ['primary', 'success', 'warning', 'danger', 'info']
      return types[index % types.length]
    }
    
    const getHistoryTitle = (action) => {
      const actionMap = {
        'CREATE': '创建资产',
        'TRANSFER': '转移所有权',
        'UPDATE': '更新资产',
        'VERIFY': '验证真伪'
      }
      return actionMap[action] || action
    }
    
    return {
      loading,
      error,
      assetData,
      form,
      fetchAssetData,
      resetSearch,
      startScan,
      verifyOnBlockchain,
      formatDate,
      getTimelineItemType,
      getHistoryTitle
    }
  }
}
</script>

<style scoped>
.trace-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.trace-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.loading-container,
.error-container,
.search-container {
  min-height: 300px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.qrcode-scanner {
  margin-top: 30px;
  text-align: center;
}

.asset-info {
  margin-top: 20px;
}

.asset-detail,
.asset-history {
  margin-top: 30px;
}

.detail-card {
  margin-top: 15px;
}

.asset-images {
  margin-bottom: 20px;
}

.asset-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.asset-description,
.asset-materials {
  margin-top: 15px;
}

.material-tag {
  margin-right: 10px;
  margin-bottom: 10px;
}

.history-card {
  margin-bottom: 10px;
}

.transaction-hash {
  font-size: 12px;
  color: #909399;
  word-break: break-all;
}

.asset-actions {
  margin-top: 30px;
  display: flex;
  justify-content: center;
  gap: 20px;
}
</style> 