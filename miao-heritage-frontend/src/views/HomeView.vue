<template>
  <div class="home">
    <div class="banner">
      <h1>苗族文化遗产区块链溯源电商平台</h1>
      <p>传承千年文化，区块链溯源，保障真品</p>
      <div class="banner-actions">
        <el-button type="primary" size="large" @click="$router.push('/products')">浏览商品</el-button>
        <el-button type="success" size="large" @click="$router.push('/trace')">溯源查询</el-button>
      </div>
    </div>
    
    <div class="features">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-card class="feature-card">
            <template #header>
              <div class="card-header">
                <h3>区块链溯源</h3>
              </div>
            </template>
            <div class="feature-content">
              <el-icon size="50" color="#409EFF"><Connection /></el-icon>
              <p>基于区块链技术，记录苗族文化遗产从创作到销售的全过程，确保真实可信的溯源信息。</p>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="8">
          <el-card class="feature-card">
            <template #header>
              <div class="card-header">
                <h3>AI鉴别</h3>
              </div>
            </template>
            <div class="feature-content">
              <el-icon size="50" color="#67C23A"><Monitor /></el-icon>
              <p>运用人工智能技术，对苗族银饰、服饰等文化遗产进行真伪鉴别，提供专业分析报告。</p>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="8">
          <el-card class="feature-card">
            <template #header>
              <div class="card-header">
                <h3>文化传承</h3>
              </div>
            </template>
            <div class="feature-content">
              <el-icon size="50" color="#E6A23C"><Collection /></el-icon>
              <p>深度解读苗族文化遗产背后的历史渊源、工艺特点和文化象征，促进非物质文化遗产保护。</p>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
    
    <div class="products-preview">
      <h2>精选商品</h2>
      <el-row :gutter="20">
        <el-col v-for="product in featuredProducts" :key="product.id" :span="6">
          <el-card class="product-card" @click="$router.push(`/product/${product.id}`)">
            <img :src="product.image" class="product-image" />
            <div class="product-info">
              <h3>{{ product.name }}</h3>
              <p class="product-price">¥{{ product.price }}</p>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <div class="view-more">
        <el-button type="primary" @click="$router.push('/products')">查看更多</el-button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'
import { Connection, Monitor, Collection } from '@element-plus/icons-vue'

export default {
  name: 'HomeView',
  components: {
    Connection,
    Monitor,
    Collection
  },
  setup() {
    const featuredProducts = ref([
      {
        id: 1,
        name: '苗族银项圈',
        price: '1280.00',
        image: '/images/products/silver-necklace.jpg'
      },
      {
        id: 2,
        name: '苗族刺绣服饰',
        price: '980.00',
        image: '/images/products/embroidery-dress.jpg'
      },
      {
        id: 3,
        name: '苗族银手镯',
        price: '680.00',
        image: '/images/products/silver-bracelet.jpg'
      },
      {
        id: 4,
        name: '苗族头饰',
        price: '1580.00',
        image: '/images/products/headdress.jpg'
      }
    ])
    
    return {
      featuredProducts
    }
  }
}
</script>

<style scoped>
.home {
  padding: 20px;
}

.banner {
  background: linear-gradient(135deg, #1976d2, #64b5f6);
  color: white;
  padding: 60px 20px;
  text-align: center;
  border-radius: 8px;
  margin-bottom: 40px;
}

.banner h1 {
  font-size: 2.5rem;
  margin-bottom: 20px;
}

.banner p {
  font-size: 1.2rem;
  margin-bottom: 30px;
}

.banner-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
}

.features {
  margin-bottom: 40px;
}

.feature-card {
  height: 100%;
  transition: transform 0.3s;
}

.feature-card:hover {
  transform: translateY(-5px);
}

.feature-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 15px;
}

.products-preview {
  margin-bottom: 40px;
}

.products-preview h2 {
  text-align: center;
  margin-bottom: 30px;
  font-size: 1.8rem;
}

.product-card {
  cursor: pointer;
  transition: transform 0.3s;
  height: 100%;
}

.product-card:hover {
  transform: translateY(-5px);
}

.product-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.product-info {
  padding: 15px 0;
}

.product-info h3 {
  margin: 0 0 10px;
  font-size: 1.1rem;
}

.product-price {
  color: #f56c6c;
  font-weight: bold;
  margin: 0;
}

.view-more {
  text-align: center;
  margin-top: 30px;
}
</style> 