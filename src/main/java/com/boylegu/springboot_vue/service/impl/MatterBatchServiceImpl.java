package com.boylegu.springboot_vue.service.impl;

import com.boylegu.springboot_vue.aop.annotation.RecordUpdate;
import com.boylegu.springboot_vue.entities.Matter;
import com.boylegu.springboot_vue.service.MatterBatchService;
import com.boylegu.springboot_vue.service.MatterService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Service
@Transactional
public class MatterBatchServiceImpl implements MatterBatchService {
    
    private static final Logger logger = Logger.getLogger(MatterBatchServiceImpl.class.getName());
    
    @Autowired
    private MatterService matterService;
    
    @Override
    @RecordUpdate(operation = RecordUpdate.OperationType.BATCH_COPY, description = "批量拷贝事项")
    public List<Matter> batchCopyMatter() {
        List<Matter> copiedMatters = new ArrayList<>();
        
        // 获取最新版本号
        Long latestVersion = matterService.findMaxVersion();
        if (latestVersion == null) {
            logger.warning("没有找到任何版本的事项");
            return copiedMatters;
        }
        
        // 获取最新版本的所有有效事项
        List<Matter> latestMatters = matterService.findMattersByVersionAndValid(latestVersion);
        if (latestMatters.isEmpty()) {
            logger.warning("最新版本 " + latestVersion + " 没有找到任何有效事项");
            return copiedMatters;
        }
        
        // 计算目标版本号
        Long targetVersion = latestVersion + 1;
        
        // 拷贝所有最新版本的事项
        for (Matter sourceMatter : latestMatters) {
            Matter copiedMatter = new Matter();
            // 拷贝所有属性，排除ID字段
            BeanUtils.copyProperties(sourceMatter, copiedMatter, "id");
            
            // 设置新版本号
            copiedMatter.setVersion(targetVersion);
            
            // 重置状态
            copiedMatter.setValid(true);
            copiedMatter.setPublish(false);
            
            // 保存拷贝的事项
            copiedMatters.add(matterService.saveMatter(copiedMatter));
        }
        
        logger.info("成功拷贝 " + copiedMatters.size() + " 个事项，从版本 " + latestVersion + " 到版本 " + targetVersion);
        return copiedMatters;
    }
    
    @Override
    @RecordUpdate(operation = RecordUpdate.OperationType.BATCH_PUBLISH, description = "批量发布事项")
    public List<Matter> batchPublishMatters() {
        List<Matter> publishedMatters = new ArrayList<>();

        // 获取最新版本号
        Long latestVersion = matterService.findMaxVersion();
        if (latestVersion == null) {
            logger.warning("没有找到任何版本的事项");
            return publishedMatters;
        }
        
        // 获取最新版本的所有有效事项
        List<Matter> latestMatters = matterService.findMattersByVersionAndValid(latestVersion);
        if (latestMatters.isEmpty()) {
            logger.warning("最新版本 " + latestVersion + " 没有找到任何有效事项");
            return publishedMatters;
        }
        
        // 批量发布
        for (Matter matter : latestMatters) {
            // 使用matterService的publishMatter方法而不是直接修改实体
            publishedMatters.add(matterService.publishMatter(matter.getId()));
        }
        
        logger.info("成功发布 " + publishedMatters.size() + " 个事项，版本号: " + latestVersion);
        return publishedMatters;
    }
}