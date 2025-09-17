// src/main/java/com/boylegu/springboot_vue/service/impl/MatterVersionCompareServiceImpl.java
package com.boylegu.springboot_vue.service.impl;

import com.boylegu.springboot_vue.entities.MatterCompareItem;
import com.boylegu.springboot_vue.entities.MatterCompareItem.CompareStatus;
import com.boylegu.springboot_vue.entities.Matter;
import com.boylegu.springboot_vue.service.MatterService;
import com.boylegu.springboot_vue.service.MatterVersionCompareService;
import com.boylegu.springboot_vue.util.ObjectCompareUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MatterVersionCompareServiceImpl implements MatterVersionCompareService {

    private static final String OLD = "old";
    private static final String NEW = "new";

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
                        matter1.get__name__(),
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
                        matter2.get__name__(),
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
                            matter1.get__name__(),
                            matter1.getId(),
                            matter2.getId(),
                            CompareStatus.MODIFIED
                    ));
                } else {
                    result.add(new MatterCompareItem(
                            matter1.getMainItemCode(),
                            matter1.getSubItemCode(),
                            matter1.getGrandchildItemCode(),
                            matter1.get__name__(),
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
                    matter1.get__name__(),
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
                    matter2.get__name__(),
                    null,
                    matter2.getId(),
                    CompareStatus.ADDED
            ));
            j++;
        }

        return result;
    }

    private boolean isMatterModified(Matter oldMatter, Matter newMatter) {
        // 比较事项的关键字段是否发生变化,publish, valid, version 暂不比较
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
                !Objects.equals(oldMatter.getBusinessProcessDiagramId(), newMatter.getBusinessProcessDiagramId());
    }

    /**
     * 对比两个事项对象，返回差异字段的详细信息
     * @param oldMatterId 旧版本事项ID
     * @param newMatterId 新版本事项ID
     * @return 包含差异字段信息的Map，结构为 {old:{"fieldName":oldValue}, new:{"fieldName":newValue}}
     */
    public Map<String, Map<String, Object>> compareMattersById(Long oldMatterId, Long newMatterId) {
        Map<String, Map<String, Object>> result = new HashMap<>();
        Map<String, Object> oldValues = new HashMap<>();
        Map<String, Object> newValues = new HashMap<>();

        // 获取两个版本的事项对象
        Matter oldMatter = (oldMatterId != null) ? matterService.getMatterById(oldMatterId) : null;
        Matter newMatter = (newMatterId != null) ? matterService.getMatterById(newMatterId) : null;

        // 如果任一事项不存在，返回空Map
        if (oldMatter != null && newMatter == null) {
            fillMapWithMatterData(oldValues, oldMatter);
        } else if (oldMatter == null && newMatter != null ) {
            fillMapWithMatterData(newValues, newMatter);
        } else if (oldMatter != null && newMatter != null) {
            // 逐一比较各个字段，只添加有差异的字段
            addIfDifferent(oldValues, newValues, "mainItemCode", oldMatter.getMainItemCode(), newMatter.getMainItemCode());
            addIfDifferent(oldValues, newValues, "subItemCode", oldMatter.getSubItemCode(), newMatter.getSubItemCode());
            addIfDifferent(oldValues, newValues, "grandchildItemCode", oldMatter.getGrandchildItemCode(), newMatter.getGrandchildItemCode());
            addIfDifferent(oldValues, newValues, "mainItemName", oldMatter.getMainItemName(), newMatter.getMainItemName());
            addIfDifferent(oldValues, newValues, "subItemName", oldMatter.getSubItemName(), newMatter.getSubItemName());
            addIfDifferent(oldValues, newValues, "grandchildItemName", oldMatter.getGrandchildItemName(), newMatter.getGrandchildItemName());
            addIfDifferent(oldValues, newValues, "bases", oldMatter.getBases(), newMatter.getBases());
            addIfDifferent(oldValues, newValues, "materialIds", oldMatter.getMaterialIds(), newMatter.getMaterialIds());
            addIfDifferent(oldValues, newValues, "legalTimeLimit", oldMatter.getLegalTimeLimit(), newMatter.getLegalTimeLimit());
            addIfDifferent(oldValues, newValues, "committedTimeLimit", oldMatter.getCommittedTimeLimit(), newMatter.getCommittedTimeLimit());
            addIfDifferent(oldValues, newValues, "approvalLevel", oldMatter.getApprovalLevel(), newMatter.getApprovalLevel());
            addIfDifferent(oldValues, newValues, "provincialDepartmentOffice", oldMatter.getProvincialDepartmentOffice(), newMatter.getProvincialDepartmentOffice());
            addIfDifferent(oldValues, newValues, "approvalProcessDiagramId", oldMatter.getApprovalProcessDiagramId(), newMatter.getApprovalProcessDiagramId());
            addIfDifferent(oldValues, newValues, "businessProcessDiagramId", oldMatter.getBusinessProcessDiagramId(), newMatter.getBusinessProcessDiagramId());
            addIfDifferent(oldValues, newValues, "__name__", oldMatter.get__name__(), newMatter.get__name__());
            addIfDifferent(oldValues, newValues, "version", oldMatter.getVersion(), newMatter.getVersion());
            addIfDifferent(oldValues, newValues, "valid", oldMatter.getValid(), newMatter.getValid());
            addIfDifferent(oldValues, newValues, "publish", oldMatter.getPublish(), newMatter.getPublish());
        }
        result.put(OLD, oldValues);
        result.put(NEW, newValues);

        return result;
    }

    /**
     * 填充Map与事项数据
     * @param values 目标Map
     * @param matter 事项对象
     */
    private void fillMapWithMatterData(Map<String, Object> values, Matter matter) {
        values.put("mainItemCode", matter.getMainItemCode());
        values.put("subItemCode", matter.getSubItemCode());
        values.put("grandchildItemCode", matter.getGrandchildItemCode());
        values.put("mainItemName", matter.getMainItemName());
        values.put("subItemName", matter.getSubItemName());
        values.put("grandchildItemName", matter.getGrandchildItemName());
        values.put("bases", matter.getBases());
        values.put("materialIds", matter.getMaterialIds());
        values.put("legalTimeLimit", matter.getLegalTimeLimit());
        values.put("committedTimeLimit", matter.getCommittedTimeLimit());
        values.put("approvalLevel", matter.getApprovalLevel());
        values.put("provincialDepartmentOffice", matter.getProvincialDepartmentOffice());
        values.put("approvalProcessDiagramId", matter.getApprovalProcessDiagramId());
        values.put("businessProcessDiagramId", matter.getBusinessProcessDiagramId());
        values.put("__name__", matter.get__name__());
        values.put("version", matter.getVersion());
        values.put("valid", matter.getValid());
        values.put("publish", matter.getPublish());
    }

    /**
     * 如果两个值不同，则添加到对应的Map中
     * @param oldValues 旧值Map
     * @param newValues 新值Map
     * @param fieldName 字段名
     * @param oldValue 旧值
     * @param newValue 新值
     */
    private void addIfDifferent(Map<String, Object> oldValues, Map<String, Object> newValues,
                                String fieldName, Object oldValue, Object newValue) {
        if (!ObjectCompareUtils.objectEqual(oldValue, newValue)) {
            oldValues.put(fieldName, oldValue);
            newValues.put(fieldName, newValue);
        }
    }
}
