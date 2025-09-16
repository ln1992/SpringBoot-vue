// src/api/matterService.js
import http from './http';
import ENDPOINTS from './endpoints';

class MatterService {
  // 获取所有事项
  async getAllMatters() {
    try {
      const response = await http.get(ENDPOINTS.MATTERS, {
        params: {
          page: 0,
          size: 1000 // 设置一个较大的数值以获取所有事项
        }
      });
      // 如果返回的是分页数据，提取content字段
      if (response.data && response.data.content) {
        return response.data.content;
      }
      return response.data;
    } catch (error) {
      throw new Error(`获取事项列表失败: ${error.message}`);
    }
  }

  // 根据ID获取事项
  async getMatterById(id) {
    try {
      const response = await http.get(ENDPOINTS.MATTERS_BY_ID(id));
      return response.data;
    } catch (error) {
      throw new Error(`获取事项失败: ${error.message}`);
    }
  }

  // 创建事项
  async createMatter(matter) {
    try {
      const response = await http.post(ENDPOINTS.MATTERS, matter);
      return response.data;
    } catch (error) {
      throw new Error(`创建事项失败: ${error.message}`);
    }
  }

  // 更新事项
  async updateMatter(id, matter) {
    try {
      const response = await http.put(ENDPOINTS.MATTERS_BY_ID(id), matter);
      return response.data;
    } catch (error) {
      throw new Error(`更新事项失败: ${error.message}`);
    }
  }

  // 删除事项
  async deleteMatter(id) {
    try {
      const response = await http.delete(ENDPOINTS.MATTERS_BY_ID(id));
      return response.data;
    } catch (error) {
      throw new Error(`删除事项失败: ${error.message}`);
    }
  }

  // 激活事项
  async activateMatter(id) {
    try {
      const response = await http.put(ENDPOINTS.MATTERS_ACTIVATE(id));
      return response.data;
    } catch (error) {
      throw new Error(`激活事项失败: ${error.message}`);
    }
  }

  // 停用事项
  async deactivateMatter(id) {
    try {
      const response = await http.put(ENDPOINTS.MATTERS_DEACTIVATE(id));
      return response.data;
    } catch (error) {
      throw new Error(`停用事项失败: ${error.message}`);
    }
  }

  // 发布事项
  async publishMatter(id) {
    try {
      const response = await http.put(ENDPOINTS.MATTERS_PUBLISH(id));
      return response.data;
    } catch (error) {
      throw new Error(`发布事项失败: ${error.message}`);
    }
  }

  // 取消发布事项
  async unpublishMatter(id) {
    try {
      const response = await http.put(ENDPOINTS.MATTERS_UNPUBLISH(id));
      return response.data;
    } catch (error) {
      throw new Error(`取消发布事项失败: ${error.message}`);
    }
  }

  // 导出事项目录
  async exportMattersCatalog(version = null) {
    try {
      const params = version ? { version } : {};
      const response = await http.get(`${ENDPOINTS.MATTERS}/export/catalog`, {
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
      const response = await http.get(`${ENDPOINTS.MATTERS}/export/documents`, {
        params,
        responseType: 'blob'
      });
      return response.data;
    } catch (error) {
      throw new Error(`导出事项文档失败: ${error.message}`);
    }
  }

  // 获取所有版本号
  async getAllVersions() {
    try {
      const response = await http.get(`${ENDPOINTS.MATTERS}/versions`);
      return response.data;
    } catch (error) {
      throw new Error(`获取版本列表失败: ${error.message}`);
    }
  }
}

export default new MatterService();
