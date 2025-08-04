<!-- src/components/material/MaterialList.vue -->
<template>
  <div class="material-list-container">
    <h2>Material 库</h2>

    <!-- 搜索和操作区域 -->
    <div class="controls">
      <el-input
        v-model="searchTerm"
        placeholder="搜索材料..."
        clearable
        style="width: 300px; margin-right: 10px;"
        @input="handleSearch"
      />
      <el-button @click="refreshMaterials" :loading="refreshing">
        {{ refreshing ? '刷新中...' : '刷新' }}
      </el-button>
    </div>

    <div v-loading="loading" class="material-content">
      <div v-if="error" class="error-message">
        <p>{{ error }}</p>
        <el-button @click="retryFetch" size="small" type="primary">重试</el-button>
      </div>
      <div v-else-if="!loading && paginatedMaterials.length === 0" class="empty-message">
        暂无材料数据
      </div>
      <div v-else>
        <div class="material-items">
          <material-item
            v-for="material in paginatedMaterials"
            :key="material.id"
            :material="material"
          ></material-item>
        </div>

        <!-- 分页 -->
        <div class="pagination-container" v-if="totalPages > 1">
          <el-pagination
            @current-change="handlePageChange"
            :current-page="currentPage"
            :page-size="itemsPerPage"
            :total="filteredMaterials.length"
            layout="prev, pager, next, jumper, ->, total"
            background
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import MaterialItem from './MaterialItem.vue';

export default {
  name: 'MaterialList',
  components: {
    MaterialItem
  },
  data() {
    return {
      materials: [],
      loading: false,
      refreshing: false,
      error: null,
      searchTerm: '',
      currentPage: 1,
      itemsPerPage: 10
    }
  },
  computed: {
    filteredMaterials() {
      if (!this.searchTerm) {
        return this.materials;
      }

      const term = this.searchTerm.toLowerCase();
      return this.materials.filter(material => {
        return (
          (material.materialDetails && material.materialDetails.toLowerCase().includes(term)) ||
          (material.reviewPoints && material.reviewPoints.toLowerCase().includes(term)) ||
          (material.autoApprovalCriteria && material.autoApprovalCriteria.toLowerCase().includes(term)) ||
          (material.materialSource && material.materialSource.toLowerCase().includes(term)) ||
          (material.processingMethodAndInfoAccess && material.processingMethodAndInfoAccess.toLowerCase().includes(term))
        );
      });
    },

    paginatedMaterials() {
      const startIndex = (this.currentPage - 1) * this.itemsPerPage;
      const endIndex = startIndex + this.itemsPerPage;
      return this.filteredMaterials.slice(startIndex, endIndex);
    },

    totalPages() {
      return Math.ceil(this.filteredMaterials.length / this.itemsPerPage);
    }
  },
  mounted() {
    this.fetchMaterials();
  },
  methods: {
    async fetchMaterials() {
      this.loading = true;
      this.error = null;

      try {
        // 使用 8000 端口
        const response = await fetch('http://127.0.0.1:8000/api/materials');

        console.log('Response status:', response.status);
        console.log('Response headers:', [...response.headers.entries()]);

        // 检查响应状态
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }

        // 检查响应内容类型
        const contentType = response.headers.get('content-type');
        console.log('Content-Type:', contentType);

        if (!contentType || !contentType.includes('application/json')) {
          const text = await response.text();
          console.error('Received non-JSON response:', text);
          throw new Error('服务器返回了非JSON格式的数据: ' + text.substring(0, 100) + '...');
        }

        this.materials = await response.json();
        this.currentPage = 1; // 重置到第一页
        console.log('Materials loaded:', this.materials);
      } catch (err) {
        this.error = '获取材料数据时出错: ' + err.message;
        console.error('Error fetching materials:', err);
      } finally {
        this.loading = false;
      }
    }

    ,

    retryFetch() {
      this.fetchMaterials();
    },

    handleSearch() {
      // 搜索时重置到第一页
      this.currentPage = 1;
    },

    handlePageChange(page) {
      this.currentPage = page;
    },

    async refreshMaterials() {
      this.refreshing = true;
      try {
        await this.fetchMaterials();
      } finally {
        this.refreshing = false;
      }
    }
  }
}
</script>

<style scoped>
.material-list-container {
  padding: 20px;
}

.controls {
  margin: 20px 0;
  display: flex;
  align-items: center;
}

.material-content {
  margin-top: 20px;
}

.material-items {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.error-message {
  color: #f56c6c;
  padding: 20px;
  text-align: center;
  border: 1px solid #f56c6c;
  border-radius: 4px;
  background-color: #fef0f0;
}

.error-message p {
  margin-bottom: 15px;
}

.empty-message {
  text-align: center;
  padding: 40px;
  color: #909399;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
