import http from './http';
import ENDPOINTS from './endpoints';

class MaterialService {
  // 获取所有材料
  async getAllMaterials() {
    try {
      const response = await http.get(ENDPOINTS.MATERIALS);
      return response.data;
    } catch (error) {
      throw new Error(`获取材料列表失败: ${error.message}`);
    }
  }

  // 根据ID获取材料
  async getMaterialById(id) {
    try {
      const response = await http.get(ENDPOINTS.MATERIALS_BY_ID(id));
      return response.data;
    } catch (error) {
      throw new Error(`获取材料失败: ${error.message}`);
    }
  }

  // 创建材料
  async createMaterial(material) {
    try {
      const response = await http.post(ENDPOINTS.MATERIALS, material);
      return response.data;
    } catch (error) {
      throw new Error(`创建材料失败: ${error.message}`);
    }
  }

  // 更新材料
  async updateMaterial(id, material) {
    try {
      const response = await http.put(ENDPOINTS.MATERIALS_BY_ID(id), material);
      return response.data;
    } catch (error) {
      throw new Error(`更新材料失败: ${error.message}`);
    }
  }

  // 删除材料
  async deleteMaterial(id) {
    try {
      const response = await http.delete(ENDPOINTS.MATERIALS_BY_ID(id));
      return response.data;
    } catch (error) {
      throw new Error(`删除材料失败: ${error.message}`);
    }
  }

  // 激活材料
  async activateMaterial(id) {
    try {
      const response = await http.put(ENDPOINTS.MATERIALS_ACTIVATE(id));
      return response.data;
    } catch (error) {
      throw new Error(`激活材料失败: ${error.message}`);
    }
  }

  // 停用材料
  async deactivateMaterial(id) {
    try {
      const response = await http.put(ENDPOINTS.MATERIALS_DEACTIVATE(id));
      return response.data;
    } catch (error) {
      throw new Error(`停用材料失败: ${error.message}`);
    }
  }
}

export default new MaterialService();