<!-- src/components/clothing/ClothingList.vue -->
<template>
  <div class="clothing-container">
    <!-- 服装列表界面 -->
    <div v-if="!selectedClothing && !showAddForm">
      <div class="header">
        <h2>服装管理</h2>
        <div class="header-actions">
          <button class="refresh-btn" @click="fetchClothings">刷新</button>
          <button class="add-btn" @click="showAddClothingForm">新增服装</button>
        </div>
      </div>

      <!-- 添加搜索和筛选区域 -->
      <div class="filter-section">
        <div class="search-group">
          <input
            type="text"
            v-model="searchKeyword"
            placeholder="搜索名称..."
            @input="filterClothings"
          >
        </div>
        <div class="filter-group">
          <select v-model="filterBrand" @change="filterClothings">
            <option value="">所有品牌</option>
            <option v-for="brand in brands" :key="brand" :value="brand">
              {{ brand }}
            </option>
          </select>
        </div>
        <div class="sort-group">
          <label>排序:</label>
          <select v-model="sortBy" @change="sortClothings">
            <option value="id">ID</option>
            <option value="name">名称</option>
            <option value="brand">品牌</option>
            <option value="price">价格</option>
            <option value="valid">状态</option>
          </select>
          <select v-model="sortDirection" @change="sortClothings">
            <option value="desc">降序</option>
            <option value="asc">升序</option>
          </select>
        </div>
      </div>

      <div class="loading" v-if="loading">
        <p>正在加载服装数据...</p>
      </div>

      <div class="error" v-else-if="error">
        <p>加载失败: {{ error }}</p>
        <button @click="fetchClothings">重试</button>
      </div>

      <div class="no-data" v-else-if="filteredClothings.length === 0">
        <div class="no-data-content">
          <h3>暂无服装数据</h3>
          <p>点击下方按钮添加您的第一个服装</p>
          <button class="add-btn" @click="showAddClothingForm">新增服装</button>
        </div>
      </div>

      <div class="clothings-table" v-else>
        <div class="table-header">
          <div class="table-cell sortable" @click="sort('id')">
            ID
            <span v-if="sortBy === 'id'">
              {{ sortDirection === 'asc' ? '↑' : '↓' }}
            </span>
          </div>
          <div class="table-cell sortable" @click="sort('name')">
            名称
            <span v-if="sortBy === 'name'">
              {{ sortDirection === 'asc' ? '↑' : '↓' }}
            </span>
          </div>
          <div class="table-cell sortable" @click="sort('brand')">
            品牌
            <span v-if="sortBy === 'brand'">
              {{ sortDirection === 'asc' ? '↑' : '↓' }}
            </span>
          </div>
          <div class="table-cell sortable" @click="sort('price')">
            价格
            <span v-if="sortBy === 'price'">
              {{ sortDirection === 'asc' ? '↑' : '↓' }}
            </span>
          </div>
          <div class="table-cell">S码(总量/库存)</div>
          <div class="table-cell">M码(总量/库存)</div>
          <div class="table-cell">L码(总量/库存)</div>
          <div class="table-cell sortable" @click="sort('valid')">
            状态
            <span v-if="sortBy === 'valid'">
              {{ sortDirection === 'asc' ? '↑' : '↓' }}
            </span>
          </div>
          <div class="table-cell">操作</div>
        </div>

        <div
          class="table-row"
          v-for="clothing in paginatedClothings"
          :key="clothing.id"
        >
          <div class="table-cell">{{ clothing.id }}</div>
          <div class="table-cell clothing-name" @click="viewClothing(clothing)">
            {{ clothing.name || '-' }}
          </div>
          <div class="table-cell">{{ clothing.brand || '-' }}</div>
          <div class="table-cell">¥{{ clothing.price || '-' }}</div>
          <div class="table-cell">{{ getTotalQuantityBySize(clothing, 'S') }}/{{ getCurrentStockBySize(clothing, 'S') }}</div>
          <div class="table-cell">{{ getTotalQuantityBySize(clothing, 'M') }}/{{ getCurrentStockBySize(clothing, 'M') }}</div>
          <div class="table-cell">{{ getTotalQuantityBySize(clothing, 'L') }}/{{ getCurrentStockBySize(clothing, 'L') }}</div>
          <div class="table-cell">
            <span :class="['status-badge', clothing.valid ? 'status-active' : 'status-inactive']">
              {{ clothing.valid ? '已上线' : '已下线' }}
            </span>
          </div>
          <div class="table-cell">
            <div class="action-buttons">
              <!-- 第一行：入库和出库按钮 -->
              <div class="button-row">
                <button
                  class="stock-btn inbound-btn"
                  @click.stop="showStockOperation(clothing, 'inbound')"
                >
                  入库
                </button>
                <button
                  class="stock-btn outbound-btn"
                  @click.stop="showStockOperation(clothing, 'outbound')"
                >
                  出库
                </button>
              </div>

              <!-- 第二行：上下线和删除按钮 -->
              <div class="button-row">
                <button
                  v-if="clothing.valid"
                  class="offline-btn"
                  @click.stop="toggleClothingStatus(clothing.id, false)"
                >
                  下线
                </button>
                <template v-else>
                  <button
                    class="online-btn"
                    @click.stop="toggleClothingStatus(clothing.id, true)"
                  >
                    上线
                  </button>
                  <button
                    class="delete-btn"
                    @click.stop="deleteClothing(clothing.id)"
                    :title="clothing.valid ? '请先下线服装再删除' : ''"
                  >
                    删除
                  </button>
                </template>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页控件 -->
      <div class="pagination" v-if="paginatedClothings.length > 0">
        <div class="pagination-controls">
          <button
            :disabled="currentPage === 1"
            @click="currentPage > 1 && (currentPage--)">
            上一页
          </button>
          <span>第 {{ currentPage }} 页，共 {{ totalPages }} 页 (总计 {{ filteredClothings.length }} 条)</span>
          <button
            :disabled="currentPage === totalPages"
            @click="currentPage < totalPages && (currentPage++)">
            下一页
          </button>
        </div>
        <div class="page-size-selector">
          <label>每页显示:</label>
          <select v-model="pageSize" @change="handlePageSizeChange">
            <option value="5">5</option>
            <option value="10">10</option>
            <option value="20">20</option>
            <option value="50">50</option>
          </select>
        </div>
      </div>
    </div>

    <!-- 服装详情界面 -->
    <ClothingDetail
      v-else-if="selectedClothing && !showAddForm"
      :clothing="selectedClothing"
      @back="goBackToList"
      @clothing-updated="handleClothingUpdated"
    />

    <!-- 新增/编辑服装表单 -->
    <div class="clothing-form-container" v-else-if="showAddForm">
      <div class="header">
        <button class="back-btn" @click="goBackToList">← 返回</button>
        <h2>{{ isEditMode ? '编辑服装' : '新增服装' }}</h2>
      </div>

      <div class="clothing-form-content">
        <form @submit.prevent="handleSubmit">
          <div class="form-section">
            <div class="form-group">
              <label for="name">名称 *</label>
              <input
                id="name"
                v-model="form.name"
                type="text"
                required
              />
              <div class="error-message" v-if="errors.name">{{ errors.name }}</div>
            </div>

            <div class="form-group">
              <label for="brand">品牌</label>
              <input
                id="brand"
                v-model="form.brand"
                type="text"
              />
            </div>

            <div class="form-group">
              <label for="price">价格</label>
              <input
                id="price"
                v-model.number="form.price"
                type="number"
                step="0.01"
              />
            </div>
          </div>

          <div class="form-section">
            <div class="form-actions">
              <button type="button" @click="goBackToList" class="cancel-btn">取消</button>
              <button type="submit" class="save-btn" :disabled="submitting">
                {{ submitting ? '保存中...' : '保存' }}
              </button>
            </div>
          </div>
        </form>
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
              <label>服装: {{ selectedClothingForStock.name }}</label>
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
import ClothingDetail from './ClothingDetail.vue';

export default {
  name: 'ClothingList',
  components: {
    ClothingDetail
  },
  data() {
    return {
      clothings: [],
      filteredClothings: [],
      selectedClothing: null,
      showAddForm: false,
      isEditMode: false,

      // 分页相关
      currentPage: 1,
      pageSize: 10,

      // 搜索和筛选相关数据
      searchKeyword: '',
      filterBrand: '',
      // 排序相关数据
      sortBy: 'id',
      sortDirection: 'desc',

      // 表单相关
      form: {
        id: null,
        name: '',
        brand: '',
        price: null
      },
      errors: {},
      submitting: false,

      // 状态相关
      loading: false,
      error: null,

      // 库存操作相关
      showStockModal: false,
      selectedClothingForStock: null,
      stockOperationType: 'inbound', // 'inbound' 或 'outbound'
      stockForm: {
        size: 'S',
        quantity: 1,
        operator: ''
      },
      stockSubmitting: false
    };
  },
  computed: {
    totalPages() {
      return Math.ceil(this.filteredClothings.length / this.pageSize);
    },
    paginatedClothings() {
      const start = (this.currentPage - 1) * this.pageSize;
      const end = start + this.pageSize;
      return this.filteredClothings.slice(start, end);
    },
    brands() {
      // 从服装数据中提取所有品牌
      const brandSet = new Set();
      this.clothings.forEach(clothing => {
        if (clothing.brand) {
          brandSet.add(clothing.brand);
        }
      });
      return Array.from(brandSet);
    }
  },
  mounted() {
    this.fetchClothings();
  },
  methods: {
    async fetchClothings() {
      this.loading = true;
      this.error = null;
      this.currentPage = 1;

      try {
        const response = await clothingService.getAll();
        this.clothings = response.data || [];
        this.filterClothings();
      } catch (err) {
        this.error = err.message || '获取服装数据失败';
        console.error('获取服装数据失败:', err);
      } finally {
        this.loading = false;
      }
    },

    filterClothings() {
      let filtered = [...this.clothings];

      // 搜索过滤
      if (this.searchKeyword) {
        const keyword = this.searchKeyword.toLowerCase();
        filtered = filtered.filter(clothing =>
          (clothing.name && clothing.name.toLowerCase().includes(keyword)) ||
          (clothing.brand && clothing.brand.toLowerCase().includes(keyword))
        );
      }

      // 品牌过滤
      if (this.filterBrand) {
        filtered = filtered.filter(clothing =>
          clothing.brand === this.filterBrand
        );
      }

      // 排序
      this.sortClothings(filtered);

      this.filteredClothings = filtered;
      this.currentPage = 1;
    },

    sortClothings(clothings) {
      const field = this.sortBy;
      const direction = this.sortDirection;

      clothings.sort((a, b) => {
        let valueA = a[field];
        let valueB = b[field];

        // 处理价格字段
        if (field === 'price') {
          valueA = valueA || 0;
          valueB = valueB || 0;
        }

        // 处理null或undefined值
        if (valueA == null && valueB == null) return 0;
        if (valueA == null) return direction === 'asc' ? -1 : 1;
        if (valueB == null) return direction === 'asc' ? 1 : -1;

        // 比较值
        let comparison = 0;
        if (typeof valueA === 'string' && typeof valueB === 'string') {
          comparison = valueA.localeCompare(valueB);
        } else {
          comparison = valueA < valueB ? -1 : valueA > valueB ? 1 : 0;
        }

        return direction === 'asc' ? comparison : -comparison;
      });
    },

    sort(field) {
      if (this.sortBy === field) {
        // 如果当前已经是这个字段，则切换排序方向
        this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
      } else {
        // 切换到新字段，默认降序
        this.sortBy = field;
        this.sortDirection = 'desc';
      }
      this.filterClothings();
    },

    viewClothing(clothing) {
      this.selectedClothing = clothing;
    },

    showAddClothingForm() {
      this.showAddForm = true;
      this.isEditMode = false;
      this.form = {
        id: null,
        name: '',
        brand: '',
        price: null
      };
      this.errors = {};
    },

    goBackToList() {
      this.selectedClothing = null;
      this.showAddForm = false;
      this.isEditMode = false;
      this.errors = {};
    },

    async handleSubmit() {
      this.submitting = true;
      this.errors = {};

      try {
        let response;
        if (this.isEditMode) {
          // 更新
          response = await clothingService.update(this.form.id, this.form);
        } else {
          // 新增
          response = await clothingService.create(this.form);
        }

        if (response.status === 200 || response.status === 201) {
          await this.fetchClothings();
          if (this.isEditMode) {
            this.selectedClothing = response.data;
          }
          this.showAddForm = false;
        }
      } catch (err) {
        if (err.response && err.response.data && err.response.data.errors) {
          this.errors = err.response.data.errors;
        } else {
          this.error = err.message || '保存失败';
        }
      } finally {
        this.submitting = false;
      }
    },

    async toggleClothingStatus(clothingId, valid) {
      try {
        if (valid) {
          // 上线服装
          await clothingService.activateClothing(clothingId);
        } else {
          // 下线服装
          await clothingService.deactivateClothing(clothingId);
        }
        await this.fetchClothings();
      } catch (err) {
        let errorMessage = '更新服装状态失败';
        if (err.response && err.response.data && err.response.data.message) {
          errorMessage = err.response.data.message;
        } else if (err.message) {
          errorMessage = err.message;
        }
        alert(errorMessage);
      }
    },

    async deleteClothing(id) {
      if (!confirm('确定要删除这个服装吗？')) {
        return;
      }

      try {
        await clothingService.delete(id);
        await this.fetchClothings();
        if (this.selectedClothing && this.selectedClothing.id === id) {
          this.selectedClothing = null;
        }
      } catch (err) {
        this.error = err.message || '删除失败';
      }
    },

    handlePageSizeChange() {
      this.currentPage = 1;
    },

    handleClothingUpdated(updatedClothing) {
      this.selectedClothing = updatedClothing;
      // 更新列表中的服装信息
      const index = this.clothings.findIndex(c => c.id === updatedClothing.id);
      if (index !== -1) {
        this.clothings.splice(index, 1, updatedClothing);
        this.filterClothings();
      }
    },

    getCurrentStockBySize(clothing, size) {
      if (!clothing || !clothing.currentStockBySize) return 0;
      // 处理后端返回的Map格式数据
      if (clothing.currentStockBySize[size] !== undefined) {
        return clothing.currentStockBySize[size];
      }
      return 0;
    },

    getTotalQuantityBySize(clothing, size) {
      if (!clothing || !clothing.totalQuantityBySize) return 0;
      // 处理后端返回的Map格式数据
      if (clothing.totalQuantityBySize[size] !== undefined) {
        return clothing.totalQuantityBySize[size];
      }
      return 0;
    },

    // 库存操作相关方法
    showStockOperation(clothing, operationType) {
      this.selectedClothingForStock = clothing;
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
      this.selectedClothingForStock = null;
    },

    async handleStockOperation() {
      if (!this.selectedClothingForStock) return;

      this.stockSubmitting = true;

      try {
        // 确保数量是整数类型
        const stockData = {
          ...this.stockForm,
          quantity: parseInt(this.stockForm.quantity)
        };

        let response;
        if (this.stockOperationType === 'inbound') {
          response = await clothingService.inbound(
            this.selectedClothingForStock.id,
            stockData
          );
        } else {
          response = await clothingService.outbound(
            this.selectedClothingForStock.id,
            stockData
          );
        }

        if (response.status === 200) {
          await this.fetchClothings();
          this.closeStockModal();
        }
      } catch (err) {
        let errorMessage = '库存操作失败';
        if (err.response && err.response.data && err.response.data.message) {
          errorMessage = err.response.data.message;
        } else if (err.message) {
          errorMessage = err.message;
        }
        alert(errorMessage);
      } finally {
        this.stockSubmitting = false;
      }
    }
  },
  watch: {
    searchKeyword() {
      this.filterClothings();
    },
    filterBrand() {
      this.filterClothings();
    }
  }
};
</script>

<style scoped>
.clothing-container {
  padding: 20px;
  position: relative;
  z-index: 1;
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

.header-actions {
  display: flex;
  gap: 10px;
}

/* 添加搜索和筛选区域样式 */
.filter-section {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f5f5;
  border-radius: 4px;
  align-items: center;
  flex-wrap: wrap;
}

.filter-group,
.search-group,
.sort-group {
  display: flex;
  align-items: center;
  gap: 5px;
}

.filter-group label,
.search-group label,
.sort-group label {
  white-space: nowrap;
}

.filter-group input,
.filter-group select,
.search-group input,
.sort-group select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  min-width: 120px;
}

.search-group input {
  min-width: 150px;
}

.loading,
.error,
.no-data {
  text-align: center;
  padding: 40px 20px;
}

.no-data-content h3 {
  margin-top: 0;
}

.clothings-table {
  border: 1px solid #ddd;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 20px;
}

.table-header {
  display: flex;
  background-color: #f8f9fa;
  font-weight: bold;
  border-bottom: 1px solid #ddd;
}

.table-row {
  display: flex;
  border-bottom: 1px solid #eee;
  transition: background-color 0.2s;
  position: relative;
  z-index: 1;
}

.table-row:hover {
  background-color: #f5f5f5;
}

.table-cell {
  flex: 1;
  padding: 12px;
  border-right: 1px solid #eee;
  display: flex;
  align-items: center;
  min-width: 0;
  word-wrap: break-word;
  word-break: break-word;
  white-space: normal;
}

.table-cell:last-child {
  border-right: none;
}

/* 进一步优化列宽以适应屏幕显示 */
.table-cell:nth-child(1) { flex: 0 0 60px; }   /* ID列 */
.table-cell:nth-child(2) { flex: 2; min-width: 150px; } /* 名称列 */
.table-cell:nth-child(3) { flex: 0 0 100px; } /* 品牌列 */
.table-cell:nth-child(4) { flex: 0 0 80px; } /* 价格列 */
.table-cell:nth-child(5) { flex: 0 0 80px; } /* S码库存列 */
.table-cell:nth-child(6) { flex: 0 0 80px; } /* M码库存列 */
.table-cell:nth-child(7) { flex: 0 0 80px; } /* L码库存列 */
.table-cell:nth-child(8) { flex: 0 0 60px; } /* 状态列 */
.table-cell:nth-child(9) { flex: 0 0 180px; } /* 操作列 */

.clothing-name {
  color: #007bff;
  text-decoration: underline;
  cursor: pointer;
  z-index: 2;
  position: relative;
}

.clothing-name:hover {
  color: #0056b3;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: bold;
}

.status-active {
  background-color: #d4edda;
  color: #155724;
}

.status-inactive {
  background-color: #f8d7da;
  color: #721c24;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.button-row {
  display: flex;
  gap: 5px;
  flex-wrap: wrap;
}

button {
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  transition: background-color 0.2s;
  position: relative;
  z-index: 2;
}

.add-btn {
  background-color: #28a745;
  color: white;
}

.add-btn:hover {
  background-color: #218838;
}

.refresh-btn {
  background-color: #17a2b8;
  color: white;
}

.refresh-btn:hover {
  background-color: #138496;
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

.delete-btn {
  background-color: #dc3545;
  color: white;
}

.delete-btn:hover {
  background-color: #c82333;
}

.delete-btn:disabled {
  background-color: #6c757d;
  cursor: not-allowed;
}

/* 库存操作按钮样式 */
.stock-btn {
  padding: 6px 10px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
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

.form-section {
  margin-bottom: 20px;
  padding: 20px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
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

.form-group input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  box-sizing: border-box;
}

.form-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
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

.back-btn {
  background-color: #909399;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.back-btn:hover {
  background-color: #5a6268;
}

.error-message {
  color: #dc3545;
  font-size: 12px;
  margin-top: 5px;
}

.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 4px;
  border: 1px solid #dee2e6;
  position: relative;
  z-index: 1;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 15px;
}

.page-size-selector {
  display: flex;
  align-items: center;
  gap: 10px;
}

.page-size-selector label {
  font-weight: bold;
}

.page-size-selector select {
  padding: 6px 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.pagination button {
  padding: 8px 16px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  position: relative;
  z-index: 2;
}

.pagination button:hover:not(:disabled) {
  background-color: #0056b3;
}

.pagination button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.sortable {
  cursor: pointer;
  user-select: none;
}

.sortable:hover {
  background-color: #e6f7ff;
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

.modal-body .form-group {
  margin-bottom: 15px;
}

.modal-body .form-group label {
  display: block;
  margin-bottom: 5px;
  color: #606266;
}

.modal-body .form-group input,
.modal-body .form-group select {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  box-sizing: border-box;
}

@media (max-width: 768px) {
  .clothings-table {
    font-size: 14px;
  }

  .table-cell {
    padding: 8px;
  }

  .filter-section {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-group,
  .search-group,
  .sort-group {
    width: 100%;
  }

  .filter-group input,
  .filter-group select,
  .search-group input,
  .sort-group select {
    width: 100%;
  }

  .pagination {
    flex-direction: column;
    gap: 15px;
  }

  .pagination-controls {
    flex-wrap: wrap;
    justify-content: center;
    text-align: center;
  }

  /* 在小屏幕上调整列宽 */
  .table-cell:nth-child(1) { flex: 0 0 40px; }
  .table-cell:nth-child(2) { flex: 1; min-width: 100px; }
  .table-cell:nth-child(3) { flex: 0 0 80px; }
  .table-cell:nth-child(4) { flex: 0 0 60px; }
  .table-cell:nth-child(5) { flex: 0 0 80px; }
  .table-cell:nth-child(6) { flex: 0 0 80px; }
  .table-cell:nth-child(7) { flex: 0 0 80px; }
  .table-cell:nth-child(8) { flex: 0 0 80px; }
  .table-cell:nth-child(9) { flex: 0 0 180px; }
}
</style>
