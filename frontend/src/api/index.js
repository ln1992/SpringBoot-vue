// API 层入口文件
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
  updateRecordService,
  http,
  ENDPOINTS
};

export default {
  materialService,
  matterService,
  processDiagramService,
  matterToolsService,
  updateRecordService,
  http,
  ENDPOINTS
};