import http from './http';
import { ENDPOINTS } from './endpoints';

const clothingService = {
  // 获取所有服装
  getAll: () => {
    // 添加超时处理
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 10000); // 10秒超时

    return http.get(ENDPOINTS.CLOTHING.BASE, { signal: controller.signal })
      .then(response => {
        clearTimeout(timeoutId);
        return response;
      })
      .catch(error => {
        clearTimeout(timeoutId);
        if (error.name === 'AbortError') {
          throw new Error('请求超时，请检查网络连接');
        }
        throw error;
      });
  },

  // 根据ID获取服装
  getById: (id) => {
    return http.get(`${ENDPOINTS.CLOTHING.BASE}/${id}`);
  },

  // 创建服装
  create: (clothingData) => {
    return http.post(ENDPOINTS.CLOTHING.BASE, clothingData);
  },

  // 更新服装
  update: (id, clothingData) => {
    return http.put(`${ENDPOINTS.CLOTHING.BASE}/${id}`, clothingData);
  },

  // 删除服装
  delete: (id) => {
    return http.delete(`${ENDPOINTS.CLOTHING.BASE}/${id}`);
  },

  // 上线服装
  activate: (id) => {
    return http.put(`${ENDPOINTS.CLOTHING.BASE}/${id}/activate`);
  },

  // 下线服装
  deactivate: (id) => {
    return http.put(`${ENDPOINTS.CLOTHING.BASE}/${id}/deactivate`);
  },

  // 入库操作
  inbound: (id, inboundData) => {
    // 确保数量参数是整数类型
    const data = {
      ...inboundData,
      quantity: parseInt(inboundData.quantity, 10)
    };
    return http.post(`${ENDPOINTS.CLOTHING.BASE}/${id}/inbound`, data);
  },

  // 出库操作
  outbound: (id, outboundData) => {
    // 确保数量参数是整数类型
    const data = {
      ...outboundData,
      quantity: parseInt(outboundData.quantity, 10)
    };
    return http.post(`${ENDPOINTS.CLOTHING.BASE}/${id}/outbound`, data);
  },

  // 根据库存记录恢复库存
  restoreStock: (id) => {
    return http.post(`${ENDPOINTS.CLOTHING.BASE}/${id}/restore-stock`);
  }

};

export default clothingService;
