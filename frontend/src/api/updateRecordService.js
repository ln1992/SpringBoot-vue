// src/api/updateRecordService.js
import http from './http';
import { ENDPOINTS } from './endpoints';

const updateRecordService = {
  // 获取所有更新记录
  getAllUpdateRecords() {
    return http.get(ENDPOINTS.UPDATE_RECORD.BASE);
  },

  // 根据ID获取更新记录
  getUpdateRecordById(id) {
    return http.get(`${ENDPOINTS.UPDATE_RECORD.BASE}/${id}`);
  }
};

export default updateRecordService;