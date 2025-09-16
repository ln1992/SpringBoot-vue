// src/main/java/com/boylegu/springboot_vue/service/MatterVersionCompareService.java
package com.boylegu.springboot_vue.service;

import com.boylegu.springboot_vue.entities.MatterCompareItem;

import java.util.List;
import java.util.Map;


public interface MatterVersionCompareService {
    /**
     * 比较两个版本的事项数据
     * @param version1 旧版本号
     * @param version2 新版本号
     * @return 版本比较结果列表
     */
    List<MatterCompareItem> compareVersions(Long version1, Long version2);

    /**
     * 对比两个事项对象，返回差异字段的详细信息
     * @param oldMatterId 旧版本事项ID
     * @param newMatterId 新版本事项ID
     * @return 包含差异字段信息的Map，结构为 {old:{"fieldName":oldValue}, new:{"fieldName":newValue}}
     */
    Map<String, Map<String, Object>> compareMattersById(Long oldMatterId, Long newMatterId);
}
