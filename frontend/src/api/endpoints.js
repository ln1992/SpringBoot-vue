// API端点配置

export const ENDPOINTS = {
  // 材料相关端点
  MATERIALS: '/materials',
  MATERIALS_BY_ID: (id) => `/materials/${id}`,
  MATERIALS_ACTIVATE: (id) => `/materials/${id}/activate`,
  MATERIALS_DEACTIVATE: (id) => `/materials/${id}/deactivate`,
  
  // 事项相关端点
  MATTERS: '/matters',
  MATTERS_BY_ID: (id) => `/matters/${id}`,
  MATTERS_ACTIVATE: (id) => `/matters/${id}/activate`,
  MATTERS_DEACTIVATE: (id) => `/matters/${id}/deactivate`,
  MATTERS_PUBLISH: (id) => `/matters/${id}/publish`,
  MATTERS_UNPUBLISH: (id) => `/matters/${id}/unpublish`,
  
  // 流程图相关端点
  PROCESS_DIAGRAMS: '/process-diagrams',
  APPROVAL_PROCESS_DIAGRAMS: '/process-diagrams/approval',
  BUSINESS_PROCESS_DIAGRAMS: '/process-diagrams/business'
};

export default ENDPOINTS;