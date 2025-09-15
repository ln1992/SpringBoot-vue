// src/main/java/com/boylegu/springboot_vue/service/MatterVersionCompareService.java
package com.boylegu.springboot_vue.service;

import com.boylegu.springboot_vue.entities.MatterCompareItem;

import java.util.List;

public interface MatterVersionCompareService {
    /**
     * 比较两个版本的事项数据
     * @param version1 旧版本号
     * @param version2 新版本号
     * @return 版本比较结果列表
     */
    List<MatterCompareItem> compareVersions(Long version1, Long version2);
}
