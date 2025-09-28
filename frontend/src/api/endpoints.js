// 定义所有API端点
const ENDPOINTS = {
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
