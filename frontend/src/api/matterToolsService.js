// src/api/matterToolsService.js
import http from './http';
import { ENDPOINTS } from './endpoints';

class MatterToolsService {
  // 版本对比
  async compareVersions(version1, version2) {
    try {
      const response = await http.get(ENDPOINTS.MATTER_TOOLS_COMPARE, {
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
}

export default new MatterToolsService();