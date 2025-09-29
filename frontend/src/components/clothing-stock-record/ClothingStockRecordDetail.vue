<!-- src/components/clothing-stock-record/ClothingStockRecordDetail.vue -->
<template>
  <div class="stock-record-detail-container">
    <div class="header">
      <h2>库存记录详情</h2>
      <button class="api-btn" @click="openApiUrl" title="查看API数据">API</button>
    </div>

    <!-- 添加Tab页 -->
    <div class="tabs">
      <button
        :class="{ active: activeTab === 'detail' }"
        @click="activeTab = 'detail'"
      >
        库存记录详情
      </button>
      <button
        :class="{ active: activeTab === 'updates' }"
        @click="activeTab = 'updates'"
      >
        更新记录
      </button>
    </div>

    <div class="stock-record-detail-content">
      <!-- 库存记录详情 Tab -->
      <div v-show="activeTab === 'detail'">
        <div class="detail-section">
          <h3>基本信息</h3>
          <div class="detail-content">
            <!-- 第一行：id，服装id，服装名称，尺寸 -->
            <div class="detail-row">
              <div class="detail-item">
                <span class="detail-label">ID:</span>
                <span class="detail-value">{{ stockRecord.id }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">服装ID:</span>
                <span class="detail-value">{{ stockRecord.clothingId }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">服装名称:</span>
                <span class="detail-value">
                  <a
                    v-if="stockRecord.clothingId"
                    @click="openClothingDetail"
                    class="clothing-link"
                  >
                    {{ stockRecord.clothingName || '-' }}
                  </a>
                  <span v-else>{{ stockRecord.clothingName || '-' }}</span>
                </span>
              </div>
              <div class="detail-item">
                <span class="detail-label">尺寸:</span>
                <span class="detail-value">{{ stockRecord.size }}</span>
              </div>
            </div>

            <!-- 第二行：操作类型，数量，操作前库存，操作后库存 -->
            <div class="detail-row">
              <div class="detail-item">
                <span class="detail-label">操作类型:</span>
                <span class="detail-value">
                  <span :class="['operation-type',
                    stockRecord.operationType === 'INBOUND' ? 'inbound' : 'outbound']">
                    {{ stockRecord.operationType === 'INBOUND' ? '入库' : '出库' }}
                  </span>
                </span>
              </div>
              <div class="detail-item">
                <span class="detail-label">数量:</span>
                <span class="detail-value">{{ stockRecord.quantity }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">操作前库存:</span>
                <span class="detail-value">{{ stockRecord.previousStock !== null ? stockRecord.previousStock : '-' }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">操作后库存:</span>
                <span class="detail-value">{{ stockRecord.currentStock !== null ? stockRecord.currentStock : '-' }}</span>
              </div>
            </div>

            <!-- 第三行：创建时间，更新时间，操作员 -->
            <div class="detail-row">
              <div class="detail-item">
                <span class="detail-label">创建时间:</span>
                <span class="detail-value">{{ formatDate(stockRecord.createdTime) }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">更新时间:</span>
                <span class="detail-value">{{ formatDate(stockRecord.updateTime) }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">操作员:</span>
                <span class="detail-value">{{ stockRecord.operator || '-' }}</span>
              </div>
            </div>
          </div>

          <!-- 返回按钮放在右下角 -->
          <div class="form-actions">
            <button class="back-btn-form" @click="goBack">返回</button>
          </div>
        </div>
      </div>

      <!-- 更新记录 Tab -->
      <div v-show="activeTab === 'updates'">
        <!-- 只在未选择记录时显示列表 -->
        <UpdateRecordList
          v-if="!selectedUpdateRecord"
          ref="updateRecordList"
          filter-entity-type="ClothingStockRecord"
          :filter-entity-id="stockRecord.id"
          :hide-actions="true"
          :hide-filters="true"
          :hide-pagination="true"
          @view-record="handleViewRecord" />

        <!-- 更新记录详情 -->
        <div v-else class="update-record-detail-wrapper">
          <div class="detail-header">
            <button class="back-btn" @click="selectedUpdateRecord = null">← 返回</button>
            <h3>更新记录详情</h3>
          </div>
          <UpdateRecordDetail
            :record="selectedUpdateRecord"
            @back="selectedUpdateRecord = null" />
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import UpdateRecordList from '../update-record/UpdateRecordList.vue';
import UpdateRecordDetail from '../update-record/UpdateRecordDetail.vue';

export default {
  name: 'ClothingStockRecordDetail',
  components: {
    UpdateRecordList,
    UpdateRecordDetail
  },
  props: {
    stockRecord: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      activeTab: 'detail',
      selectedUpdateRecord: null
    };
  },
  methods: {
    goBack() {
      this.$emit('back');
    },

    formatDate(dateString) {
      if (!dateString) return '-';
      const date = new Date(dateString);
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      const hours = String(date.getHours()).padStart(2, '0');
      const minutes = String(date.getMinutes()).padStart(2, '0');
      const seconds = String(date.getSeconds()).padStart(2, '0');
      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
    },

    // 打开API网址查看数据
    openApiUrl() {
      if (this.stockRecord.id) {
        // 构造API URL，假设API端点为 /api/clothing-stock-records/{id}
        const apiUrl = `${window.location.origin}/api/clothing-stock-records/${this.stockRecord.id}`;
        window.open(apiUrl, '_blank');
      } else {
        alert('库存记录ID不存在，无法打开API链接');
      }
    },

    // 处理查看更新记录事件
    handleViewRecord(record) {
      this.selectedUpdateRecord = record;
    },

    // 打开服装详情页面
    openClothingDetail() {
      if (this.stockRecord.clothingId) {
        // 打开新窗口跳转到服装详情页面
        const clothingUrl = `#/clothing/${this.stockRecord.clothingId}`;
        window.open(clothingUrl, '_blank');
      }
    }
  }
};
</script>

<style scoped>
.stock-record-detail-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h2 {
  margin: 0;
  color: #333;
}

.api-btn {
  background-color: #409eff;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.api-btn:hover {
  background-color: #337ecc;
}

/* Tab页样式 */
.tabs {
  display: flex;
  margin-bottom: 20px;
  border-bottom: 1px solid #dcdfe6;
}

.tabs button {
  padding: 10px 20px;
  background-color: #f5f7fa;
  border: 1px solid #dcdfe6;
  border-bottom: none;
  border-radius: 4px 4px 0 0;
  cursor: pointer;
  margin-right: 5px;
}

.tabs button.active {
  background-color: #ffffff;
  border-bottom: 1px solid #ffffff;
  margin-bottom: -1px;
  font-weight: bold;
}

.stock-record-detail-content {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.detail-section {
  margin-bottom: 20px;
}

.detail-section h3 {
  margin-top: 0;
  color: #303133;
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 10px;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.detail-row {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  padding: 10px 0;
  border-bottom: 1px solid #f5f7fa;
}

.detail-item {
  flex: 1;
  min-width: 200px;
}

.detail-label {
  font-weight: bold;
  color: #606266;
  display: inline-block;
  min-width: 100px;
}

.detail-value {
  color: #303133;
}

.operation-type {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
}

.operation-type.inbound {
  background-color: #f0f9eb;
  color: #67c23a;
}

.operation-type.outbound {
  background-color: #fef0f0;
  color: #f56c6c;
}

/* 服装名称链接样式 */
.clothing-link {
  color: #409eff;
  text-decoration: underline;
  cursor: pointer;
}

.clothing-link:hover {
  color: #337ecc;
}

/* 返回按钮样式 */
.form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.form-actions button {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.back-btn-form {
  background-color: #6c757d;
  color: white;
}

.back-btn-form:hover {
  background-color: #5a6268;
}

/* 更新记录详情样式 */
.update-record-detail-wrapper {
  margin-top: 20px;
}

.detail-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.detail-header h3 {
  margin: 0;
  margin-left: 10px;
}

.back-btn {
  background-color: #f0f0f0;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.back-btn:hover {
  background-color: #e0e0e0;
}

@media (max-width: 768px) {
  .header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .detail-row {
    flex-direction: column;
  }

  .detail-item {
    min-width: auto;
  }

  .detail-label {
    display: inline-block;
    margin-bottom: 5px;
  }
}
</style>
