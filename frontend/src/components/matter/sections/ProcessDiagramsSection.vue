<!-- src/components/matter/sections/ProcessDiagramsSection.vue -->
<template>
  <div class="process-diagrams-section">
    <!-- 流程图上传 - 并列显示 -->
    <div class="form-group">
      <div class="process-diagrams-container">
        <!-- 审批流程图 -->
        <div class="process-diagram-section">
          <div class="process-diagram-selection">
            <div class="diagram-select-wrapper" ref="approvalWrapper">
              <label class="diagram-type-label">审批流程图:</label>
              <input
                type="text"
                class="diagram-search-input"
                placeholder="搜索审批流程图..."
                :value="approvalDiagramSearchQuery"
                @input="onApprovalDiagramSearchInput"
                @focus="onApprovalDiagramSearchFocus"
                @blur="hideApprovalDropdown"
              >
              <div
                class="diagram-search-dropdown"
                :class="{ 'dropdown-up': showApprovalDropdownUp }"
                v-if="showApprovalDropdown && approvalDiagramSearchResults.length > 0"
              >
                <div
                  class="diagram-search-option"
                  v-for="diagram in approvalDiagramSearchResults"
                  :key="diagram.id"
                  @mousedown="selectApprovalDiagram(diagram)"
                >
                  {{ diagram.id }} - {{ diagram.imageName }}
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
            <div class="diagram-select-wrapper" ref="businessWrapper">
              <label class="diagram-type-label">业务经办流程图:</label>
              <input
                type="text"
                class="diagram-search-input"
                placeholder="搜索业务经办流程图..."
                :value="businessDiagramSearchQuery"
                @input="onBusinessDiagramSearchInput"
                @focus="onBusinessDiagramSearchFocus"
                @blur="hideBusinessDropdown"
              >
              <div
                class="diagram-search-dropdown"
                :class="{ 'dropdown-up': showBusinessDropdownUp }"
                v-if="showBusinessDropdown && businessDiagramSearchResults.length > 0"
              >
                <div
                  class="diagram-search-option"
                  v-for="diagram in businessDiagramSearchResults"
                  :key="diagram.id"
                  @mousedown="selectBusinessDiagram(diagram)"
                >
                  {{ diagram.id }} - {{ diagram.imageName }}
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
      businessDiagramSearchResults: [],
      showApprovalDropdown: false,
      showBusinessDropdown: false,
      showApprovalDropdownUp: false,
      showBusinessDropdownUp: false
    }
  },
  watch: {
    approvalDiagramId: {
      handler(newVal) {
        if (newVal) {
          const diagram = this.approvalDiagrams.find(d => d.id === newVal);
          if (diagram) {
            this.approvalDiagramSearchQuery = `${diagram.id} - ${diagram.imageName}`;
          }
        } else {
          this.approvalDiagramSearchQuery = '';
        }
      },
      immediate: true
    },
    businessDiagramId: {
      handler(newVal) {
        if (newVal) {
          const diagram = this.businessDiagrams.find(d => d.id === newVal);
          if (diagram) {
            this.businessDiagramSearchQuery = `${diagram.id} - ${diagram.imageName}`;
          }
        } else {
          this.businessDiagramSearchQuery = '';
        }
      },
      immediate: true
    }
  },
  methods: {
    onApprovalDiagramSearchInput(event) {
      const query = event.target.value;
      this.approvalDiagramSearchQuery = query;

      if (query.trim() === '') {
        this.approvalDiagramSearchResults = [...this.approvalDiagrams];
        this.showApprovalDropdown = true;
        return;
      }

      const filtered = this.approvalDiagrams.filter(diagram =>
        diagram.id.toString().includes(query) ||
        (diagram.imageName && diagram.imageName.toLowerCase().includes(query.toLowerCase()))
      );

      this.approvalDiagramSearchResults = filtered;
      this.showApprovalDropdown = true;
    },

    onApprovalDiagramSearchFocus(event) {
      this.checkDropdownDirection(event.target, 'approval');
      this.showApprovalDropdown = true;
      if (!this.approvalDiagramSearchQuery || this.approvalDiagramSearchQuery.trim() === '') {
        this.approvalDiagramSearchResults = [...this.approvalDiagrams];
      }
    },

    checkDropdownDirection(inputElement, type) {
      const wrapper = type === 'approval' ? this.$refs.approvalWrapper : this.$refs.businessWrapper;
      if (!wrapper) return;

      const rect = wrapper.getBoundingClientRect();
      const viewportHeight = window.innerHeight;
      const spaceBelow = viewportHeight - rect.bottom;
      const dropdownHeight = 200; // 预估下拉列表高度

      if (type === 'approval') {
        this.showApprovalDropdownUp = spaceBelow < dropdownHeight;
      } else {
        this.showBusinessDropdownUp = spaceBelow < dropdownHeight;
      }
    },

    hideApprovalDropdown() {
      setTimeout(() => {
        this.showApprovalDropdown = false;
      }, 200);
    },

    selectApprovalDiagram(diagram) {
      this.$emit('update:approval-diagram', diagram.id);
      this.approvalDiagramSearchQuery = `${diagram.id} - ${diagram.imageName}`;
      this.showApprovalDropdown = false;
    },

    clearApprovalDiagram() {
      this.$emit('clear:approval-diagram');
      this.approvalDiagramSearchQuery = '';
      this.approvalDiagramSearchResults = [];
      this.showApprovalDropdown = false;
    },

    onBusinessDiagramSearchInput(event) {
      const query = event.target.value;
      this.businessDiagramSearchQuery = query;

      if (query.trim() === '') {
        this.businessDiagramSearchResults = [...this.businessDiagrams];
        this.showBusinessDropdown = true;
        return;
      }

      const filtered = this.businessDiagrams.filter(diagram =>
        diagram.id.toString().includes(query) ||
        (diagram.imageName && diagram.imageName.toLowerCase().includes(query.toLowerCase()))
      );

      this.businessDiagramSearchResults = filtered;
      this.showBusinessDropdown = true;
    },

    onBusinessDiagramSearchFocus(event) {
      this.checkDropdownDirection(event.target, 'business');
      this.showBusinessDropdown = true;
      if (!this.businessDiagramSearchQuery || this.businessDiagramSearchQuery.trim() === '') {
        this.businessDiagramSearchResults = [...this.businessDiagrams];
      }
    },

    hideBusinessDropdown() {
      setTimeout(() => {
        this.showBusinessDropdown = false;
      }, 200);
    },

    selectBusinessDiagram(diagram) {
      this.$emit('update:business-diagram', diagram.id);
      this.businessDiagramSearchQuery = `${diagram.id} - ${diagram.imageName}`;
      this.showBusinessDropdown = false;
    },

    clearBusinessDiagram() {
      this.$emit('clear:business-diagram');
      this.businessDiagramSearchQuery = '';
      this.businessDiagramSearchResults = [];
      this.showBusinessDropdown = false;
    },

    getApprovalProcessDiagramUrl(id) {
      const diagram = this.approvalDiagrams.find(d => d.id === parseInt(id));
      return diagram ? diagram.imageDataUrl : '';
    },

    getBusinessProcessDiagramUrl(id) {
      const diagram = this.businessDiagrams.find(d => d.id === parseInt(id));
      return diagram ? diagram.imageDataUrl : '';
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

.diagram-search-dropdown.dropdown-up {
  top: auto;
  bottom: 100%;
  border-top: 1px solid #dcdfe6;
  border-bottom: none;
  border-radius: 4px 4px 0 0;
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
