// src/api/matterToolsService.js
import http from './http';
import { ENDPOINTS } from './endpoints';

class MatterToolsService {
  // 版本对比
  async compareVersions(version1, version2) {
    try {
      const response = await http.get(ENDPOINTS.MATTER_TOOLS.COMPARE_VERSIONS, {
        params: {
          version1: version1,
          version2: version2
        }
      });
      return response.data;
    } catch (error) {
      console.error('API调用失败:', error);
      throw new Error(`版本对比失败: ${error.message}`);
    }
  }

  // 事项详细对比
  async compareMatters(oldMatterId, newMatterId) {
    try {
      // 构建参数对象，只包含非空值
      const params = {};
      if (oldMatterId != null) params.oldMatterId = oldMatterId;
      if (newMatterId != null) params.newMatterId = newMatterId;

      const response = await http.get(ENDPOINTS.MATTER_TOOLS.COMPARE_MATTERS, { params });
      return response.data;
    } catch (error) {
      console.error('API调用失败:', error);
      throw new Error(`事项对比失败: ${error.message}`);
    }
  }

  // 批量拷贝事项
  async batchCopyMatter() {
    try {
      const response = await http.post(ENDPOINTS.MATTER_TOOLS.BATCH_COPY);
      return response.data;
    } catch (error) {
      throw new Error(`批量拷贝事项失败: ${error.message}`);
    }
  }

  // 批量发布事项
  async batchPublishMatters() {
    try {
      const response = await http.post(ENDPOINTS.MATTER_TOOLS.BATCH_PUBLISH);
      return response.data;
    } catch (error) {
      throw new Error(`批量发布事项失败: ${error.message}`);
    }
  }

  // 导出事项目录
  async exportMattersCatalog(version = null) {
    try {
      const params = version ? { version } : {};
      const response = await http.get(ENDPOINTS.MATTER_TOOLS.EXPORT_CATALOG, {
        params,
        responseType: 'blob'
      });
      return response.data;
    } catch (error) {
      throw new Error(`导出事项目录失败: ${error.message}`);
    }
  }

  // 导出事项文档
  async exportMattersDocuments(version = null) {
    try {
      const params = version ? { version } : {};
      const response = await http.get(ENDPOINTS.MATTER_TOOLS.EXPORT_DOCUMENTS, {
        params,
        responseType: 'blob'
      });
      return response.data;
    } catch (error) {
      throw new Error(`导出事项文档失败: ${error.message}`);
    }
  }
}

export default new MatterToolsService();