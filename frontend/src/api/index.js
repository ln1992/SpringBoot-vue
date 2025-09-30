// API 层入口文件
import clothingService from './clothingService';
import clothingStockRecordService from './clothingStockRecordService';
import materialService from './materialService';
import matterService from './matterService';
import processDiagramService from './processDiagramService';
import matterToolsService from './matterToolsService';
import updateRecordService from './updateRecordService';
import http from './http';
import { ENDPOINTS } from './endpoints';

export {
  materialService,
  matterService,
  processDiagramService,
  matterToolsService,
  clothingService,
  clothingStockRecordService,
  updateRecordService,
  http,
  ENDPOINTS
};

export default {
  clothingService,
  clothingStockRecordService,
  materialService,
  matterService,
  processDiagramService,
  matterToolsService,
  updateRecordService,
  http,
  ENDPOINTS
};
