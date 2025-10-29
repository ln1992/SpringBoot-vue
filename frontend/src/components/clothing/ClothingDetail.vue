<!-- src/components/clothing/ClothingDetail.vue -->
<template>
  <div class="clothing-detail-container">
    <div class="header">
      <h2>编辑服装</h2>
      <button class="api-btn" @click="openApiUrl" title="查看API数据">API</button>
    </div>

    <!-- 添加Tab页 -->
    <div class="tabs">
      <button
        :class="{ active: activeTab === 'detail' }"
        @click="activeTab = 'detail'"
      >
        服装详情
      </button>
      <button
        :class="{ active: activeTab === 'records' }"
        @click="activeTab = 'records'"
      >
        库存记录
      </button>
      <button
        :class="{ active: activeTab === 'updates' }"
        @click="activeTab = 'updates'"
      >
        更新记录
      </button>
    </div>

    <div class="clothing-detail-content">
      <!-- 服装详情 Tab -->
      <div v-show="activeTab === 'detail'">
        <form @submit.prevent="handleEditSubmit">
          <div class="detail-section">
            <div class="detail-content">
              <!-- 第一行：id，名称，品牌 -->
              <div class="detail-row-group">
                <div class="detail-row">
                  <span class="detail-label">ID:</span>
                  <span class="detail-value">{{ clothing.id }}</span>
                </div>
                <div class="detail-row">
                  <span class="detail-label">名称:</span>
                  <span class="detail-value">
                    <input v-model="editForm.name" type="text" required class="edit-input" />
                  </span>
                </div>
                <div class="detail-row">
                  <span class="detail-label">品牌:</span>
                  <span class="detail-value">
                    <input v-model="editForm.brand" type="text" class="edit-input" />
                  </span>
                </div>
              </div>

              <!-- 第二行：价格，状态 -->
              <div class="detail-row-group">
                <div class="detail-row">
                  <span class="detail-label">价格:</span>
                  <span class="detail-value">
                    <input v-model.number="editForm.price" type="number" step="0.01" class="edit-input" />
                  </span>
                </div>
                <div class="detail-row">
                  <span class="detail-label">安全库存:</span>
                  <span class="detail-value">
                    <input v-model.number="editForm.safetyStock" type="number" min="0" class="edit-input" />
                  </span>
                </div>
                <div class="detail-row">
                  <span class="detail-label">状态:</span>
                  <span class="detail-value">
                    <span :class="['status-badge', clothing.valid ? 'status-active' : 'status-inactive']">
                      {{ clothing.valid ? '已上线' : '已下线' }}
                    </span>
                  </span>
                </div>
              </div>

              <!-- 尺码信息移到创建时间和更新时间之前 -->
              <div class="detail-section">
                <div class="stock-table">
                  <div class="stock-table-row header">
                    <div class="stock-table-cell">尺码</div>
                    <div class="stock-table-cell">S</div>
                    <div class="stock-table-cell">M</div>
                    <div class="stock-table-cell">L</div>
                  </div>
                  <div class="stock-table-row">
                    <div class="stock-table-cell">总入库数</div>
                    <div class="stock-table-cell">{{ getTotalQuantityBySize('S') }}</div>
                    <div class="stock-table-cell">{{ getTotalQuantityBySize('M') }}</div>
                    <div class="stock-table-cell">{{ getTotalQuantityBySize('L') }}</div>
                  </div>
                  <div class="stock-table-row">
                    <div class="stock-table-cell">当前库存</div>
                    <div class="stock-table-cell">{{ getCurrentStockBySize('S') }}</div>
                    <div class="stock-table-cell">{{ getCurrentStockBySize('M') }}</div>
                    <div class="stock-table-cell">{{ getCurrentStockBySize('L') }}</div>
                  </div>
                  <div class="stock-table-row">
                    <div class="stock-table-cell">已出库数</div>
                    <div class="stock-table-cell">{{ getOutboundQuantityBySize('S') }}</div>
                    <div class="stock-table-cell">{{ getOutboundQuantityBySize('M') }}</div>
                    <div class="stock-table-cell">{{ getOutboundQuantityBySize('L') }}</div>
                  </div>
                </div>

                <!-- 操作按钮放在同一行 -->
                <div class="form-actions inline">
                  <button class="stock-btn inbound-btn" @click="showStockOperation('inbound')">入库</button>
                  <button class="stock-btn outbound-btn" @click="showStockOperation('outbound')">出库</button>
                  <button v-if="clothing.valid" class="offline-btn" @click="toggleClothingStatus(false)">下线</button>
                  <template v-else>
                    <button class="online-btn" @click="toggleClothingStatus(true)">上线</button>
                    <button class="delete-btn" @click="deleteClothing">删除</button>
                  </template>
                </div>
              </div>

              <!-- 第三行：创建时间和更新时间 -->
              <div class="detail-row-group">
                <div class="detail-row">
                  <span class="detail-label">创建时间:</span>
                  <span class="detail-value">{{ formatDate(clothing.createdTime) }}</span>
                </div>
                <div class="detail-row">
                  <span class="detail-label">更新时间:</span>
                  <span class="detail-value">{{ formatDate(clothing.updateTime) }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 返回和保存按钮在页面右下方但在框架内 -->
          <div class="form-actions bottom-right">
            <button type="button" @click="goBack" class="cancel-btn">返回</button>
            <button type="submit" class="save-btn" :disabled="editSubmitting">
              {{ editSubmitting ? '保存中...' : '保存' }}
            </button>
          </div>
        </form>
      </div>

      <!-- 库存记录 Tab -->
      <div v-show="activeTab === 'records'">
        <ClothingStockRecordList
          v-if="!selectedStockRecord"
          ref="stockRecordList"
          :filter-clothing-id="clothing.id"
          :hide-actions="true"
          :hide-filters="true"
          :hide-pagination="true"
          @view-record="handleViewRecord" />

        <!-- 库存记录详情 -->
        <div v-else class="stock-record-detail-wrapper">
          <div class="detail-header">
            <button class="back-btn" @click="selectedStockRecord = null">← 返回</button>
            <h3>库存记录详情</h3>
          </div>
          <ClothingStockRecordDetail
            :stock-record="selectedStockRecord"
            @back="selectedStockRecord = null" />
        </div>
      </div>

      <!-- 更新记录 Tab -->
      <div v-show="activeTab === 'updates'">
        <!-- 只在未选择记录时显示列表 -->
        <UpdateRecordList
          v-if="!selectedUpdateRecord"
          ref="updateRecordList"
          filter-entity-type="Clothing"
          :filter-entity-id="clothing.id"
          :hide-actions="true"
          :hide-filters="true"
          :hide-pagination="true"
          @view-record="handleViewUpdateRecord" />

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

    <!-- 库存操作模态框 -->
    <div class="modal" v-if="showStockModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>{{ stockOperationType === 'inbound' ? '入库操作' : '出库操作' }}</h3>
          <button class="close-btn" @click="closeStockModal">&times;</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="handleStockOperation">
            <div class="form-group">
              <label>服装: {{ clothing.name }}</label>
            </div>

            <div class="form-group">
              <label for="size">尺寸:</label>
              <select id="size" v-model="stockForm.size" required>
                <option value="S">S</option>
                <option value="M">M</option>
                <option value="L">L</option>
              </select>
            </div>

            <div class="form-group">
              <label for="quantity">数量:</label>
              <input
                id="quantity"
                v-model.number="stockForm.quantity"
                type="number"
                min="1"
                required
              />
            </div>

            <div class="form-group">
              <label for="operator">操作员:</label>
              <input
                id="operator"
                v-model="stockForm.operator"
                type="text"
                required
              />
            </div>

            <div class="form-actions">
              <button type="button" @click="closeStockModal" class="cancel-btn">取消</button>
              <button type="submit" class="save-btn" :disabled="stockSubmitting">
                {{ stockSubmitting ? '处理中...' : '确认' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { clothingService } from '../../api';
import ClothingStockRecordList from '../clothing-stock-record/ClothingStockRecordList.vue';
import ClothingStockRecordDetail from '../clothing-stock-record/ClothingStockRecordDetail.vue';
import UpdateRecordList from '../update-record/UpdateRecordList.vue';
import UpdateRecordDetail from '../update-record/UpdateRecordDetail.vue';

export default {
  name: 'ClothingDetail',
  components: {
    ClothingStockRecordList,
    ClothingStockRecordDetail,
    UpdateRecordList,
    UpdateRecordDetail
  },
  props: {
    clothing: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      activeTab: 'detail',
      selectedStockRecord: null,
      selectedUpdateRecord: null,
      showStockModal: false,
      editSubmitting: false,
      stockSubmitting: false,
      stockOperationType: 'inbound', // 'inbound' 或 'outbound',

      // 编辑表单相关
      editForm: {
        id: null,
        name: '',
        brand: '',
        price: null,
        valid: true
      },

      // 库存操作表单
      stockForm: {
        size: 'S',
        quantity: 1,
        operator: ''
      }
    };
  },
  created() {
    // 初始化编辑表单
    this.editForm = { ...this.clothing };
  },
  methods: {
    goBack() {
      this.$emit('back');
    },

    async handleEditSubmit() {
      this.editSubmitting = true;

      try {
        const response = await clothingService.update(this.editForm.id, this.editForm);
        if (response.status === 200) {
          this.$emit('clothing-updated', response.data);
        }
      } catch (err) {
        console.error('更新服装失败:', err);
        alert('更新失败: ' + (err.message || '未知错误'));
      } finally {
        this.editSubmitting = false;
      }
    },

    getCurrentStockBySize(size) {
      if (!this.clothing || !this.clothing.currentStockBySize) return 0;
      return this.clothing.currentStockBySize[size] !== undefined ?
        this.clothing.currentStockBySize[size] : 0;
    },

    getTotalQuantityBySize(size) {
      if (!this.clothing || !this.clothing.totalQuantityBySize) return 0;
      return this.clothing.totalQuantityBySize[size] !== undefined ?
        this.clothing.totalQuantityBySize[size] : 0;
    },

    getOutboundQuantityBySize(size) {
      const total = this.getTotalQuantityBySize(size);
      const current = this.getCurrentStockBySize(size);
      return total - current;
    },

    async toggleClothingStatus(valid) {
      try {
        if (valid) {
          // 上线服装
          await clothingService.activateClothing(this.clothing.id);
        } else {
          // 下线服装
          await clothingService.deactivateClothing(this.clothing.id);
        }
        // 重新获取最新的服装信息
        const updatedResponse = await clothingService.getById(this.clothing.id);
        if (updatedResponse.status === 200) {
          this.$emit('clothing-updated', updatedResponse.data);
        }
      } catch (err) {
        console.error('更新服装状态失败:', err);
        let errorMessage = '未知错误';
        if (err.response && err.response.data && err.response.data.message) {
          errorMessage = err.response.data.message;
        } else if (err.message) {
          errorMessage = err.message;
        }
        alert('更新服装状态失败: ' + errorMessage);
      }
    },

    async deleteClothing() {
      if (!confirm('确定要删除这个服装吗？')) {
        return;
      }

      try {
        await clothingService.delete(this.clothing.id);
        this.$emit('back');
      } catch (err) {
        console.error('删除服装失败:', err);
        alert('删除失败: ' + (err.message || '未知错误'));
      }
    },

    handleViewRecord(record) {
      this.selectedStockRecord = record;
    },

    handleViewUpdateRecord(record) {
      this.selectedUpdateRecord = record;
    },

    openApiUrl() {
      // 打开API数据查看页面
      const apiUrl = `${window.location.origin}/api/clothings/${this.clothing.id}`;
      window.open(apiUrl, '_blank');
    },

    showStockOperation(operationType) {
      this.stockOperationType = operationType;
      this.stockForm = {
        size: 'S',
        quantity: 1,
        operator: ''
      };
      this.showStockModal = true;
    },

    closeStockModal() {
      this.showStockModal = false;
    },

    async handleStockOperation() {
      this.stockSubmitting = true;

      try {
        // 确保数量是整数类型
        const stockData = {
          ...this.stockForm,
          quantity: parseInt(this.stockForm.quantity, 10)
        };

        let response;
        if (this.stockOperationType === 'inbound') {
          response = await clothingService.inbound(this.clothing.id, stockData);
        } else {
          response = await clothingService.outbound(this.clothing.id, stockData);
        }

        if (response.status === 200) {
          // 重新获取最新的服装信息
          const updatedResponse = await clothingService.getById(this.clothing.id);
          if (updatedResponse.status === 200) {
            this.$emit('clothing-updated', updatedResponse.data);
          }
          this.closeStockModal();
        }
      } catch (err) {
        console.error('库存操作失败:', err);
        let errorMessage = '未知错误';
        if (err.response && err.response.data && err.response.data.message) {
          errorMessage = err.response.data.message;
        } else if (err.message) {
          errorMessage = err.message;
        }
        alert('库存操作失败: ' + errorMessage);
      } finally {
        this.stockSubmitting = false;
      }
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
    }
  }
};
</script>

<style scoped>
/* 添加编辑输入框样式 */
.edit-input {
  width: 100%;
  padding: 6px 8px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  box-sizing: border-box;
}

/* 底部按钮样式 */
.form-actions.bottom-right {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.clothing-detail-container {
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

.clothing-detail-content {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: relative;
}

.detail-section {
  margin-bottom: 20px;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.detail-row-group {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 15px;
}

.detail-row {
  display: flex;
  padding: 10px 0;
  border-bottom: 1px solid #f5f7fa;
}

.detail-label {
  flex: 0 0 120px;
  font-weight: bold;
  color: #606266;
}

.detail-value {
  flex: 1;
  color: #303133;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
}

.status-active {
  background-color: #f0f9eb;
  color: #67c23a;
}

.status-inactive {
  background-color: #fef0f0;
  color: #f56c6c;
}

/* 库存表格样式 */
.stock-table {
  width: 100%;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 20px;
}

.stock-table-row {
  display: flex;
  border-bottom: 1px solid #dcdfe6;
}

.stock-table-row.header {
  background-color: #f5f7fa;
  font-weight: bold;
}

.stock-table-cell {
  flex: 1;
  padding: 12px;
  text-align: center;
  border-right: 1px solid #dcdfe6;
}

.stock-table-cell:last-child {
  border-right: none;
}

/* 按钮样式 */
.form-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-start;
  flex-wrap: wrap;
}

.form-actions.inline {
  border-top: 1px solid #eee;
  padding-top: 20px;
  margin-top: 20px;
}

.online-btn,
.offline-btn,
.delete-btn,
.save-btn,
.cancel-btn,
.stock-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.2s;
  position: relative;
  z-index: 2;
}

.save-btn {
  background-color: #007bff;
  color: white;
}

.save-btn:hover {
  background-color: #0056b3;
}

.save-btn:disabled {
  background-color: #6c757d;
  cursor: not-allowed;
}

.cancel-btn {
  background-color: #6c757d;
  color: white;
}

.cancel-btn:hover {
  background-color: #5a6268;
}

.delete-btn {
  background-color: #dc3545;
  color: white;
}

.delete-btn:hover {
  background-color: #c82333;
}

.online-btn {
  background-color: #28a745;
  color: white;
}

.online-btn:hover {
  background-color: #218838;
}

.offline-btn {
  background-color: #ffc107;
  color: #212529;
}

.offline-btn:hover {
  background-color: #e0a800;
}

.stock-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.2s;
  position: relative;
  z-index: 2;
}

.inbound-btn {
  background-color: #67c23a;
  color: white;
}

.inbound-btn:hover {
  background-color: #55a028;
}

.outbound-btn {
  background-color: #f56c6c;
  color: white;
}

.outbound-btn:hover {
  background-color: #e05252;
}

/* 模态框样式 */
.modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background-color: white;
  border-radius: 4px;
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #dcdfe6;
}

.modal-header h3 {
  margin: 0;
  color: #303133;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #909399;
}

.close-btn:hover {
  color: #212529;
  background-color: #f8f9fa;
  border-radius: 50%;
}

.modal-body {
  padding: 20px;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  color: #606266;
  font-weight: bold;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  box-sizing: border-box;
}

/* 库存记录详情样式 */
.stock-record-detail-wrapper,
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

.detail-header .back-btn {
  background-color: #f0f0f0;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.detail-header .back-btn:hover {
  background-color: #e0e0e0;
}

@media (max-width: 768px) {
  .header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .detail-row-group {
    grid-template-columns: 1fr;
  }

  .detail-row {
    flex-direction: column;
  }

  .detail-label {
    flex: 0 0 auto;
    margin-bottom: 5px;
  }

  .form-actions {
    flex-direction: column;
  }

  .tabs {
    flex-direction: column;
  }

  .tabs button {
    margin-bottom: 5px;
    border-bottom: 1px solid #dcdfe6;
    border-radius: 4px;
  }

  .tabs button.active {
    margin-bottom: 5px;
  }

  .form-actions.bottom-right {
    flex-direction: column;
  }
}
</style>
