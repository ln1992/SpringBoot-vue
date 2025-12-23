// 定义所有API端点
const ENDPOINTS = {
  CLOTHING: {
    BASE: '/clothings'
  },
  CLOTHING_STOCK_RECORD: {
    BASE: '/clothing-stock-records'
  },
  MATTER: {
    BASE: '/matters'
  },
  MATERIAL: {
    BASE: '/materials'
  },
  PROCESS_DIAGRAMS: '/process-diagrams',
  APPROVAL_PROCESS_DIAGRAM: {
    BASE: '/process-diagrams/approval'
  },
  BUSINESS_PROCESS_DIAGRAM: {
    BASE: '/process-diagrams/business'
  },
  UPDATE_RECORD: {
    BASE: '/update-records'
  },
  CLOTHING_REPORTS: {
    BASE: '/clothing-reports',
    INVENTORY_SUMMARY: '/clothing-reports/inventory-summary',
    LOW_STOCK: '/clothing-reports/low-stock',
    INBOUND_OUTBOUND: '/clothing-reports/inbound-outbound',
    STOCK_VALUE: '/clothing-reports/stock-value',
    MONTHLY_STATS: '/clothing-reports/monthly-stats'
  },
  MATTER_TOOLS: {
    BASE: '/matters/tools',
    COMPARE_VERSIONS: '/matters/tools/compare-versions',
    COMPARE_MATTERS: '/matters/tools/compare-matters',
    BATCH_COPY: '/matters/tools/batch/copy',
    BATCH_PUBLISH: '/matters/tools/batch/publish',
    EXPORT_CATALOG: '/matters/tools/export/catalog',
    EXPORT_DOCUMENTS: '/matters/tools/export/documents'
  }
};

export { ENDPOINTS };