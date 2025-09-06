<!-- src/components/processdiagram/ApprovalProcessDiagramDetail.vue -->
<template>
  <div class="diagram-detail-container">
    <div class="header">
      <h2>编辑审批流程图</h2>
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
            <option :value="true">已上线</option>
            <option :value="false">已下线</option>
          </select>
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
const API_BASE_URL = 'http://localhost:8000/api/process-diagrams/approval';
const API_UPDATE = (id) => `${API_BASE_URL}/${id}`;

export default {
  name: 'ApprovalProcessDiagramDetail',
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
        version: '',
        __name__: '',
        imageFile: null,
        imagePreview: null,
        imageDataUrl: null,
        valid: true
      }
    };
  },
  created() {
    // 初始化表单数据
    this.resetForm();
  },
  methods: {
    resetForm() {
      this.form = {
        id: this.diagram.id,
        imageName: this.diagram.imageName,
        version: this.diagram.version || '',
        __name__: this.diagram.__name__ || '',
        imageFile: null,
        imagePreview: null,
        imageDataUrl: this.diagram.imageDataUrl,
        valid: this.diagram.valid
      };
    },

    // 返回列表
    goBack() {
      this.$emit('back');
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
      try {
        const formData = new FormData();
        formData.append('imageName', this.form.imageName);
        if (this.form.version) {
          // 确保传递的是数字类型
          const versionValue = parseInt(this.form.version);
          if (!isNaN(versionValue) && versionValue > 0) {
            formData.append('version', versionValue);
          }
        }
        // 修复：使用 'isValid' 而不是 'valid'
        formData.append('isValid', this.form.valid);

        // 如果重新上传了图片
        if (this.form.imageFile) {
          formData.append('imageFile', this.form.imageFile);
        }

        const response = await fetch(API_UPDATE(this.form.id), {
          method: 'PUT',
          body: formData
        });

        if (response.ok) {
          const updatedDiagram = await response.json();

          // 重建图像数据URL
          if (updatedDiagram.imageData && updatedDiagram.imageType) {
            const contentTypes = {
              'JPEG': 'image/jpeg',
              'PNG': 'image/png',
              'GIF': 'image/gif',
              'BMP': 'image/bmp',
              'SVG': 'image/svg+xml'
            };
            const contentType = contentTypes[updatedDiagram.imageType] || 'image/jpeg';
            updatedDiagram.imageDataUrl = `data:${contentType};base64,${updatedDiagram.imageData}`;
          }

          this.$emit('diagram-updated', updatedDiagram);
          alert('审批流程图更新成功');
        } else {
          const errorText = await response.text();
          alert('更新失败: ' + response.status + ' - ' + errorText);
        }
      } catch (error) {
        console.error('保存审批流程图出错:', error);
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
  z-index: 1001;
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
  z-index: 1001;
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

.form-actions {
  margin-top: 20px;
  text-align: right;
  position: relative;
  z-index: 1002;
}

.form-actions button {
  margin-left: 10px;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  position: relative;
  z-index: 1003;
}

.form-actions button[type="button"] {
  background-color: #909399;
  color: white;
  border: none;
}

.form-actions button[type="button"]:hover {
  background-color: #a6a9ad;
}

.back-btn-form {
  background-color: #909399 !important;
  color: white !important;
  border: none !important;
}

.back-btn-form:hover {
  background-color: #a6a9ad !important;
}

.save-btn {
  background-color: #67c23a;
  color: white;
  border: none;
}

.save-btn:hover {
  background-color: #85ce61;
}

@media (max-width: 768px) {
  .diagram-detail-container {
    padding: 10px;
  }

  .form-row {
    flex-direction: column;
    gap: 0;
  }

  /* 在移动端恢复ID字段的默认宽度 */
  .form-row .form-group:first-child {
    flex: 1;
  }

  .form-actions {
    display: flex;
    justify-content: space-between;
  }

  .form-actions button {
    flex: 1;
    margin: 0 5px;
  }
}
</style>
