package com.douyu.springboot_vue.service;

import org.springframework.stereotype.Service;

/**
 * 事项导出服务接口
 * 定义了事项导出功能的方法，供实现类具体实现
 */
@Service
public interface MatterExportService {
    /**
     * 导出指定版本的事项目录
     * @param version 版本号
     * @return 导出的字节数组
     * @throws Exception 导出过程中可能发生的异常
     */
    byte[] exportMattersCatalogByVersion(Long version) throws Exception;
    
    /**
     * 导出指定版本的事项文档
     * @param version 版本号
     * @return 导出的字节数组
     * @throws Exception 导出过程中可能发生的异常
     */
    byte[] exportMattersDocumentsByVersion(Long version) throws Exception;
}