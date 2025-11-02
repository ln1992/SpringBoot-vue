import http from './http';
import { ENDPOINTS } from './endpoints';

const clothingStockRecordService = {
  // 获取所有库存记录
  getAll: () => {
    return http.get(ENDPOINTS.CLOTHING_STOCK_RECORD.BASE);
  },

  // 根据ID获取库存记录
  getById: (id) => {
    return http.get(`${ENDPOINTS.CLOTHING_STOCK_RECORD.BASE}/${id}`);
  },

  // 创建库存记录
  create: (stockRecordData) => {
    return http.post(ENDPOINTS.CLOTHING_STOCK_RECORD.BASE, stockRecordData);
  },

  // 更新库存记录
  update: (id, stockRecordData) => {
    return http.put(`${ENDPOINTS.CLOTHING_STOCK_RECORD.BASE}/${id}`, stockRecordData);
  },

  // 删除库存记录
  delete: (id) => {
    return http.delete(`${ENDPOINTS.CLOTHING_STOCK_RECORD.BASE}/${id}`);
  },

  // 根据服装ID获取库存记录
  getByClothingId: (clothingId) => {
    return http.get(`${ENDPOINTS.CLOTHING_STOCK_RECORD.BASE}/clothing/${clothingId}`);
  },

  // 下线库存记录
  deactivate: (id) => {
    return http.put(`${ENDPOINTS.CLOTHING_STOCK_RECORD.BASE}/${id}/deactivate`);
  },

  // 上线库存记录
  activate: (id) => {
    return http.put(`${ENDPOINTS.CLOTHING_STOCK_RECORD.BASE}/${id}/activate`);
  },

  // 删除已下线的库存记录
  deleteDeactivated: (id) => {
    return http.delete(`${ENDPOINTS.CLOTHING_STOCK_RECORD.BASE}/${id}/delete-deactivated`);
  }
};

export default clothingStockRecordService;