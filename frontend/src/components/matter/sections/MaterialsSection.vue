<!-- src/components/matter/sections/MaterialsSection.vue -->
<template>
  <div class="materials-section">
    <div class="form-group">
      <label>关联材料:</label>
      <div class="materials-container">
        <!-- 材料表格 -->
        <div class="materials-table-container" v-if="materials.length > 0">
          <table class="materials-table">
            <thead>
            <tr>
              <th class="material-col-detail">材料明细</th>
              <th class="material-col-review">审核要点</th>
              <th class="material-col-auto-approval">智能秒批判断标准</th>
              <th class="material-col-shared">是否共享</th>
              <th class="material-col-source">材料来源</th>
              <th class="material-col-processing">办理方式说明</th>
              <th class="material-col-notification">适用告知承诺</th>
              <th class="material-col-action">操作</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="(material, index) in materials" :key="index">
              <td>
                <!-- 带搜索功能的材料选择 -->
                <div class="material-autocomplete">
                  <input
                    :ref="'materialInput' + index"
                    type="text"
                    :value="material.materialDetail"
                    @input="onMaterialInput(index, $event.target.value)"
                    @focus="showDropdown(index, $event.target.value)"
                    @blur="hideDropdown(index)"
                    placeholder="输入材料名称搜索或选择"
                    class="material-input"
                  />
                  <div
                    v-if="dropdownVisible[index]"
                    class="material-dropdown"
                  >
                    <div
                      v-for="item in filteredMaterials[index]"
                      :key="item.id"
                      @mousedown="selectMaterial(index, item)"
                      class="material-option"
                    >
                      {{ item.id }} - {{ item.materialDetail }}
                    </div>
                    <div v-if="filteredMaterials[index] && filteredMaterials[index].length === 0" class="no-results">
                      无匹配结果
                    </div>
                  </div>
                </div>
              </td>
              <td>
                <span class="readonly-field">{{ material.reviewPoint }}</span>
              </td>
              <td>
                <span class="readonly-field">{{ material.autoApprovalCriteria }}</span>
              </td>
              <td>
                <span class="readonly-field">{{ material.isShared ? '是' : '否' }}</span>
              </td>
              <td>
                <span class="readonly-field">{{ getMaterialSourceLabel(material.source || material.materialSource) }}</span>
              </td>
              <td>
                <span class="readonly-field">{{ material.processingMethodAndInfoAccess }}</span>
              </td>
              <td>
                <span class="readonly-field">{{ material.isEligibleForPromise ? '是' : '否' }}</span>
              </td>
              <td>
                <button
                  type="button"
                  class="remove-material-btn"
                  @click="removeMaterial(index)"
                >
                  删除
                </button>
              </td>
            </tr>
            </tbody>
          </table>
        </div>

        <button
          type="button"
          class="add-material-btn"
          @click="addMaterial"
        >
          + 添加材料
        </button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'MaterialsSection',
  props: {
    materials: {
      type: Array,
      default: () => []
    },
    materialsList: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      dropdownVisible: [],
      filteredMaterials: []
    }
  },
  created() {
    this.initializeState()
  },
  watch: {
    materials: {
      handler() {
        this.initializeState()
      },
      deep: true
    }
  },
  methods: {
    initializeState() {
      this.dropdownVisible = new Array(this.materials.length).fill(false)
      this.filteredMaterials = this.materials.map(() => [...this.materialsList])
    },

    addMaterial() {
      this.$emit('add-material')
      this.$nextTick(() => {
        this.dropdownVisible.push(false)
        this.filteredMaterials.push([...this.materialsList])
      })
    },

    removeMaterial(index) {
      this.$emit('remove-material', index)
      this.dropdownVisible.splice(index, 1)
      this.filteredMaterials.splice(index, 1)
    },

    onMaterialInput(index, value) {
      // 更新材料明细
      const material = { ...this.materials[index] }
      material.materialDetail = value
      this.$emit('update-material', index, material)

      // 过滤材料列表
      this.filterMaterials(index, value)
    },

    showDropdown(index, currentValue) {
      this.$set(this.dropdownVisible, index, true)
      // 显示所有材料作为初始建议或根据当前值过滤
      this.filterMaterials(index, currentValue || '')
    },

    hideDropdown(index) {
      setTimeout(() => {
        this.$set(this.dropdownVisible, index, false)
      }, 200)
    },

    filterMaterials(index, value) {
      if (!this.filteredMaterials[index]) {
        this.$set(this.filteredMaterials, index, [])
      }

      if (value.trim() === '') {
        this.$set(this.filteredMaterials, index, [...this.materialsList])
      } else {
        const filtered = this.materialsList.filter(item =>
          item.materialDetail.toLowerCase().includes(value.toLowerCase()) ||
          item.id.toString().includes(value)
        )
        this.$set(this.filteredMaterials, index, filtered)
      }
    },

    selectMaterial(index, selectedMaterial) {
      // 更新材料的所有字段
      const material = { ...this.materials[index] }
      material.id = selectedMaterial.id
      material.materialDetail = selectedMaterial.materialDetail
      material.reviewPoint = selectedMaterial.reviewPoint || ''
      material.autoApprovalCriteria = selectedMaterial.autoApprovalCriteria || ''
      material.isShared = selectedMaterial.isShared || false
      material.source = selectedMaterial.source || selectedMaterial.materialSource || ''
      material.processingMethodAndInfoAccess = selectedMaterial.processingMethodAndInfoAccess || ''
      material.isEligibleForPromise = selectedMaterial.isEligibleForPromise || false

      this.$emit('update-material', index, material)

      // 隐藏下拉列表
      this.$set(this.dropdownVisible, index, false)
    },

    getMaterialSourceLabel(sourceValue) {
      if (!sourceValue) return ''

      const sourceMap = {
        'PERSONAL_SUBMISSION': '申请人自备',
        'SYSTEM_AUTO_SHARED': '系统自动获取',
        'WANG_SHAN_PROCESSING': '网上办理'
      }

      return sourceMap[sourceValue] || sourceValue
    }
  }
}
</script>

<style scoped>
.materials-table-container {
  overflow-x: auto;
}

.materials-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 15px;
}

.materials-table th,
.materials-table td {
  border: 1px solid #dcdfe6;
  padding: 8px 12px;
  text-align: left;
  vertical-align: top;
}

.materials-table th {
  background-color: #f5f7fa;
  font-weight: bold;
  white-space: normal;
  word-wrap: break-word;
}

/* 材料列宽度定义 */
.material-col-detail {
  width: 18%;
}

.material-col-review {
  width: 18%;
}

.material-col-auto-approval {
  width: 10%;
}

.material-col-shared {
  width: 7%;
}

.material-col-source {
  width: 10%;
}

.material-col-processing {
  width: 15%;
}

.material-col-notification {
  width: 7%;
}

.material-col-action {
  width: 15%;
}

.materials-table td input,
.materials-table td select,
.materials-table td textarea {
  width: 100%;
  padding: 6px 8px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 13px;
  box-sizing: border-box;
}

.materials-table td textarea {
  min-height: 60px;
  resize: vertical;
}

.materials-table .remove-material-btn {
  background-color: #f56c6c;
  color: white;
  border: none;
  padding: 6px 10px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  white-space: nowrap;
}

.materials-table .remove-material-btn:hover {
  background-color: #f78989;
}

.add-material-btn {
  background-color: #409eff;
  color: white;
  border: none;
  padding: 8px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.add-material-btn:hover {
  background-color: #66b1ff;
}

/* 材料自动完成样式 */
.material-autocomplete {
  position: relative;
  width: 100%;
}

.material-input {
  width: 100%;
  padding: 6px 8px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 13px;
  box-sizing: border-box;
}

.material-dropdown {
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
  z-index: 1000;
}

.material-option {
  padding: 8px 12px;
  cursor: pointer;
}

.material-option:hover {
  background-color: #f5f7fa;
}

.no-results {
  padding: 8px 12px;
  color: #909399;
  font-style: italic;
}

.readonly-field {
  display: block;
  padding: 6px 8px;
  min-height: 32px;
  font-size: 13px;
  line-height: 1.4;
  word-wrap: break-word;
  word-break: break-all;
  background-color: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  color: #606266;
}

@media (max-width: 768px) {
  .materials-table-container {
    font-size: 12px;
  }

  .materials-table th,
  .materials-table td {
    padding: 6px 8px;
  }

  .materials-table td input,
  .materials-table td select,
  .materials-table td textarea {
    font-size: 12px;
    padding: 4px 6px;
  }

  .readonly-field {
    padding: 6px 8px;
    font-size: 13px;
  }

  .material-dropdown {
    max-height: 150px;
  }

  .material-option {
    padding: 6px 10px;
    font-size: 12px;
  }
}
</style>
