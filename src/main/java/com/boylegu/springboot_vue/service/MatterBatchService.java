package com.boylegu.springboot_vue.service;

import com.boylegu.springboot_vue.entities.Matter;

import java.util.List;

public interface MatterBatchService {
    
    /**
     * 批量拷贝最新版本的事项到新版本（版本号+1）
     * @return 拷贝后的事项列表
     */
    List<Matter> batchCopyMatter();
    
    /**
     * 批量发布最新版本的所有事项
     * @return 发布后的事项列表
     */
    List<Matter> batchPublishMatters();
}