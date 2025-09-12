import http from './http';
import ENDPOINTS from './endpoints';

class ProcessDiagramService {
  // 获取所有审批流程图
  async getAllApprovalProcessDiagrams() {
    try {
      const response = await http.get(`${ENDPOINTS.APPROVAL_PROCESS_DIAGRAMS}/all`);
      return response.data;
    } catch (error) {
      console.error('获取审批流程图失败:', error);
      // 返回空数组而不是抛出错误，确保即使服务不可用也能继续运行
      return [];
    }
  }

  // 获取所有业务流程图
  async getAllBusinessProcessDiagrams() {
    try {
      const response = await http.get(`${ENDPOINTS.BUSINESS_PROCESS_DIAGRAMS}/all`);
      return response.data;
    } catch (error) {
      console.error('获取业务流程图失败:', error);
      // 返回空数组而不是抛出错误，确保即使服务不可用也能继续运行
      return [];
    }
  }
  
  // 获取审批流程图详情
  async getApprovalProcessDiagramById(id) {
    try {
      const response = await http.get(`${ENDPOINTS.APPROVAL_PROCESS_DIAGRAMS}/${id}`);
      return response.data;
    } catch (error) {
      throw new Error(`获取审批流程图详情失败: ${error.message}`);
    }
  }
  
  // 获取业务流程图详情
  async getBusinessProcessDiagramById(id) {
    try {
      const response = await http.get(`${ENDPOINTS.BUSINESS_PROCESS_DIAGRAMS}/${id}`);
      return response.data;
    } catch (error) {
      throw new Error(`获取业务流程图详情失败: ${error.message}`);
    }
  }

  // 获取审批流程图（带分页等参数）
  async getApprovalProcessDiagrams(params = {}) {
    try {
      const response = await http.get(ENDPOINTS.APPROVAL_PROCESS_DIAGRAMS, { params });
      return response.data;
    } catch (error) {
      throw new Error(`获取审批流程图失败: ${error.message}`);
    }
  }

  // 获取业务流程图（带分页等参数）
  async getBusinessProcessDiagrams(params = {}) {
    try {
      const response = await http.get(ENDPOINTS.BUSINESS_PROCESS_DIAGRAMS, { params });
      return response.data;
    } catch (error) {
      throw new Error(`获取业务流程图失败: ${error.message}`);
    }
  }

  // 创建审批流程图
  async createApprovalProcessDiagram(formData) {
    try {
      const response = await http.post(ENDPOINTS.APPROVAL_PROCESS_DIAGRAMS, formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
      return response.data;
    } catch (error) {
      throw new Error(`创建审批流程图失败: ${error.message}`);
    }
  }
  
  // 创建业务流程图
  async createBusinessProcessDiagram(formData) {
    try {
      const response = await http.post(ENDPOINTS.BUSINESS_PROCESS_DIAGRAMS, formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
      return response.data;
    } catch (error) {
      throw new Error(`创建业务流程图失败: ${error.message}`);
    }
  }
  
  // 更新审批流程图
  async updateApprovalProcessDiagram(id, formData) {
    try {
      const response = await http.put(`${ENDPOINTS.APPROVAL_PROCESS_DIAGRAMS}/${id}`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
      return response.data;
    } catch (error) {
      throw new Error(`更新审批流程图失败: ${error.message}`);
    }
  }
  
  // 更新业务流程图
  async updateBusinessProcessDiagram(id, formData) {
    try {
      const response = await http.put(`${ENDPOINTS.BUSINESS_PROCESS_DIAGRAMS}/${id}`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
      return response.data;
    } catch (error) {
      throw new Error(`更新业务流程图失败: ${error.message}`);
    }
  }
  
  // 删除审批流程图
  async deleteApprovalProcessDiagram(id) {
    try {
      const response = await http.delete(`${ENDPOINTS.APPROVAL_PROCESS_DIAGRAMS}/${id}`);
      return response.data;
    } catch (error) {
      throw new Error(`删除审批流程图失败: ${error.message}`);
    }
  }
  
  // 删除业务流程图
  async deleteBusinessProcessDiagram(id) {
    try {
      const response = await http.delete(`${ENDPOINTS.BUSINESS_PROCESS_DIAGRAMS}/${id}`);
      return response.data;
    } catch (error) {
      throw new Error(`删除业务流程图失败: ${error.message}`);
    }
  }
  
  // 激活审批流程图
  async activateApprovalProcessDiagram(id) {
    try {
      const response = await http.put(`${ENDPOINTS.APPROVAL_PROCESS_DIAGRAMS}/${id}/activate`);
      return response.data;
    } catch (error) {
      throw new Error(`激活审批流程图失败: ${error.message}`);
    }
  }
  
  // 激活业务流程图
  async activateBusinessProcessDiagram(id) {
    try {
      const response = await http.put(`${ENDPOINTS.BUSINESS_PROCESS_DIAGRAMS}/${id}/activate`);
      return response.data;
    } catch (error) {
      throw new Error(`激活业务流程图失败: ${error.message}`);
    }
  }
  
  // 停用审批流程图
  async deactivateApprovalProcessDiagram(id) {
    try {
      const response = await http.put(`${ENDPOINTS.APPROVAL_PROCESS_DIAGRAMS}/${id}/deactivate`);
      return response.data;
    } catch (error) {
      throw new Error(`停用审批流程图失败: ${error.message}`);
    }
  }
  
  // 停用业务流程图
  async deactivateBusinessProcessDiagram(id) {
    try {
      const response = await http.put(`${ENDPOINTS.BUSINESS_PROCESS_DIAGRAMS}/${id}/deactivate`);
      return response.data;
    } catch (error) {
      throw new Error(`停用业务流程图失败: ${error.message}`);
    }
  }
}

export default new ProcessDiagramService();