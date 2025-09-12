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
              <th class="material-col-processing">办理方式及材料信息获取方式说明</th>
              <th class="material-col-notification">是否适用告知承诺</th>
              <th class="material-col-action">操作</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="(material, index) in materials" :key="index">
              <td>
                <!-- materialDetail 字段改为可输入的下拉选择 -->
                <div class="material-detail-autocomplete-container">
                  <div class="material-detail-input-wrapper">
                    <input
                      :ref="el => { materialInputs[index] = el }"
                      type="text"
                      v-model="materialSearchQueries[index]"
                      placeholder="输入或选择材料明细"
                      class="material-detail-input"
                      @input="onMaterialSearchInput(index, $event)"
                      @focus="showMaterialDropdown(index)"
                      @blur="hideMaterialDropdown(index)"
                    />
                    <div
                      v-if="showMaterialDropdownList[index]"
                      class="material-dropdown-list"
                      @mousedown.prevent
                    >
                      <div
                        v-for="availableMaterial in filteredMaterialsList[index]"
                        :key="availableMaterial.id"
                        class="material-dropdown-item"
                        @mousedown="selectMaterialFromDropdown(index, availableMaterial)"
                      >
                        {{ availableMaterial.id }} - {{ availableMaterial.__name__ }}
                      </div>
                      <div
                        v-if="filteredMaterialsList[index] && filteredMaterialsList[index].length === 0"
                        class="no-results"
                      >
                        无匹配结果
                      </div>
                    </div>
                  </div>
                </div>
              </td>
              <td>
                <!-- reviewPoint 字段改为只读显示 -->
                <span class="readonly-field">{{ material.reviewPoint }}</span>
              </td>
              <td>
                <!-- autoApprovalCriteria 字段改为只读显示 -->
                <span class="readonly-field">{{ material.autoApprovalCriteria }}</span>
              </td>
              <td>
                <!-- shared 字段改为只读显示 -->
                <span class="readonly-field">{{ material.shared ? '是' : '否' }}</span>
              </td>
              <td>
                <!-- materialSource 字段改为只读显示 -->
                <span class="readonly-field">{{ getMaterialSourceLabel(material.materialSource) }}</span>
              </td>
              <td>
                <!-- processingMethodAndInfoAccess 字段改为只读显示 -->
                <span class="readonly-field">{{ getProcessingMethodDisplay(material.processingMethodAndInfoAccess) }}</span>
              </td>
              <td>
                <!-- eligibleForPromise 字段改为只读显示 -->
                <span class="readonly-field">{{ material.eligibleForPromise ? '是' : '否' }}</span>
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
      showMaterialDropdownList: [], // 控制材料下拉列表显示
      filteredMaterialsList: [], // 过滤后的材料列表
      materialSearchQueries: [], // 材料搜索查询文本
      materialInputs: [] // 材料输入框引用
    }
  },
  created() {
    this.initializeState()
  },
  watch: {
    materials: {
      handler() {
        this.$nextTick(() => {
          this.initializeState()
        })
      },
      deep: true
    }
  },
  methods: {
    initializeState() {
      this.showMaterialDropdownList = new Array(this.materials.length).fill(false)
      this.filteredMaterialsList = this.materials.map(() => [...this.materialsList])
      // 初始化搜索查询文本为材料的 __name__
      this.materialSearchQueries = this.materials.map(material =>
        material.__name__ ? `${material.id} - ${material.__name__}` : (material.materialDetail || ''))
      this.materialInputs = new Array(this.materials.length).fill(null)
    },

    addMaterial() {
      this.$emit('add-material')
      this.$nextTick(() => {
        this.showMaterialDropdownList.push(false)
        this.filteredMaterialsList.push([...this.materialsList])
        this.materialSearchQueries.push('')
        this.materialInputs.push(null)
      })
    },

    removeMaterial(index) {
      this.$emit('remove-material', index)
      this.showMaterialDropdownList.splice(index, 1)
      this.filteredMaterialsList.splice(index, 1)
      this.materialSearchQueries.splice(index, 1)
      this.materialInputs.splice(index, 1)
    },

    // 处理材料搜索输入
    onMaterialSearchInput(index, event) {
      const query = event.target.value;

      // 更新搜索查询文本
      this.$set(this.materialSearchQueries, index, query);

      // 触发过滤
      this.filterMaterials(index, query);
    },

    // 显示材料下拉列表
    showMaterialDropdown(index) {
      this.$set(this.showMaterialDropdownList, index, true)
      // 显示所有材料作为初始建议
      this.$set(this.filteredMaterialsList, index, [...this.materialsList])
    },

    // 隐藏材料下拉列表
    hideMaterialDropdown(index) {
      setTimeout(() => {
        this.$set(this.showMaterialDropdownList, index, false)
      }, 200) // 延迟隐藏，确保点击选项时能正常选择
    },

    // 过滤材料列表
    filterMaterials(index, value) {
      if (!this.filteredMaterialsList[index]) {
        this.$set(this.filteredMaterialsList, index, [])
      }

      if (value.trim() === '') {
        this.$set(this.filteredMaterialsList, index, [...this.materialsList])
      } else {
        // 统一转换为小写进行比较
        const searchValue = value.toLowerCase()
        const filtered = this.materialsList.filter(item =>
          (item.__name__ && item.__name__.toLowerCase().includes(searchValue)) ||
          (item.id && item.id.toString().includes(searchValue)) ||
          (item.materialDetail && item.materialDetail.toLowerCase().includes(searchValue))
        )
        this.$set(this.filteredMaterialsList, index, filtered)
      }
    },

    // 从下拉列表选择材料
    selectMaterialFromDropdown(index, selectedMaterial) {
      // 更新材料的其他字段
      const updatedMaterial = { 
        ...this.materials[index],
        materialDetail: selectedMaterial.materialDetail,
        reviewPoint: selectedMaterial.reviewPoint || '',
        autoApprovalCriteria: selectedMaterial.autoApprovalCriteria || '',
        shared: selectedMaterial.shared || false,
        materialSource: selectedMaterial.materialSource || '',
        processingMethodAndInfoAccess: selectedMaterial.processingMethodAndInfoAccess || '',
        eligibleForPromise: selectedMaterial.eligibleForPromise || false,
        id: selectedMaterial.id,
        __name__: selectedMaterial.__name__ || ''
      };

      this.$emit('update-material', index, updatedMaterial)

      // 更新搜索查询文本
      this.$set(this.materialSearchQueries, index,
        selectedMaterial.__name__ ? `${selectedMaterial.id} - ${selectedMaterial.__name__}` : selectedMaterial.materialDetail)

      // 隐藏下拉列表
      this.$set(this.showMaterialDropdownList, index, false)

      // 聚焦到下一个字段或保持焦点在当前输入框
      this.$nextTick(() => {
        const input = this.materialInputs[index]
        if (input) {
          input.focus()
        }
      })
    },

    getMaterialSourceLabel(sourceValue) {
      if (!sourceValue) return ''

      const sourceMap = {
        'PERSONAL_SUBMISSION': '申请人自备',
        'SYSTEM_AUTO_SHARED': '系统自动获取',
        'WANG_SHAN_PROCESSING': '网上办理'
      }

      return sourceMap[sourceValue] || sourceValue
    },

    /**
     * 将processingMethodAndInfoAccess枚举值转换为可读的中文标签
     * @param {string} methodValue - 枚举值
     * @returns {string} - 可读的中文标签或原始值
     */
    getProcessingMethodDisplay(methodValue) {
      if (!methodValue) return ''
      
      const methodMap = {
        'ONLINE_PROCESSING': '网上办理，线上提交材料',
        'SYSTEM_AUTO_WITH_FALLBACK': '系统自动获取，如数据不全则需申请者提交',
        'PAPER_CERTIFICATE': '纸质证书需申请者提交'
      }

      // 如果找不到匹配项，返回原始值并添加"（未知类型）"标识
      return methodMap[methodValue] || `${methodValue}（未知类型）`
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
  width: 25%; /* 增加材料明细列宽度 */
}

.material-col-review {
  width: 15%;
}

.material-col-auto-approval {
  width: 8%;
}

.material-col-shared {
  width: 7%; /* 是否共享列宽度调整为7% */
}

.material-col-source {
  width: 8%;
}

.material-col-processing {
  width: 15%;
}

.material-col-notification {
  width: 10%; /* 是否适用告知承诺列宽度调整为10% */
}

.material-col-action {
  width: 8%; /* 操作列进一步缩窄 */
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

/* 材料明细自动完成样式 */
.material-detail-autocomplete-container {
  position: relative;
  width: 100%;
}

.material-detail-input-wrapper {
  position: relative;
}

.material-detail-input {
  width: 100%;
  padding: 6px 8px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 13px;
  box-sizing: border-box;
}

.material-dropdown-list {
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

.material-dropdown-item {
  padding: 8px 12px;
  cursor: pointer;
}

.material-dropdown-item:hover {
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

  .material-dropdown-list {
    max-height: 150px;
  }

  .material-dropdown-item {
    padding: 6px 10px;
    font-size: 12px;
  }

  /* 调整移动端列宽 */
  .material-col-detail {
    width: 25%;
  }

  .material-col-shared {
    width: 7%;
  }

  .material-col-notification {
    width: 10%;
  }

  .material-col-action {
    width: 8%;
  }
}
</style>