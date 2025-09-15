// src/main/java/com/boylegu/springboot_vue/service/impl/MatterVersionCompareServiceImpl.java
package com.boylegu.springboot_vue.service.impl;

import com.boylegu.springboot_vue.entities.MatterCompareItem;
import com.boylegu.springboot_vue.entities.MatterCompareItem.CompareStatus;
import com.boylegu.springboot_vue.entities.Matter;
import com.boylegu.springboot_vue.service.MatterService;
import com.boylegu.springboot_vue.service.MatterVersionCompareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class MatterVersionCompareServiceImpl implements MatterVersionCompareService {

    @Autowired
    private MatterService matterService;

    @Override
    public List<MatterCompareItem> compareVersions(Long version1, Long version2) {
        // 获取两个版本的所有有效事项
        List<Matter> matters1 = matterService.findMattersByVersionAndValid(version1);
        List<Matter> matters2 = matterService.findMattersByVersionAndValid(version2);

        // 对两个列表进行排序
        matters1.sort(Matter.createCodeComparator());
        matters2.sort(Matter.createCodeComparator());

        // 创建用于存储结果的列表
        List<MatterCompareItem> result = new ArrayList<>();

        // 使用类似归并排序的方式进行对比
        int i = 0, j = 0;
        while (i < matters1.size() && j < matters2.size()) {
            Matter matter1 = matters1.get(i);
            Matter matter2 = matters2.get(j);

            int comparison = Matter.createCodeComparator().compare(matter1, matter2);
            if (comparison < 0) {
                // matters1中的事项在matters2中不存在，标记为删除
                result.add(new MatterCompareItem(
                        matter1.getMainItemCode(),
                        matter1.getSubItemCode(),
                        matter1.getGrandchildItemCode(),
                        matter1.getId(),
                        null,
                        CompareStatus.DELETED
                ));
                i++;
            } else if (comparison > 0) {
                // matters2中的事项在matters1中不存在，标记为新增
                result.add(new MatterCompareItem(
                        matter2.getMainItemCode(),
                        matter2.getSubItemCode(),
                        matter2.getGrandchildItemCode(),
                        null,
                        matter2.getId(),
                        CompareStatus.ADDED
                ));
                j++;
            } else {
                // 两个版本中都存在，检查是否有修改
                if (isMatterModified(matter1, matter2)) {
                    result.add(new MatterCompareItem(
                            matter1.getMainItemCode(),
                            matter1.getSubItemCode(),
                            matter1.getGrandchildItemCode(),
                            matter1.getId(),
                            matter2.getId(),
                            CompareStatus.MODIFIED
                    ));
                } else {
                    result.add(new MatterCompareItem(
                            matter1.getMainItemCode(),
                            matter1.getSubItemCode(),
                            matter1.getGrandchildItemCode(),
                            matter1.getId(),
                            matter2.getId(),
                            CompareStatus.UNCHANGED
                    ));
                }
                i++;
                j++;
            }
        }

        // 处理剩余的旧版本事项（删除的事项）
        while (i < matters1.size()) {
            Matter matter1 = matters1.get(i);
            result.add(new MatterCompareItem(
                    matter1.getMainItemCode(),
                    matter1.getSubItemCode(),
                    matter1.getGrandchildItemCode(),
                    matter1.getId(),
                    null,
                    CompareStatus.DELETED
            ));
            i++;
        }

        // 处理剩余的新版本事项（新增的事项）
        while (j < matters2.size()) {
            Matter matter2 = matters2.get(j);
            result.add(new MatterCompareItem(
                    matter2.getMainItemCode(),
                    matter2.getSubItemCode(),
                    matter2.getGrandchildItemCode(),
                    null,
                    matter2.getId(),
                    CompareStatus.ADDED
            ));
            j++;
        }

        return result;
    }

    private boolean isMatterModified(Matter oldMatter, Matter newMatter) {
        // 比较事项的关键字段是否发生变化
        return !Objects.equals(oldMatter.getMainItemCode(), newMatter.getMainItemCode()) ||
                !Objects.equals(oldMatter.getSubItemCode(), newMatter.getSubItemCode()) ||
                !Objects.equals(oldMatter.getGrandchildItemCode(), newMatter.getGrandchildItemCode()) ||
                !Objects.equals(oldMatter.getMainItemName(), newMatter.getMainItemName()) ||
                !Objects.equals(oldMatter.getSubItemName(), newMatter.getSubItemName()) ||
                !Objects.equals(oldMatter.getGrandchildItemName(), newMatter.getGrandchildItemName()) ||
                !Objects.equals(oldMatter.getBases(), newMatter.getBases()) ||
                !Objects.equals(oldMatter.getMaterialIds(), newMatter.getMaterialIds()) ||
                !Objects.equals(oldMatter.getLegalTimeLimit(), newMatter.getLegalTimeLimit()) ||
                !Objects.equals(oldMatter.getCommittedTimeLimit(), newMatter.getCommittedTimeLimit()) ||
                !Objects.equals(oldMatter.getApprovalLevel(), newMatter.getApprovalLevel()) ||
                !Objects.equals(oldMatter.getProvincialDepartmentOffice(), newMatter.getProvincialDepartmentOffice()) ||
                !Objects.equals(oldMatter.getApprovalProcessDiagramId(), newMatter.getApprovalProcessDiagramId()) ||
                !Objects.equals(oldMatter.getBusinessProcessDiagramId(), newMatter.getBusinessProcessDiagramId()) ||
                !Objects.equals(oldMatter.getPublish(), newMatter.getPublish()) ||
                !Objects.equals(oldMatter.getValid(), newMatter.getValid()) ||
                !Objects.equals(oldMatter.getVersion(), newMatter.getVersion());
    }
}
