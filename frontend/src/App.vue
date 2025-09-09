<!-- src/App.vue -->
<template>
  <div class="wrapper">
    <db-header></db-header>
    <el-row class="container">
      <el-col :span="4" class="menu">
        <db-sidebar
          @menu-selected="handleMenuSelect"
          :key="sidebarKey"
        ></db-sidebar>
      </el-col>
      <el-col :span="20" class="content">
        <div v-if="selectedMenu === 'dashboard'">
          <db-filterinput></db-filterinput>
          <db-table></db-table>
        </div>
        <div v-else-if="selectedMenu === 'material'">
          <material-list ref="materialList"></material-list>
        </div>
        <!-- 添加事项管理视图 -->
        <div v-else-if="selectedMenu === 'matter'">
          <matter-list ref="matterList"></matter-list>
        </div>
        <!-- 添加审批流程图视图 -->
        <div v-else-if="selectedMenu === 'approval-diagram'">
          <approval-process-diagram-list ref="approvalDiagramList"></approval-process-diagram-list>
        </div>
        <!-- 添加业务流程图视图 -->
        <div v-else-if="selectedMenu === 'business-diagram'">
          <business-process-diagram-list ref="businessDiagramList"></business-process-diagram-list>
        </div>
      </el-col>
    </el-row>

    <el-row>
      <el-col :span="24">
        <footer class="footer">
          <db-footer></db-footer>
        </footer>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import DbHeader from './components/DbHeader.vue'
import DbSidebar from './components/DbSidebar.vue'
import DbFilterinput from './components/DbFilterinput.vue'
import DbTable from './components/DbTable.vue'
import DbFooter from './components/DbFooter.vue'
import MaterialList from './components/material/MaterialList.vue'
// 导入事项管理组件
import MatterList from './components/matter/MatterList.vue'
// 导入流程图管理组件
import ApprovalProcessDiagramList from './components/process-diagram/ApprovalProcessDiagramList.vue'
import BusinessProcessDiagramList from './components/process-diagram/BusinessProcessDiagramList.vue'
import ElRow from "element-ui/packages/row/src/row";

export default {
  name: 'app',
  components: {
    ElRow,
    DbHeader,
    DbSidebar,
    DbFilterinput,
    DbTable,
    DbFooter,
    MaterialList,
    // 注册事项管理组件
    MatterList,
    // 注册流程图管理组件
    ApprovalProcessDiagramList,
    BusinessProcessDiagramList
  },
  data() {
    return {
      selectedMenu: 'dashboard', // 默认显示dashboard
      sidebarKey: 0 // 用于强制刷新sidebar组件
    }
  },
  methods: {
    handleMenuSelect(menuItem) {
      // 如果点击的是当前已选中的菜单项，则强制刷新到列表视图
      if (this.selectedMenu === menuItem) {
        this.resetToListView(menuItem);
      }

      // 更新选中的菜单项
      this.selectedMenu = menuItem;
    },

    resetToListView(menuItem) {
      // 通过ref调用对应组件的重置方法
      this.$nextTick(() => {
        try {
          switch (menuItem) {
            case 'material':
              if (this.$refs.materialList && typeof this.$refs.materialList.resetToListView === 'function') {
                this.$refs.materialList.resetToListView();
              }
              break;
            case 'matter':
              if (this.$refs.matterList && typeof this.$refs.matterList.resetToListView === 'function') {
                this.$refs.matterList.resetToListView();
              }
              break;
            case 'approval-diagram':
              if (this.$refs.approvalDiagramList && typeof this.$refs.approvalDiagramList.resetToListView === 'function') {
                this.$refs.approvalDiagramList.resetToListView();
              }
              break;
            case 'business-diagram':
              if (this.$refs.businessDiagramList && typeof this.$refs.businessDiagramList.resetToListView === 'function') {
                this.$refs.businessDiagramList.resetToListView();
              }
              break;
            default:
              break;
          }
        } catch (error) {
          console.error('重置到列表视图时出错:', error);
        }
      });
    }
  }
}
</script>

<style>
element.style {
  background-color: rgb(10, 47, 88);
}

body {
  font-family: "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", "微软雅黑", Arial, sans-serif;
  margin: 0;
  display: flex;
  min-height: 100vh;
  flex-direction: column;
}

.el-menu, body, html {
  height: 100%;
}

.wrapper {
  position: relative;
}

footer, div {
  display: block;
}

.container {
  padding-top: 70px;
  flex: 1;
}

.container, .wrapper {
  height: 100%;
}

.menu {
  height: 100%;
  background-color: #eef1f6;
}

.content {
  padding-top: 25px;
  padding-right: 25px;
  padding-bottom: 125px;
  padding-left: 25px;
}

.footer {
  height: 120px;
  background-color: #324057;
  color: #a4aebd;
  width: 100%;
  z-index: 1000;
  margin-top: -120px;
  line-height: 1;
  font-size: 22px;
}
</style>
