<!-- src/components/matter/sections/BasisSection.vue -->
<template>
  <div class="basis-section">
    <div class="form-group">
      <label>经办依据:</label>
      <div class="basis-list-container">
        <div
          class="basis-item"
          v-for="(basis, index) in bases"
          :key="index"
        >
          <input
            type="text"
            :value="basis"
            @input="updateBasis(index, $event.target.value)"
            placeholder="请输入经办依据"
          >
          <button
            type="button"
            class="remove-basis-btn"
            @click="removeBasis(index)"
          >
            删除
          </button>
        </div>
        <button
          type="button"
          class="add-basis-btn"
          @click="addBasis"
        >
          + 新增法条
        </button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'BasisSection',
  props: {
    bases: {
      type: Array,
      default: () => []
    }
  },
  methods: {
    addBasis() {
      this.$emit('update:bases', [...this.bases, ''])
    },

    removeBasis(index) {
      const newBases = [...this.bases]
      newBases.splice(index, 1)
      this.$emit('update:bases', newBases)
    },

    updateBasis(index, value) {
      const newBases = [...this.bases]
      newBases[index] = value
      this.$emit('update:bases', newBases)
    }
  }
}
</script>

<style scoped>
.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
  color: #303133;
}

.basis-list-container {
  width: 100%;
}

.basis-item {
  display: flex;
  margin-bottom: 10px;
  gap: 10px;
}

.basis-item input {
  flex: 1;
}

.remove-basis-btn {
  background-color: #f56c6c;
  color: white;
  border: none;
  padding: 8px 12px;
  border-radius: 4px;
  cursor: pointer;
  white-space: nowrap;
}

.remove-basis-btn:hover {
  background-color: #f78989;
}

.add-basis-btn {
  background-color: #409eff;
  color: white;
  border: none;
  padding: 8px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.add-basis-btn:hover {
  background-color: #66b1ff;
}
</style>
