<!-- src/components/DbSidebar.vue -->
<template>
  <div class="sidebar-container">
    <div
      class="menu-item"
      :class="{ active: activeItem === 'dashboard' }"
      @click="selectItem('dashboard')"
    >
      {{ msg }}
    </div>
    <div
       class="menu-item"
       :class="{ active: activeItem === 'clothing' }"
       @click="selectItem('clothing')"
    >
       服装管理
    </div>
    <div
       class="menu-item"
       :class="{ active: activeItem === 'clothing-stock-record' }"
       @click="selectItem('clothing-stock-record')"
    >
      库存记录
    </div>
    <div
      class="menu-item"
      :class="{ active: activeItem === 'clothing-reports' }"
      @click="selectItem('clothing-reports')"
    >
      报表中心
    </div>
    <div
      class="menu-item"
      :class="{ active: activeItem === 'matter' }"
      @click="selectItem('matter')"
    >
      事项管理
    </div>
    <div
      class="menu-item"
      :class="{ active: activeItem === 'material' }"
      @click="selectItem('material')"
    >
      材料管理
    </div>
    <div
      class="menu-item"
      :class="{ active: activeItem === 'approval-diagram' }"
      @click="selectItem('approval-diagram')"
    >
      审批流程图
    </div>
    <div
      class="menu-item"
      :class="{ active: activeItem === 'business-diagram' }"
      @click="selectItem('business-diagram')"
    >
      业务经办流程图
    </div>
    <div
      class="menu-item"
      :class="{ active: activeItem === 'update-record' }"
      @click="selectItem('update-record')"
    >
      更新记录
    </div>
    <div
      class="menu-item"
      :class="{ active: activeItem === 'matter-version-compare' }"
      @click="selectItem('matter-version-compare')"
    >
      版本对比
    </div>
    <div
      class="menu-item"
      :class="{ active: activeItem === 'admin' }"
      @click="selectItem('admin')"
    >
      管理员
    </div>
  </div>
</template>

<script>
export default {
  name: 'db-sidebar',
  props: {
    // 接收父组件传递的当前选中菜单
    activeMenu: {
      type: String,
      default: 'dashboard'
    }
  },
  data() {
    return {
      msg: '仪表盘',
      activeItem: 'dashboard'
    }
  },
  watch: {
    // 监听activeMenu变化，同步更新activeItem
    activeMenu: {
      handler(newVal) {
        this.activeItem = newVal;
      },
      immediate: true
    }
  },
  methods: {
    selectItem(item) {
      // 如果点击的是当前已激活的项，则重新发送事件以确保回到列表视图
      if (this.activeItem === item) {
        this.$emit('menu-selected', item);
        return;
      }

      // 更新激活项并发送事件
      this.activeItem = item;
      this.$emit('menu-selected', item);
    }
  }
}
</script>

<style scoped>
.sidebar-container {
  display: block;
  width: 200px;
  height: 100%;
  background-color: #f5f7fa;
  border-right: 1px solid #d1d5da;
}

.menu-item {
  padding-left: 20px;
  height: 50px;
  line-height: 50px;
  cursor: pointer;
  border-bottom: 1px solid #d1d5da;
  transition: all 0.3s;
}

.menu-item.active {
  background-color: #409eff;
  color: white;
  font-weight: bold;
}

.menu-item:hover {
  background-color: #ecf5ff;
}

@media (max-width: 768px) {
  .sidebar-container {
    width: 100%;
    height: auto;
  }

  .menu-item {
    height: 40px;
    line-height: 40px;
    border-bottom: 1px solid #dcdfe6;
  }
}
</style>
