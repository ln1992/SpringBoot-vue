<!-- src/components/processdiagram/BusinessProcessDiagramDetail.vue -->
<template>
  <div class="diagram-detail-container">
    <div class="header">
      <h2>编辑业务流程图</h2>
    </div>

    <div class="diagram-detail-content">
      <form @submit.prevent="handleSubmit">
        <!-- 将ID和名称放在同一行 -->
        <div class="form-row">
          <div class="form-group">
            <label>ID:</label>
            <input type="text" v-model="form.id" disabled>
          </div>

          <div class="form-group">
            <label>名称 *</label>
            <input type="text" v-model="form.imageName" required>
          </div>
        </div>

        <!-- 将版本和显示名称放在同一行 -->
        <div class="form-row">
          <div class="form-group">
            <label>版本</label>
            <input type="text" v-model="form.version" placeholder="默认版本为1.0">
          </div>

          <div class="form-group">
            <label>显示名称</label>
            <input type="text" v-model="form.__name__" disabled>
          </div>
        </div>

        <div class="form-group">
          <label>上传图像</label>
          <input
            type="file"
            accept="image/*"
            @change="onImageChange"
          />
          <div v-if="form.imagePreview" class="image-preview">
            <img :src="form.imagePreview" alt="预览图像" />
            <p>新图像预览</p>
          </div>
          <div v-else-if="form.imageDataUrl" class="image-preview">
            <img :src="form.imageDataUrl" alt="当前图像" />
            <p>当前图像</p>
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
          <button type="button" @click="goBack" class="back-btn-form">返回</button>
          <button type="submit" class="save-btn">保存</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
import { processDiagramService } from '../../api';

// API端点常量
const API_GET_BY_ID = (id) => `/api/process-diagrams/business/${id}`;
const API_UPDATE = (id) => `/api/process-diagrams/business/${id}`;

export default {
  name: 'BusinessProcessDiagramDetail',
  props: {
    diagram: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      form: {
        id: null,
        imageName: '',
        version: 1,
        imageFile: null,
        valid: true,
        imageDataUrl: null
      },
      previewUrl: null,
      isEditing: false
    };
  },
  watch: {
    diagram: {
      handler(newVal) {
        this.initializeForm(newVal);
      },
      deep: true,
      immediate: true
    }
  },
  methods: {
    initializeForm(diagram) {
      if (diagram && diagram.id) {
        // 编辑模式
        this.isEditing = true;
        this.form = {
          id: diagram.id,
          imageName: diagram.imageName || '',
          __name__: diagram.__name__ || '',
          version: diagram.version || 1,
          valid: diagram.valid !== undefined ? diagram.valid : true,
          imageDataUrl: diagram.imageDataUrl || null,
          createdTime: diagram.createdTime || null,
          updateTime: diagram.updateTime || null
        };
        this.previewUrl = diagram.imageDataUrl || null;
      } else {
        // 新增模式
        this.isEditing = false;
        this.form = {
          id: null,
          imageName: '',
          __name__: '',
          version: 1,
          valid: true,
          imageDataUrl: null,
          createdTime: null,
          updateTime: null
        };
        this.previewUrl = null;
      }
    },

    // 返回列表
    goBack() {
      this.$emit('back');
    },

    // 格式化日期时间
    formatDateTime(dateString) {
      if (!dateString) return '';

      // 如果是已经格式化的字符串 (yyyy-MM-dd HH:mm:ss)
      if (typeof dateString === 'string' &&
        dateString.includes('-') &&
        dateString.includes(':') &&
        dateString.length === 19) { // "yyyy-MM-dd HH:mm:ss" 长度为19
        return dateString;
      }

      // 如果是 Date 对象或其他格式，则进行格式化
      const date = new Date(dateString);
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      const hours = String(date.getHours()).padStart(2, '0');
      const minutes = String(date.getMinutes()).padStart(2, '0');
      const seconds = String(date.getSeconds()).padStart(2, '0');

      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
    },

    onImageChange(event) {
      const file = event.target.files[0];
      if (file) {
        this.form.imageFile = file;
        // 生成预览
        const reader = new FileReader();
        reader.onload = (e) => {
          this.form.imagePreview = e.target.result;
        };
        reader.readAsDataURL(file);
      }
    },

    // 表单提交处理
    async handleSubmit() {
      if (!this.form.imageName.trim()) {
        alert('请输入流程图名称');
        return;
      }

      const formData = new FormData();
      formData.append('imageName', this.form.imageName);
      formData.append('version', this.form.version);
      
      // 修复：使用 'isValid' 而不是 'valid'
      formData.append('isValid', this.form.valid);
      
      if (this.form.imageFile) {
        formData.append('imageFile', this.form.imageFile);
      }

      try {
        let result;
        if (this.isEditing) {
          // 更新
          result = await processDiagramService.updateBusinessProcessDiagram(this.form.id, formData);
        } else {
          // 创建
          result = await processDiagramService.createBusinessProcessDiagram(formData);
        }

        this.$emit('diagram-updated', result);
        this.$emit('back');
      } catch (error) {
        console.error('保存业务流程图失败:', error);
        alert('保存失败: ' + error.message);
      }
    }
  }
};
</script>

<style scoped>
.diagram-detail-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  position: relative;
  z-index: 1; /* 降低z-index值，确保侧边栏可以覆盖 */
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h2 {
  color: #303133;
  margin: 0;
}

.diagram-detail-content {
  background-color: white;
  border-radius: 4px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  position: relative;
  z-index: 1; /* 降低z-index值，确保侧边栏可以覆盖 */
}

/* 表单样式 */
.form-group {
  margin-bottom: 15px;
}

.form-row {
  display: flex;
  gap: 15px;
}

.form-row .form-group {
  flex: 1;
  margin-bottom: 15px;
}

/* 调整ID字段的宽度 */
.form-row .form-group:first-child {
  flex: 0 0 120px; /* ID字段更窄 */
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
  color: #303133;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
}

.form-group input:disabled {
  background-color: #f5f7fa;
  cursor: not-allowed;
}

.image-preview {
  margin-top: 10px;
}

.image-preview img {
  max-width: 100%;
  max-height: 200px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.image-preview p {
  margin: 5px 0;
  font-size: 12px;
  color: #909399;
}

/* 时间信息样式 */
.time-info {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.time-details p {
  margin: 5px 0;
  color: #909399;
  font-size: 14px;
}

.form-actions {
  margin-top: 20px;
  text-align: right;
  position: relative;
  z-index: 1; /* 降低z-index值，确保侧边栏可以覆盖 */
}

.form-actions button {
  margin-left: 10px;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  position: relative;
  z-index: 1; /* 降低z-index值，确保侧边栏可以覆盖 */
}

.back-btn-form {
  background-color: #909399;
  color: white;
  border: none;
}

.back-btn-form:hover {
  background-color: #a0a3a9;
}

.save-btn {
  background-color: #409eff;
  color: white;
  border: none;
}

.save-btn:hover {
  background-color: #66b1ff;
}

@media (max-width: 768px) {
  .form-row {
    flex-direction: column;
    gap: 0;
  }
  
  .form-row .form-group:first-child {
    flex: 1;
  }
}
</style>
