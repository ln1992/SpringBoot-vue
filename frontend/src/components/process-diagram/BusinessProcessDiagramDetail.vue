<!-- src/components/process-diagram/BusinessProcessDiagramDetail.vue -->
<template>
  <div class="diagram-detail-container">
    <div class="header">
      <h2>{{ isEditMode ? '编辑业务流程图' : '新增业务流程图' }}</h2>
      <button v-if="isEditMode" class="api-btn" @click="openApiUrl" title="查看API数据">API</button>
    </div>

    <!-- 添加Tab页 -->
    <div class="tabs" v-if="isEditMode">
      <button
        :class="{ active: activeTab === 'detail' }"
        @click="activeTab = 'detail'"
      >
        流程图详情
      </button>
      <button
        :class="{ active: activeTab === 'updates' }"
        @click="activeTab = 'updates'"
      >
        更新记录
      </button>
    </div>

    <div class="diagram-detail-content">
      <!-- 流程图详情 Tab -->
      <div v-show="activeTab === 'detail'">
        <!-- 流程图拷贝选择区域 -->
        <div class="copy-section" v-if="!isEditMode">
          <div class="form-group">
            <label for="copyDiagram">拷贝流程图:</label>
            <div class="diagram-copy-container">
              <input
                type="text"
                v-model="diagramSearchQuery"
                placeholder="输入或选择要拷贝的流程图"
                class="diagram-search-input"
                @input="onDiagramSearchInput"
                @focus="showDiagramDropdown = true"
                @blur="hideDiagramDropdown"
              />
              <div
                v-if="showDiagramDropdown && allDiagrams && allDiagrams.length > 0"
                class="diagram-dropdown-list"
                @mousedown.prevent
              >
                <div
                  v-for="availableDiagram in filteredDiagrams"
                  :key="availableDiagram.id"
                  class="diagram-dropdown-item"
                  @mousedown="selectDiagramFromDropdown(availableDiagram)"
                >
                  {{ availableDiagram.__name__ }}
                </div>
                <div
                  v-if="filteredDiagrams && filteredDiagrams.length === 0"
                  class="no-results"
                >
                  无匹配结果
                </div>
              </div>
            </div>
          </div>
        </div>

        <form @submit.prevent="handleSubmit">
          <!-- 基本信息表单行：包含ID、名称、版本和图片类型 -->
          <div class="form-row">
            <div class="form-group">
              <label>ID:</label>
              <input type="text" v-model="form.id" disabled>
            </div>

            <div class="form-group">
              <label>名称 *</label>
              <input type="text" v-model="form.imageName" required>
            </div>

            <div class="form-group">
              <label>版本</label>
              <input type="text" v-model="form.version" placeholder="默认版本为1.0">
            </div>

            <div class="form-group">
              <label>图片类型</label>
              <input type="text" :value="form.imageType || '未指定'" disabled>
            </div>
          </div>

          <div class="form-group">
            <label>上传图像</label>
            <input
              type="file"
              accept="image/*"
              @change="onImageChange"
            />
            <div v-if="form.imagePreview || form.imageDataUrl" class="image-container">
              <div v-if="form.imagePreview" class="image-preview">
                <img :src="form.imagePreview" alt="预览图像" />
                <div class="image-label">新图像预览</div>
              </div>
              <div v-else-if="form.imageDataUrl" class="image-preview">
                <img :src="form.imageDataUrl" alt="当前图像" />
                <div class="image-label">当前图像</div>
              </div>
            </div>
          </div>

          <div class="form-group">
            <label>状态:</label>
            <select v-model="form.valid">
              <option :value="true">上线</option>
              <option :value="false">下线</option>
            </select>
          </div>

          <!-- 时间信息 -->
          <div class="time-info" v-if="form.createdTime || form.updateTime">
            <div class="form-group">
              <label>时间信息:</label>
              <div class="time-details">
                <p v-if="form.createdTime">创建时间: {{ formatDateTime(form.createdTime) }}</p>
                <p v-if="form.updateTime">更新时间: {{ formatDateTime(form.updateTime) }}</p>
              </div>
            </div>
          </div>

          <div class="form-actions">
            <button type="button" class="back-btn-form" @click="goBack">返回</button>
            <button type="button" class="save-btn" @click="handleSubmit">
              {{ isEditMode ? '保存' : '创建' }}
            </button>
          </div>
        </form>
      </div>

      <!-- 更新记录 Tab -->
      <div v-show="activeTab === 'updates'" v-if="isEditMode">
        <!-- 只在未选择记录时显示列表 -->
        <UpdateRecordList
          v-if="!selectedUpdateRecord"
          ref="updateRecordList"
          filter-entity-type="BusinessProcessDiagram"
          :filter-entity-id="form.id"
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
import { processDiagramService } from '../../api';
import UpdateRecordList from '../update-record/UpdateRecordList.vue';
import UpdateRecordDetail from '../update-record/UpdateRecordDetail.vue';

// API端点常量
const API_GET_BY_ID = (id) => `/api/process-diagrams/business/${id}`;
const API_UPDATE = (id) => `/api/process-diagrams/business/${id}`;

export default {
  name: 'BusinessProcessDiagramDetail',
  components: {
    UpdateRecordList,
    UpdateRecordDetail
  },
  props: {
    diagram: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      // 修复：将form数据移到data中，移除computed中的form
      form: {
        id: null,
        imageName: '',
        __name__: '',
        version: 1,
        valid: true,
        imageDataUrl: null,
        imageData: null,
        createdTime: null,
        updateTime: null,
        imageType: null
      },
      // 拷贝功能相关数据
      allDiagrams: [],
      diagramSearchQuery: '',
      showDiagramDropdown: false,
      filteredDiagrams: [],
      // Tab页相关数据
      activeTab: 'detail',
      // 更新记录详情相关数据
      selectedUpdateRecord: null
    };
  },
  computed: {
    isEditMode() {
      return !!this.diagram.id;
    },
    imageData() {
      // 如果有新的图片数据，优先使用新图片数据
      if (this.form.imageData) {
        return this.form.imageData;
      }
      // 否则返回null
      return null;
    }
  },
  watch: {
    diagram: {
      handler(newVal) {
        if (newVal) {
          // 当diagram属性变化时更新表单数据
          this.form = {
            id: newVal.id || null,
            imageName: newVal.imageName || '',
            __name__: newVal.__name__ || '',
            version: newVal.version !== undefined ? newVal.version : 1,
            valid: newVal.valid !== undefined ? newVal.valid : true,
            imageDataUrl: newVal.imageDataUrl || null,
            imageData: newVal.imageData || null,
            createdTime: newVal.createdTime || null,
            updateTime: newVal.updateTime || null,
            imageType: newVal.imageType || null
          };
        }
      },
      deep: true,
      immediate: true
    }
  },
  async mounted() {
    // 只在新增模式下加载所有流程图用于拷贝功能
    if (!this.isEditMode) {
      await this.loadAllDiagrams();
    }
  },
  methods: {
    // 加载所有流程图用于拷贝功能
    async loadAllDiagrams() {
      try {
        this.allDiagrams = await processDiagramService.getAllBusinessProcessDiagrams();
        // 限制初始显示数量
        this.filteredDiagrams = this.allDiagrams.slice(0, 100);
      } catch (error) {
        console.error('加载业务流程图列表失败:', error);
        // 不中断用户操作，只是不显示拷贝功能
        this.allDiagrams = [];
      }
    },

    // 处理流程图搜索输入
    onDiagramSearchInput() {
      if (!this.diagramSearchQuery) {
        // 限制显示数量，只显示前100个流程图
        this.filteredDiagrams = this.allDiagrams.slice(0, 100);
      } else {
        const query = this.diagramSearchQuery.toLowerCase();
        // 过滤并限制显示数量
        this.filteredDiagrams = this.allDiagrams
          .filter(diagram =>
            diagram.__name__ && diagram.__name__.toLowerCase().includes(query)
          )
          .slice(0, 100);
      }
      this.showDiagramDropdown = true;
    },

    // 显示流程图下拉列表
    showDiagramDropdown() {
      this.showDiagramDropdown = true;
    },

    // 隐藏流程图下拉列表
    hideDiagramDropdown() {
      // 延迟隐藏，确保点击选项时能正常触发
      setTimeout(() => {
        this.showDiagramDropdown = false;
      }, 200);
    },

    // 从下拉列表中选择流程图
    async selectDiagramFromDropdown(diagram) {
      this.diagramSearchQuery = diagram.__name__;
      this.showDiagramDropdown = false;

      try {
        // 获取选中的流程图详情
        const diagramDetail = await processDiagramService.getBusinessProcessDiagramById(diagram.id);
        this.copyDiagramData(diagramDetail);
      } catch (error) {
        console.error('拷贝流程图失败:', error);
        alert('拷贝流程图失败: ' + (error.message || '未知错误'));
      }
    },

    // 拷贝流程图数据到当前表单
    copyDiagramData(diagramData) {
      // 基本信息
      this.form.imageName = diagramData.imageName;
      this.form.version = diagramData.version;
      this.form.valid = diagramData.valid;
      this.form.imageDataUrl = diagramData.imageDataUrl;
      this.form.imageType = diagramData.imageType;
      // 拷贝图像数据
      this.form.imageData = diagramData.imageData;

      // 触发更新以确保视图刷新
      this.$forceUpdate();

      alert('流程图数据拷贝成功');
    },

    onImageChange(event) {
      const file = event.target.files[0];
      if (file) {
        const reader = new FileReader();
        reader.onload = (e) => {
          this.form.imagePreview = e.target.result;
        };
        reader.readAsDataURL(file);
      }
    },

    formatDateTime(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      return date.toLocaleString('zh-CN');
    },

    goBack() {
      this.$emit('back');
    },

    async handleSubmit() {
      const formData = new FormData();
      const fileInput = document.querySelector('input[type="file"]');

      // 添加表单数据
      formData.append('imageName', this.form.imageName || '');
      formData.append('version', this.form.version || 1);
      formData.append('isValid', this.form.valid !== undefined ? this.form.valid : true);

      // 添加文件（如果有的话）
      if (fileInput && fileInput.files[0]) {
        formData.append('imageFile', fileInput.files[0]);
      } else if (this.form.imageData && !this.isEditMode) {
        // 如果没有新文件但有拷贝的图像数据（新增模式），创建一个Blob并添加到表单中
        try {
          const response = await fetch(this.form.imageDataUrl);
          const blob = await response.blob();
          formData.append('imageFile', blob, 'copied_image.' + (this.form.imageType === 'PNG' ? 'png' : 'jpg'));
        } catch (error) {
          console.error('从imageDataUrl创建文件时出错:', error);
        }
      }

      try {
        let response;
        if (this.isEditMode) {
          // 编辑模式
          response = await processDiagramService.updateBusinessProcessDiagram(this.form.id, formData);
        } else {
          // 新增模式
          response = await processDiagramService.createBusinessProcessDiagram(formData);
        }

        this.$emit('diagram-updated', response);
        alert(this.isEditMode ? '业务流程图更新成功' : '业务流程图创建成功');
      } catch (error) {
        console.error('保存业务流程图出错:', error);
        alert('保存失败: ' + (error.message || '未知错误'));
      }
    },

    // 打开API网址查看数据
    openApiUrl() {
      if (this.form.id) {
        // 构造API URL，假设API端点为 /api/process-diagrams/business/{id}
        const apiUrl = `${window.location.origin}/api/process-diagrams/business/${this.form.id}`;
        window.open(apiUrl, '_blank');
      } else {
        alert('流程图ID不存在，无法打开API链接');
      }
    },

    // 处理查看更新记录事件
    handleViewRecord(record) {
      this.selectedUpdateRecord = record;
    }
  }
};
</script>

<style scoped>
.diagram-detail-container {
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

.diagram-detail-content {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.copy-section {
  margin-bottom: 20px;
  padding: 15px;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  background-color: #f9f9f9;
}

.copy-section .form-group {
  margin-bottom: 0;
}

.diagram-copy-container {
  position: relative;
  display: inline-block;
  width: 100%;
  max-width: 400px;
}

.diagram-search-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.diagram-dropdown-list {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  border: 1px solid #ddd;
  border-top: none;
  border-radius: 0 0 4px 4px;
  max-height: 200px;
  overflow-y: auto;
  z-index: 1000;
  box-shadow: 0 2px 5px rgba(0,0,0,0.1);
}

.diagram-dropdown-item {
  padding: 10px 12px;
  cursor: pointer;
  border-bottom: 1px solid #eee;
}

.diagram-dropdown-item:hover {
  background-color: #f5f5f5;
}

.diagram-dropdown-item:last-child {
  border-bottom: none;
}

.no-results {
  padding: 10px 12px;
  color: #999;
  font-style: italic;
}

.form-row {
  display: flex;
  gap: 15px;
  margin-bottom: 15px;
}

.form-row .form-group {
  flex: 1;
  margin-bottom: 0;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
  color: #555;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
}

.form-group input:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.image-preview {
  margin-top: 10px;
  text-align: center;
}

.image-preview img {
  max-width: 100%;
  max-height: 300px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.image-container {
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-top: 10px;
  margin-bottom: 20px; /* 添加底部边距防止遮挡下面的元素 */
}

.image-label {
  margin-top: 5px;
  font-weight: bold;
  color: #555;
}

.time-info {
  background-color: #f8f9fa;
  padding: 15px;
  border-radius: 4px;
  margin: 20px 0;
}

.time-details p {
  margin: 5px 0;
  color: #666;
  font-size: 14px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #eee;
  margin-bottom: 20px; /* 添加底部边距防止被遮挡 */
  position: relative; /* 添加定位上下文 */
  z-index: 10; /* 确保按钮在图片之上 */
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

.save-btn {
  background-color: #007bff;
  color: white;
}

.save-btn:hover {
  background-color: #0056b3;
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
  .form-row {
    flex-direction: column;
    gap: 0;
  }

  .header {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }
}
</style>
