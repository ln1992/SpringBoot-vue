import http from './http'
import { ENDPOINTS } from './endpoints'

export const clothingReportService = {
  // 库存汇总报表
  getInventorySummary(params) {
    return http.get(ENDPOINTS.CLOTHING_REPORTS.INVENTORY_SUMMARY, { params: params })
  },

  // 低库存报表
  getLowStockReport(params) {
    return http.get(ENDPOINTS.CLOTHING_REPORTS.LOW_STOCK, { params: params })
  },

  // 出入库报表
  getInboundOutboundReport(startDate, endDate) {
    // 传递null参数而不是跳过参数
    const params = {};
    if (startDate) params.startDate = startDate;
    if (endDate) params.endDate = endDate;
    return http.get(ENDPOINTS.CLOTHING_REPORTS.INBOUND_OUTBOUND, { params })
  },

  // 库存价值报表
  getStockValueReport(params) {
    return http.get(ENDPOINTS.CLOTHING_REPORTS.STOCK_VALUE, { params: params })
  },
  
  // 月度统计报表
  getMonthlyStatsReport(clothingName, startDate, endDate) {
    const params = { clothingName };
    if (startDate) params.startDate = startDate;
    if (endDate) params.endDate = endDate;
    return http.get(ENDPOINTS.CLOTHING_REPORTS.MONTHLY_STATS, { params })
  }
}

export default clothingReportService