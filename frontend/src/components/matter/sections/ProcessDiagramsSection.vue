<!-- src/components/matter/sections/ProcessDiagramsSection.vue -->
<template>
  <div class="process-diagrams-section">
    <!-- 流程图上传 - 并列显示 -->
    <div class="form-group">
      <div class="process-diagrams-container">
        <!-- 审批流程图 -->
        <div class="process-diagram-section">
          <div class="process-diagram-selection">
            <div class="diagram-select-wrapper">
              <label class="diagram-type-label">审批流程图:</label>
              <input
                type="text"
                class="diagram-search-input"
                placeholder="搜索审批流程图..."
                v-model="approvalDiagramSearchQuery"
                @input="onApprovalDiagramSearchInput($event.target.value)"
                @focus="onApprovalDiagramSearchFocus"
              >
              <div
                class="diagram-search-dropdown"
                v-if="approvalDiagramSearchResults.length > 0"
              >
                <div
                  class="diagram-search-option"
                  v-for="diagram in approvalDiagramSearchResults"
                  :key="diagram.id"
                  @click="selectApprovalDiagram(diagram)"
                >
                  {{ diagram.id }} - {{ diagram.__name__ }}
                </div>
              </div>
            </div>
            <button
              v-if="approvalDiagramId"
              type="button"
              class="clear-selection-btn"
              @click="clearApprovalDiagram"
            >
              清除
            </button>
          </div>
          <div v-if="approvalDiagramId" class="image-preview">
            <img
              :src="getApprovalProcessDiagramUrl(approvalDiagramId)"
              alt="审批流程图预览"
            />
          </div>
        </div>

        <!-- 业务流程图 -->
        <div class="process-diagram-section">
          <div class="process-diagram-selection">
            <div class="diagram-select-wrapper">
              <label class="diagram-type-label">业务经办流程图:</label>
              <input
                type="text"
                class="diagram-search-input"
                placeholder="搜索业务经办流程图..."
                v-model="businessDiagramSearchQuery"
                @input="onBusinessDiagramSearchInput($event.target.value)"
                @focus="onBusinessDiagramSearchFocus"
              >
              <div
                class="diagram-search-dropdown"
                v-if="businessDiagramSearchResults.length > 0"
              >
                <div
                  class="diagram-search-option"
                  v-for="diagram in businessDiagramSearchResults"
                  :key="diagram.id"
                  @click="selectBusinessDiagram(diagram)"
                >
                  {{ diagram.id }} - {{ diagram.__name__ }}
                </div>
              </div>
            </div>
            <button
              v-if="businessDiagramId"
              type="button"
              class="clear-selection-btn"
              @click="clearBusinessDiagram"
            >
              清除
            </button>
          </div>
          <div v-if="businessDiagramId" class="image-preview">
            <img
              :src="getBusinessProcessDiagramUrl(businessDiagramId)"
              alt="业务经办流程图预览"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ProcessDiagramsSection',
  props: {
    approvalDiagramId: {
      type: [Number, String, null],
      default: null
    },
    businessDiagramId: {
      type: [Number, String, null],
      default: null
    },
    approvalDiagrams: {
      type: Array,
      default: () => []
    },
    businessDiagrams: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      approvalDiagramSearchQuery: '',
      businessDiagramSearchQuery: '',
      approvalDiagramSearchResults: [],
      businessDiagramSearchResults: []
    }
  },
  watch: {
    approvalDiagramId: {
      handler(newVal) {
        if (newVal) {
          const diagram = this.approvalDiagrams.find(d => d.id === newVal)
          if (diagram) {
            this.approvalDiagramSearchQuery = `${diagram.id} - ${diagram.__name__}`
          }
        } else {
          this.approvalDiagramSearchQuery = ''
        }
      },
      immediate: true
    },
    businessDiagramId: {
      handler(newVal) {
        if (newVal) {
          const diagram = this.businessDiagrams.find(d => d.id === newVal)
          if (diagram) {
            this.businessDiagramSearchQuery = `${diagram.id} - ${diagram.__name__}`
          }
        } else {
          this.businessDiagramSearchQuery = ''
        }
      },
      immediate: true
    }
  },
  methods: {
    // 处理审批流程图搜索输入
    onApprovalDiagramSearchInput(query) {
      if (query.trim() === '') {
        this.approvalDiagramSearchResults = []
        return
      }

      // 过滤流程图列表
      const filtered = this.approvalDiagrams.filter(diagram =>
        diagram.id.toString().includes(query) ||
        (diagram.__name__ && diagram.__name__.includes(query))
      )

      this.approvalDiagramSearchResults = filtered
    },

    // 审批流程图搜索框获得焦点时显示所有流程图
    onApprovalDiagramSearchFocus() {
      // 如果搜索框为空，显示所有流程图
      if (!this.approvalDiagramSearchQuery || this.approvalDiagramSearchQuery.trim() === '') {
        this.approvalDiagramSearchResults = [...this.approvalDiagrams]
      }
    },

    // 选择审批流程图
    selectApprovalDiagram(diagram) {
      this.$emit('update:approval-diagram', diagram.id)
      this.approvalDiagramSearchQuery = `${diagram.id} - ${diagram.__name__}`
      this.approvalDiagramSearchResults = []
    },

    // 清除审批流程图选择
    clearApprovalDiagram() {
      this.$emit('clear:approval-diagram')
      this.approvalDiagramSearchQuery = ''
      this.approvalDiagramSearchResults = []
    },

    // 处理业务流程图搜索输入
    onBusinessDiagramSearchInput(query) {
      if (query.trim() === '') {
        this.businessDiagramSearchResults = []
        return
      }

      // 过滤流程图列表
      const filtered = this.businessDiagrams.filter(diagram =>
        diagram.id.toString().includes(query) ||
        (diagram.__name__ && diagram.__name__.includes(query))
      )

      this.businessDiagramSearchResults = filtered
    },

    // 业务流程图搜索框获得焦点时显示所有流程图
    onBusinessDiagramSearchFocus() {
      // 如果搜索框为空，显示所有流程图
      if (!this.businessDiagramSearchQuery || this.businessDiagramSearchQuery.trim() === '') {
        this.businessDiagramSearchResults = [...this.businessDiagrams]
      }
    },

    // 选择业务流程图
    selectBusinessDiagram(diagram) {
      this.$emit('update:business-diagram', diagram.id)
      this.businessDiagramSearchQuery = `${diagram.id} - ${diagram.__name__}`
      this.businessDiagramSearchResults = []
    },

    // 清除业务流程图选择
    clearBusinessDiagram() {
      this.$emit('clear:business-diagram')
      this.businessDiagramSearchQuery = ''
      this.businessDiagramSearchResults = []
    },

    // 获取审批流程图URL
    getApprovalProcessDiagramUrl(id) {
      const diagram = this.approvalDiagrams.find(d => d.id === id)
      return diagram ? diagram.imageDataUrl : ''
    },

    // 获取业务流程图URL
    getBusinessProcessDiagramUrl(id) {
      const diagram = this.businessDiagrams.find(d => d.id === id)
      return diagram ? diagram.imageDataUrl : ''
    }
  }
}
</script>

<style scoped>
.form-group {
  margin-bottom: 15px;
}

.process-diagrams-container {
  display: flex;
  gap: 20px;
}

.process-diagram-section {
  flex: 1;
}

.process-diagram-selection {
  display: flex;
  gap: 10px;
}

.diagram-select-wrapper {
  flex: 1;
  position: relative;
}

.diagram-type-label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
  color: #303133;
}

.diagram-search-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
}

.diagram-search-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  border: 1px solid #dcdfe6;
  border-top: none;
  border-radius: 0 0 4px 4px;
  max-height: 200px;
  overflow-y: auto;
  z-index: 100;
}

.diagram-search-option {
  padding: 8px 12px;
  cursor: pointer;
}

.diagram-search-option:hover {
  background-color: #f5f7fa;
}

.clear-selection-btn {
  background-color: #f56c6c;
  color: white;
  border: none;
  padding: 8px 12px;
  border-radius: 4px;
  cursor: pointer;
  white-space: nowrap;
}

.clear-selection-btn:hover {
  background-color: #f78989;
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

@media (max-width: 768px) {
  .process-diagrams-container {
    flex-direction: column;
    gap: 15px;
  }

  .process-diagram-selection {
    flex-direction: column;
  }
}
</style>
