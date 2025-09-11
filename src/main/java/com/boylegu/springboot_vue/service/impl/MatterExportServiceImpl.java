package com.boylegu.springboot_vue.service.impl;

import com.boylegu.springboot_vue.entities.ApprovalProcessDiagram;
import com.boylegu.springboot_vue.entities.BusinessProcessDiagram;
import com.boylegu.springboot_vue.entities.Material;
import com.boylegu.springboot_vue.entities.Matter;
import com.boylegu.springboot_vue.service.MatterExportService;
import com.boylegu.springboot_vue.service.MatterService;
import com.boylegu.springboot_vue.service.MaterialService;
import com.boylegu.springboot_vue.service.ProcessDiagramService;
import com.boylegu.springboot_vue.util.MatterWordExportUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Objects;

/**
 * 事项导出服务实现类
 * 实现了 MatterExportService 接口定义的方法
 */
@Service
public class MatterExportServiceImpl implements MatterExportService {

    private final MatterService matterService;
    private final MaterialService materialService;
    private final ProcessDiagramService<ApprovalProcessDiagram> approvalProcessDiagramService;
    private final ProcessDiagramService<BusinessProcessDiagram> businessProcessDiagramService;
    private final MatterWordExportUtil matterWordExportUtil;

    @Autowired
    public MatterExportServiceImpl(
            MatterService matterService,
            MaterialService materialService,
            ProcessDiagramService<ApprovalProcessDiagram> approvalProcessDiagramService,
            ProcessDiagramService<BusinessProcessDiagram> businessProcessDiagramService,
            MatterWordExportUtil matterWordExportUtil) {
        this.matterService = matterService;
        this.materialService = materialService;
        this.approvalProcessDiagramService = approvalProcessDiagramService;
        this.businessProcessDiagramService = businessProcessDiagramService;
        this.matterWordExportUtil = matterWordExportUtil;
    }

    @Override
    public byte[] exportMattersCatalogByVersion(Long version) throws Exception {
        List<Matter> matters = matterService.getMattersByVersionAndValid(version);
        return matterWordExportUtil.exportMattersCatalogToWord(matters);
    }

    @Override
    public byte[] exportMattersDocumentsByVersion(Long version) throws Exception {
        List<Matter> matters = matterService.getMattersByVersionAndValid(version);
        List<Long> materialIds = matters.stream()
                .flatMap(matter -> matter.getMaterialIds() != null ? matter.getMaterialIds().stream() : java.util.stream.Stream.empty())
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        List<Long> approvalDiagramIds = matters.stream()
                .map(Matter::getApprovalProcessDiagramId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        List<Long> businessDiagramIds = matters.stream()
                .map(Matter::getBusinessProcessDiagramId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Material> materialsMap = materialService.getMaterialsMapByIds(materialIds);

        Map<Long, ApprovalProcessDiagram> approvalDiagramsMap = approvalProcessDiagramService.getProcessDiagramsMapByIds(approvalDiagramIds);

        Map<Long, BusinessProcessDiagram> businessDiagramsMap = businessProcessDiagramService.getProcessDiagramsMapByIds(businessDiagramIds);

        // 文档导出使用详细文档导出方法
        return matterWordExportUtil.exportMattersToWord(matters, materialsMap, approvalDiagramsMap, businessDiagramsMap);
    }
}