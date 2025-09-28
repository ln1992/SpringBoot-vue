import http from './http';
import { ENDPOINTS } from './endpoints';

class MaterialService {
  // 获取所有材料
  async getAllMaterials() {
    try {
      const response = await http.get(ENDPOINTS.MATERIAL.BASE);
      return response.data;
    } catch (error) {
      throw new Error(`获取材料列表失败: ${error.message}`);
    }
  }

  // 根据ID获取材料
  async getMaterialById(id) {
    try {
      const response = await http.get(`${ENDPOINTS.MATERIAL.BASE}/${id}`);
      return response.data;
    } catch (error) {
      throw new Error(`获取材料失败: ${error.message}`);
    }
  }

  // 创建材料
  async createMaterial(material) {
    try {
      const response = await http.post(ENDPOINTS.MATERIAL.BASE, material);
      return response.data;
    } catch (error) {
      throw new Error(`创建材料失败: ${error.message}`);
    }
  }

  // 更新材料
  async updateMaterial(id, material) {
    try {
      const response = await http.put(`${ENDPOINTS.MATERIAL.BASE}/${id}`, material);
      return response.data;
    } catch (error) {
      throw new Error(`更新材料失败: ${error.message}`);
    }
  }

  // 删除材料
  async deleteMaterial(id) {
    try {
      const response = await http.delete(`${ENDPOINTS.MATERIAL.BASE}/${id}`);
      return response.data;
    } catch (error) {
      throw new Error(`删除材料失败: ${error.message}`);
    }
  }

  // 激活材料
  async activateMaterial(id) {
    try {
      const response = await http.put(`${ENDPOINTS.MATERIAL.BASE}/${id}/activate`);
      return response.data;
    } catch (error) {
      throw new Error(`激活材料失败: ${error.message}`);
    }
  }

  // 停用材料
  async deactivateMaterial(id) {
    try {
      const response = await http.put(`${ENDPOINTS.MATERIAL.BASE}/${id}/deactivate`);
      return response.data;
    } catch (error) {
      throw new Error(`停用材料失败: ${error.message}`);
    }
  }
  
  // 获取材料所有版本
  async getMaterialVersions() {
    try {
      const response = await http.get(`${ENDPOINTS.MATERIAL.BASE}/versions`);
      return response.data;
    } catch (error) {
      console.error('获取材料版本失败:', error);
      return [];
    }
  }
}

export default new MaterialService();