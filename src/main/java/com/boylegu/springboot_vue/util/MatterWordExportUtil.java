package com.boylegu.springboot_vue.util;

import com.boylegu.springboot_vue.entities.Matter;
import com.boylegu.springboot_vue.entities.Material;
import com.boylegu.springboot_vue.entities.ProcessDiagram;
import com.boylegu.springboot_vue.entities.ApprovalProcessDiagram;
import com.boylegu.springboot_vue.entities.BusinessProcessDiagram;
import com.boylegu.springboot_vue.service.MatterService;
import com.boylegu.springboot_vue.service.MaterialService;
import com.boylegu.springboot_vue.service.ApprovalProcessDiagramService;
import com.boylegu.springboot_vue.service.BusinessProcessDiagramService;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Matter实体导出到Word文档的工具类（重写版）
 * 支持根据Matter ID列表批量导出，每个Matter包含表格和相关材料信息
 */
/**
 * Matter实体导出到Word文档的工具类（重写版）
 * 支持根据Matter ID列表批量导出，每个Matter包含表格和相关材料信息
 */
@Component
public class MatterWordExportUtil {

    // 定义表格行号常量
    private static final int ROW_HEADER_MAIN = 0;                 // 主表头行
    private static final int ROW_HEADER_SUB = 1;                  // 子表头行
    private static final int ROW_DATA_START = 2;                  // 数据起始行

    // 定义表格列的常量，提高可读性和可维护性
    private static final int COL_MAIN_ITEM_NAME = 0;              // 主项名称
    private static final int COL_SUB_ITEM_NAME = 1;               // 子项名称
    private static final int COL_GRANDCHILD_ITEM_NAME = 2;        // 孙项名称
    private static final int COL_BASES = 3;                       // 经办依据
    private static final int COL_MATERIAL_START = 4;              // 材料信息起始列
    private static final int COL_MATERIAL_ORIGINAL_DETAIL = 4;    // 原材料明细
    private static final int COL_MATERIAL_CURRENT_DETAIL = 5;     // 现材料明细
    private static final int COL_MATERIAL_REVIEW_POINT = 6;       // 审核要点
    private static final int COL_MATERIAL_AUTO_APPROVAL_CRITERIA = 7; // "智能秒批"判断标准
    private static final int COL_MATERIAL_SHARED = 8;             // 是否共享
    private static final int COL_MATERIAL_SOURCE = 9;             // 材料来源
    private static final int COL_MATERIAL_PROCESSING_METHOD = 10; // 办理方式及材料信息获取方式说明
    private static final int COL_MATERIAL_ELIGIBLE_FOR_PROMISE = 11; // 是否适用告知承诺
    private static final int COL_MATERIAL_END = 11;               // 材料信息结束列
    private static final int COL_LEGAL_TIME_LIMIT = 12;           // 法定时限
    private static final int COL_COMMITTED_TIME_LIMIT = 13;       // 承诺时限
    private static final int COL_APPROVAL_LEVEL = 14;             // 审批层级
    private static final int COL_PROVINCIAL_DEPARTMENT = 15;      // 省厅对口指导处室（单位）

    // 材料信息列数
    private static final int MATERIAL_COLUMN_COUNT = COL_MATERIAL_END - COL_MATERIAL_START + 1;

    // 定义表头字符串常量
    private static final String HEADER_MAIN_ITEM_NAME = "主项名称";
    private static final String HEADER_SUB_ITEM_NAME = "子项名称";
    private static final String HEADER_GRANDCHILD_ITEM_NAME = "孙项名称";
    private static final String HEADER_BASES = "经办依据";
    private static final String HEADER_MATERIALS = "经 办 所 需 材 料";
    private static final String HEADER_MATERIAL_ORIGINAL_DETAIL = "原材料明细";
    private static final String HEADER_MATERIAL_CURRENT_DETAIL = "现材料明细";
    private static final String HEADER_MATERIAL_REVIEW_POINT = "审核要点";
    private static final String HEADER_MATERIAL_AUTO_APPROVAL_CRITERIA = "“智能秒批”判断标准";
    private static final String HEADER_MATERIAL_SHARED = "是否共享";
    private static final String HEADER_MATERIAL_SOURCE = "材料来源";
    private static final String HEADER_MATERIAL_PROCESSING_METHOD = "办理方式及材料信息获取方式说明";
    private static final String HEADER_MATERIAL_ELIGIBLE_FOR_PROMISE = "是否适用告知承诺";
    private static final String HEADER_LEGAL_TIME_LIMIT = "办理时限";
    private static final String HEADER_COMMITTED_TIME_LIMIT = "法定时限";
    private static final String HEADER_PROMISED_TIME_LIMIT = "承诺时限";
    private static final String HEADER_APPROVAL_LEVEL = "审批层级";
    private static final String HEADER_PROVINCIAL_DEPARTMENT = "省厅对口指导处室（单位）";

    // 定义标题字符串常量
    private static final String TITLE_TEXT = "全省交通运输系统群众和企业到政府办事事项“八统一”梳理表";

    /**
     * 根据Matter列表生成目录Word文档
     * 每个Matter在目录中占一行，按主项、子项、孙项层级结构展示
     *
     * @param matters Matter列表
     * @return Word文档字节数组
     * @throws IOException IO异常
     */
    public byte[] exportMattersCatalogToWord(List<Matter> matters) throws IOException {
        sortedMatters(matters);
        try (XWPFDocument document = new XWPFDocument()) {

            // 添加目录标题
            XWPFParagraph title = document.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = title.createRun();
            titleRun.setText("全省交通运输系统群众和企业到政府办事事项目录");
            titleRun.setBold(true);
            titleRun.setFontSize(16);
            titleRun.addBreak();

            // 添加空行
            document.createParagraph().createRun().addBreak();

            // 按层级结构输出目录
            Long currentMainItemCode = null;
            Long currentSubItemCode = null;

            for (int i = 0; i < matters.size(); i++) {
                Matter matter = matters.get(i);

                // 分别构建主项、子项、孙项名称
                String mainItemName = matter.getMainItemName() != null ? String.format("%s.%s", matter.getMainItemCode(),matter.getMainItemName()) : "";
                String subItemName = matter.getSubItemName() != null ? String.format("%s.%s", matter.getSubItemCode(),matter.getSubItemName()) : "";
                String grandchildItemName = matter.getGrandchildItemName() != null ? String.format("%s.%s", matter.getGrandchildItemCode(),matter.getGrandchildItemName()) : "";

                // 检查是否是新的主项
                if (matter.getMainItemCode() != null && !matter.getMainItemCode().equals(currentMainItemCode)) {

                    XWPFParagraph paragraph = document.createParagraph();
                    // 设置右对齐制表符
                    CTPPr ppr = paragraph.getCTP().isSetPPr() ? paragraph.getCTP().getPPr() : paragraph.getCTP().addNewPPr();
                    CTTabs tabs = ppr.isSetTabs() ? ppr.getTabs() : ppr.addNewTabs();
                    CTTabStop tab = tabs.addNewTab();
                    tab.setVal(STTabJc.RIGHT);
                    tab.setLeader(STTabTlc.DOT);
                    tab.setPos(8000); // 设置在页面右侧位置

                    XWPFRun run = paragraph.createRun();
                    // 输出主项 - 只显示主项名称
                    run.setText(mainItemName);

                    // 添加制表符和页码
                    run.addTab();
                    run.setText(String.format("（%d）", i));

                    currentMainItemCode = matter.getMainItemCode();
                    currentSubItemCode = 0L; // 重置孙项编号

                }

                // 检查是否是新的子项
                if (matter.getSubItemCode() != null && !matter.getSubItemCode().equals(currentSubItemCode)) {

                    XWPFParagraph paragraph = document.createParagraph();
                    // 设置缩进
                    paragraph.setIndentationFirstLine(283); // 0.5cm缩进

                    // 设置右对齐制表符
                    CTPPr ppr = paragraph.getCTP().isSetPPr() ? paragraph.getCTP().getPPr() : paragraph.getCTP().addNewPPr();
                    CTTabs tabs = ppr.isSetTabs() ? ppr.getTabs() : ppr.addNewTabs();
                    CTTabStop tab = tabs.addNewTab();
                    tab.setVal(STTabJc.RIGHT);
                    tab.setLeader(STTabTlc.DOT);
                    tab.setPos(8000); // 设置在页面右侧位置

                    XWPFRun run = paragraph.createRun();

                    // 显示子项名称
                    run.setText(subItemName);

                    // 添加制表符和页码
                    run.addTab();
                    run.setText(String.format("（%d）", i));

                    currentSubItemCode = matter.getSubItemCode();
                }

                // 输出孙项（如果存在）
                if (matter.getGrandchildItemCode() != null && !grandchildItemName.isEmpty()) {
                    XWPFParagraph paragraph = document.createParagraph();

                    // 设置更大缩进
                    paragraph.setIndentationFirstLine(567); // 1cm缩进

                    // 设置右对齐制表符
                    CTPPr ppr = paragraph.getCTP().isSetPPr() ? paragraph.getCTP().getPPr() : paragraph.getCTP().addNewPPr();
                    CTTabs tabs = ppr.isSetTabs() ? ppr.getTabs() : ppr.addNewTabs();
                    CTTabStop tab = tabs.addNewTab();
                    tab.setVal(STTabJc.RIGHT);
                    tab.setLeader(STTabTlc.DOT);
                    tab.setPos(8000); // 设置在页面右侧位置

                    XWPFRun run = paragraph.createRun();

                    // 显示完整的孙项名称
                    run.setText(grandchildItemName);

                    // 添加制表符和页码
                    run.addTab();
                    run.setText(String.format("（%d）", i));
                }
            }

            // 将文档转换为字节数组
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.write(out);
            return out.toByteArray();
        }
    }


    /**
     * 根据Matter列表导出Word文档（纯函数版本）
     * 每个Matter生成一个表格和相关材料信息
     *
     * @param matters Matter列表
     * @param materialsMap 材料映射，key为Matter ID，value为材料
     * @param approvalDiagramsMap 审批流程图映射，key为Matter ID，value为审批流程图
     * @param businessDiagramsMap 业务流程图映射，key为Matter ID，value为业务流程图
     * @return Word文档字节数组
     * @throws IOException IO异常
     */
    public byte[] exportMattersToWord( List<Matter> matters,
                                       Map<Long, Material> materialsMap,
                                       Map<Long, ApprovalProcessDiagram> approvalDiagramsMap,
                                       Map<Long, BusinessProcessDiagram> businessDiagramsMap) throws IOException {
        sortedMatters(matters);

        try (XWPFDocument document = new XWPFDocument()) {
            // 设置页面大小为 A3，横向
            setPageSizeA3Landscape(document);

            // 为每个Matter ID生成表格和材料信息
            for (Matter matter: matters) {
                if (matter != null) {
                    // 添加Matter表格
                    addMatterTable(document, matter,  materialsMap);
                    // 添加分页符
                    addSeparator(document);

                    addImageTable(document, matter, approvalDiagramsMap, businessDiagramsMap);
                    // 添加分页符
                    addSeparator(document);
                }
            }

            // 将文档转换为字节数组
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.write(out);
            return out.toByteArray();
        }
    }

    /**
     * 为单个Matter添加表格和材料信息（严格按照用户描述的格式）
     */
    private void addMatterTable(XWPFDocument document, Matter matter,Map<Long, Material> materialsMap) {
        // 添加主标题
        XWPFParagraph title = document.createParagraph();
        title.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun titleRun = title.createRun();
        titleRun.setText(TITLE_TEXT);
        titleRun.setBold(true);
        titleRun.setFontSize(16);
        titleRun.addBreak();

        // 获取材料列表
        List<Material> materials = new java.util.ArrayList<>();
        if (matter.getMaterialIds() != null && !matter.getMaterialIds().isEmpty()) {
            for (Long materialId : matter.getMaterialIds()) {
                Material material = materialsMap.get(materialId);
                if (material != null && material.getValid()) {
                    materials.add(material);
                }
            }
        }

        // 计算需要的行数（基础3行 + 材料行数 - 1，因为1行可以显示1个材料）
        int materialSize = Math.max(1, materials.size());
        int rowSize = 2 + materialSize; // 表头2行 + 至少1行数据

        // 创建主表格
        XWPFTable mainTable = document.createTable(rowSize, 16);
        setTableStyle(mainTable);

        // 第一行：主表头
        setTableCellText(mainTable.getRow(ROW_HEADER_MAIN).getCell(COL_MAIN_ITEM_NAME), HEADER_MAIN_ITEM_NAME);
        setTableCellText(mainTable.getRow(ROW_HEADER_MAIN).getCell(COL_SUB_ITEM_NAME), HEADER_SUB_ITEM_NAME);
        setTableCellText(mainTable.getRow(ROW_HEADER_MAIN).getCell(COL_GRANDCHILD_ITEM_NAME), HEADER_GRANDCHILD_ITEM_NAME);
        setTableCellText(mainTable.getRow(ROW_HEADER_MAIN).getCell(COL_BASES), HEADER_BASES);
        // 经办所需材料占8列 (4-11列)
        setTableCellText(mainTable.getRow(ROW_HEADER_MAIN).getCell(COL_MATERIAL_START), HEADER_MATERIALS);
        for (int i = COL_MATERIAL_START + 1; i <= COL_MATERIAL_END; i++) {
            setTableCellText(mainTable.getRow(ROW_HEADER_MAIN).getCell(i), "");
        }
        setTableCellText(mainTable.getRow(ROW_HEADER_MAIN).getCell(COL_LEGAL_TIME_LIMIT), HEADER_LEGAL_TIME_LIMIT);
        setTableCellText(mainTable.getRow(ROW_HEADER_MAIN).getCell(COL_COMMITTED_TIME_LIMIT), "");
        setTableCellText(mainTable.getRow(ROW_HEADER_MAIN).getCell(COL_APPROVAL_LEVEL), HEADER_APPROVAL_LEVEL);
        setTableCellText(mainTable.getRow(ROW_HEADER_MAIN).getCell(COL_PROVINCIAL_DEPARTMENT), HEADER_PROVINCIAL_DEPARTMENT);

        // 第二行：经办所需材料的子表头
        for (int i = 0; i < 4; i++) {
            setTableCellText(mainTable.getRow(ROW_HEADER_SUB).getCell(i), "");
        }
        setTableCellText(mainTable.getRow(ROW_HEADER_SUB).getCell(COL_MATERIAL_ORIGINAL_DETAIL), HEADER_MATERIAL_ORIGINAL_DETAIL);
        setTableCellText(mainTable.getRow(ROW_HEADER_SUB).getCell(COL_MATERIAL_CURRENT_DETAIL), HEADER_MATERIAL_CURRENT_DETAIL);
        setTableCellText(mainTable.getRow(ROW_HEADER_SUB).getCell(COL_MATERIAL_REVIEW_POINT), HEADER_MATERIAL_REVIEW_POINT);
        setTableCellText(mainTable.getRow(ROW_HEADER_SUB).getCell(COL_MATERIAL_AUTO_APPROVAL_CRITERIA), HEADER_MATERIAL_AUTO_APPROVAL_CRITERIA);
        setTableCellText(mainTable.getRow(ROW_HEADER_SUB).getCell(COL_MATERIAL_SHARED), HEADER_MATERIAL_SHARED);
        setTableCellText(mainTable.getRow(ROW_HEADER_SUB).getCell(COL_MATERIAL_SOURCE), HEADER_MATERIAL_SOURCE);
        setTableCellText(mainTable.getRow(ROW_HEADER_SUB).getCell(COL_MATERIAL_PROCESSING_METHOD), HEADER_MATERIAL_PROCESSING_METHOD);
        setTableCellText(mainTable.getRow(ROW_HEADER_SUB).getCell(COL_MATERIAL_ELIGIBLE_FOR_PROMISE), HEADER_MATERIAL_ELIGIBLE_FOR_PROMISE);
        setTableCellText(mainTable.getRow(ROW_HEADER_SUB).getCell(COL_LEGAL_TIME_LIMIT), HEADER_COMMITTED_TIME_LIMIT);
        setTableCellText(mainTable.getRow(ROW_HEADER_SUB).getCell(COL_COMMITTED_TIME_LIMIT), HEADER_PROMISED_TIME_LIMIT);
        for (int i = COL_PROVINCIAL_DEPARTMENT - 1; i < COL_PROVINCIAL_DEPARTMENT + 1; i++) {
            setTableCellText(mainTable.getRow(ROW_HEADER_SUB).getCell(i), "");
        }

        // 填充基础信息 (在第2行，因为第0和第1行是表头)
        setTableCellText(mainTable.getRow(ROW_DATA_START).getCell(COL_MAIN_ITEM_NAME), matter.getMainItemName() != null ? matter.getMainItemName() : "");
        setTableCellText(mainTable.getRow(ROW_DATA_START).getCell(COL_SUB_ITEM_NAME), matter.getSubItemName() != null ? matter.getSubItemName() : "");
        setTableCellText(mainTable.getRow(ROW_DATA_START).getCell(COL_GRANDCHILD_ITEM_NAME), matter.getGrandchildItemName() != null ? matter.getGrandchildItemName() : "");

        String bases = matter.getBases() != null && !matter.getBases().isEmpty()
                ? String.join("\n", matter.getBases())
                : "";
        setTableCellText(mainTable.getRow(ROW_DATA_START).getCell(COL_BASES), bases);

        // 法定时限
        setTableCellText(mainTable.getRow(ROW_DATA_START).getCell(COL_LEGAL_TIME_LIMIT), matter.getLegalTimeLimit() != null ? matter.getLegalTimeLimit().toString() + "个工作日" : "");

        // 承诺时限
        setTableCellText(mainTable.getRow(ROW_DATA_START).getCell(COL_COMMITTED_TIME_LIMIT), matter.getCommittedTimeLimit() != null ? matter.getCommittedTimeLimit().toString() + "个工作日" : "");

        // 审批层级
        setTableCellText(mainTable.getRow(ROW_DATA_START).getCell(COL_APPROVAL_LEVEL), matter.getApprovalLevel() != null ? matter.getApprovalLevel().getDescription() : "");

        // 省厅对口指导处室（单位）
        setTableCellText(mainTable.getRow(ROW_DATA_START).getCell(COL_PROVINCIAL_DEPARTMENT), matter.getProvincialDepartmentOffice() != null ? matter.getProvincialDepartmentOffice().getDescription() : "");

        // 填充材料信息，每个材料一行
        for (int i = 0; i < Math.max(1, materials.size()); i++) {
            int rowIdx = i + ROW_DATA_START; // 从数据起始行开始填入材料信息

            // 如果材料存在，则填充材料信息
            if (i < materials.size()) {
                Material material = materials.get(i);
                setTableCellText(mainTable.getRow(rowIdx).getCell(COL_MATERIAL_ORIGINAL_DETAIL), material.getMaterialDetail() != null ? material.getMaterialDetail() : "");
                setTableCellText(mainTable.getRow(rowIdx).getCell(COL_MATERIAL_CURRENT_DETAIL), material.getMaterialDetail() != null ? material.getMaterialDetail() : "");
                setTableCellText(mainTable.getRow(rowIdx).getCell(COL_MATERIAL_REVIEW_POINT), material.getReviewPoint() != null ? material.getReviewPoint() : "");
                setTableCellText(mainTable.getRow(rowIdx).getCell(COL_MATERIAL_AUTO_APPROVAL_CRITERIA), material.getAutoApprovalCriteria() != null ? material.getAutoApprovalCriteria() : "");
                setTableCellText(mainTable.getRow(rowIdx).getCell(COL_MATERIAL_SHARED), material.getShared() != null ? (material.getShared() ? "是" : "否") : "");
                setTableCellText(mainTable.getRow(rowIdx).getCell(COL_MATERIAL_SOURCE), material.getMaterialSource() != null ? material.getMaterialSource().toString() : "");
                setTableCellText(mainTable.getRow(rowIdx).getCell(COL_MATERIAL_PROCESSING_METHOD), material.getProcessingMethodAndInfoAccess() != null ? material.getProcessingMethodAndInfoAccess().toString() : "");
                setTableCellText(mainTable.getRow(rowIdx).getCell(COL_MATERIAL_ELIGIBLE_FOR_PROMISE), material.getEligibleForPromise() != null ? (material.getEligibleForPromise() ? "是" : "否") : "");
            } else {
                // 如果没有材料，保持空行
                for (int col = COL_MATERIAL_START; col <= COL_MATERIAL_END; col++) {
                    setTableCellText(mainTable.getRow(rowIdx).getCell(col), "");
                }
            }
        }

        // 合并表头单元格 & 材料
        mergeColumnsInRow(mainTable, ROW_HEADER_MAIN, COL_MATERIAL_START, MATERIAL_COLUMN_COUNT);
        mergeColumnsInRow(mainTable, ROW_HEADER_MAIN, COL_LEGAL_TIME_LIMIT, 2);

        for (int col = 0; col < 4; col++) {
            mergeRowsInColumn(mainTable, col, ROW_HEADER_MAIN, 2);
            mergeRowsInColumn(mainTable, col, ROW_DATA_START, materialSize);
        }
        mergeRowsInColumn(mainTable, COL_LEGAL_TIME_LIMIT, ROW_DATA_START, materialSize);
        mergeRowsInColumn(mainTable, COL_COMMITTED_TIME_LIMIT, ROW_DATA_START, materialSize);
        mergeRowsInColumn(mainTable, COL_APPROVAL_LEVEL, ROW_HEADER_MAIN, 2);
        mergeRowsInColumn(mainTable, COL_APPROVAL_LEVEL, ROW_DATA_START, materialSize);
        mergeRowsInColumn(mainTable, COL_PROVINCIAL_DEPARTMENT, ROW_HEADER_MAIN, 2);
        mergeRowsInColumn(mainTable, COL_PROVINCIAL_DEPARTMENT, ROW_DATA_START, materialSize);

    }

    private void addImageTable(XWPFDocument document, Matter matter,  Map<Long, ApprovalProcessDiagram> approvalDiagramsMap,
                               Map<Long, BusinessProcessDiagram> businessDiagramsMap) {
        // 添加图片表格标题
        XWPFParagraph imageTitle = document.createParagraph();
        imageTitle.setAlignment(ParagraphAlignment.CENTER);

        // 创建2行2列的图片表格
        XWPFTable imageTable = document.createTable(2, 2);

        // 设置表格居中
        imageTable.setTableAlignment(TableRowAlign.CENTER);

        // 设置表格样式
        setTableStyle(imageTable);

        // 设置表格列宽（可根据需要调整）
        imageTable.setWidth("80%");

        // 查询审批流程图
        if (matter.getApprovalProcessDiagramId() != null) {
            ApprovalProcessDiagram approvalDiagram = approvalDiagramsMap.get(matter.getApprovalProcessDiagramId());
            if (approvalDiagram != null) {
                // 第一行第一列：A图片名
                setTableCellText(imageTable.getRow(0).getCell(0), approvalDiagram.getImageName() != null ? approvalDiagram.getImageName() : "审批流程图");

                // 第二行第一列：A图片
                if (approvalDiagram.getImageData() != null) {
                    insertImageToCell(document, imageTable.getRow(1).getCell(0), approvalDiagram.getImageData(), approvalDiagram.getImageType());
                } else {
                    setTableCellText(imageTable.getRow(1).getCell(0), "无图片数据");
                }
            } else {
                setTableCellText(imageTable.getRow(0).getCell(0), "审批流程图（未找到）");
                setTableCellText(imageTable.getRow(1).getCell(0), "");
            }
        } else {
            setTableCellText(imageTable.getRow(0).getCell(0), "审批流程图（未设置）");
            setTableCellText(imageTable.getRow(1).getCell(0), "");
        }

        // 查询业务流程图
        if (matter.getBusinessProcessDiagramId() != null) {
            BusinessProcessDiagram businessDiagram = businessDiagramsMap.get(matter.getBusinessProcessDiagramId());
            if (businessDiagram != null) {
                // 第一行第二列：B图片名
                setTableCellText(imageTable.getRow(0).getCell(1), businessDiagram.getImageName() != null ? businessDiagram.getImageName() : "业务流程图");

                // 第二行第二列：B图片
                if (businessDiagram.getImageData() != null) {
                    insertImageToCell(document, imageTable.getRow(1).getCell(1), businessDiagram.getImageData(), businessDiagram.getImageType());
                } else {
                    setTableCellText(imageTable.getRow(1).getCell(1), "无图片数据");
                }
            } else {
                setTableCellText(imageTable.getRow(0).getCell(1), "业务流程图（未找到）");
                setTableCellText(imageTable.getRow(1).getCell(1), "");
            }
        } else {
            setTableCellText(imageTable.getRow(0).getCell(1), "业务流程图（未设置）");
            setTableCellText(imageTable.getRow(1).getCell(1), "");
        }

    }


    /**
     * 根据主项、子项、孙项编号对Matter列表进行排序
     *
     * @param matters 需要排序的Matter对象列表
     * @return 按编号排序的Matter对象列表
     */
    private void sortedMatters(List<Matter> matters) {
        // 根据主项、子项、孙项编号排序
        matters.sort(Comparator
                .comparing(Matter::getMainItemCode, Comparator.nullsFirst(Comparator.naturalOrder()))
                .thenComparing(Matter::getSubItemCode, Comparator.nullsFirst(Comparator.naturalOrder()))
                .thenComparing(Matter::getGrandchildItemCode, Comparator.nullsFirst(Comparator.naturalOrder())));
    }

    private void insertImageToCell(XWPFDocument document, XWPFTableCell cell, byte[] imageData, ProcessDiagram.ImageType imageType) {
        // 清空单元格中的内容
        cell.removeParagraph(0);
        XWPFParagraph paragraph = cell.addParagraph();
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = paragraph.createRun();

        // 根据图片类型确定图片格式
        int pictureType = XWPFDocument.PICTURE_TYPE_PNG; // 默认为PNG
        if (imageType != null) {
            switch (imageType) {
                case JPEG:
                    pictureType = XWPFDocument.PICTURE_TYPE_JPEG;
                    break;
                case PNG:
                    pictureType = XWPFDocument.PICTURE_TYPE_PNG;
                    break;
                case GIF:
                    pictureType = XWPFDocument.PICTURE_TYPE_GIF;
                    break;
                case BMP:
                    pictureType = XWPFDocument.PICTURE_TYPE_BMP;
                    break;
                default:
                    pictureType = XWPFDocument.PICTURE_TYPE_PNG;
                    break;
            }
        }

        try {
            // 插入图片到单元格
            String imageName = "image";
            run.addPicture(
                    new java.io.ByteArrayInputStream(imageData),
                    pictureType,
                    imageName,
                    Units.toEMU(200), // 宽度
                    Units.toEMU(150)  // 高度
            );
        } catch (Exception e) {
            // 如果插入图片失败，则显示错误信息
            run.setText("图片加载失败");
        }
    }

    /**
     * 指定行，合并列
     * @param table 表格
     * @param row 行索引
     * @param startCol 起始列索引
     * @param colCount 合并列数
     */
    private void mergeColumnsInRow(XWPFTable table, int row, int startCol, int colCount) {
        for (int i = 0; i < colCount; i++) {
            int colIndex = startCol + i;
            if (i == 0) {
                // 第一个单元格标记为合并开始
                table.getRow(row).getCell(colIndex).getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.RESTART);
            } else {
                // 其他单元格标记为合并继续
                table.getRow(row).getCell(colIndex).getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.CONTINUE);
            }
        }
    }

    /**
     * 指定列，合并行
     * @param table 表格
     * @param col 列索引
     * @param startRow 起始行索引
     * @param rowCount 合并行数
     */
    private void mergeRowsInColumn(XWPFTable table, int col, int startRow, int rowCount) {
        for (int i = 0; i < rowCount; i++) {
            int rowIndex = startRow + i;
            if (i == 0) {
                // 第一个单元格标记为合并开始
                table.getRow(rowIndex).getCell(col).getCTTc().addNewTcPr().addNewVMerge().setVal(STMerge.RESTART);
            } else {
                // 其他单元格标记为合并继续
                table.getRow(rowIndex).getCell(col).getCTTc().addNewTcPr().addNewVMerge().setVal(STMerge.CONTINUE);
            }
        }
    }

    /**
     * 添加分页符
     */
    private static void addSeparator(XWPFDocument document) {
        XWPFParagraph separator = document.createParagraph();
        separator.setPageBreak(true);
    }

    /**
     * 设置表格样式
     */
    private static void setTableStyle(XWPFTable table) {
        // 设置表格宽度
        table.setWidth("100%");

        // 设置表格边框
        CTTblBorders borders = CTTblBorders.Factory.newInstance();
        borders.addNewTop().setVal(STBorder.SINGLE);
        borders.addNewLeft().setVal(STBorder.SINGLE);
        borders.addNewBottom().setVal(STBorder.SINGLE);
        borders.addNewRight().setVal(STBorder.SINGLE);
        borders.addNewInsideH().setVal(STBorder.SINGLE);
        borders.addNewInsideV().setVal(STBorder.SINGLE);

        // 设置表头样式
        XWPFTableRow headerRow = table.getRow(0);
        if (headerRow != null) {
            for (XWPFTableCell cell : headerRow.getTableCells()) {
                // 设置单元格内容居中
                CTTc cttc = cell.getCTTc();
                CTP ctp = cttc.getPList().get(0);
                CTPPr ctppr = ctp.getPPr();
                if (ctppr == null) ctppr = ctp.addNewPPr();
                CTJc ctjc = ctppr.getJc();
                if (ctjc == null) ctjc = ctppr.addNewJc();
                ctjc.setVal(STJc.CENTER);

                // 设置字体加粗
                for (XWPFParagraph p : cell.getParagraphs()) {
                    for (XWPFRun r : p.getRuns()) {
                        r.setBold(true);
                    }
                }
            }
        }
    }

    /**
     * 设置单元格文本内容并居中
     */
    private static void setTableCellText(XWPFTableCell cell, String text) {
        XWPFParagraph paragraph = cell.getParagraphs().get(0);
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = paragraph.createRun();
        if (text != null) {
            // 处理换行符，确保在Word中正确换行
            String[] lines = text.split("\n");
            for (int i = 0; i < lines.length; i++) {
                if (i > 0) {
                    run.addBreak(); // 添加Word换行符
                }
                run.setText(lines[i]);
            }
        }
    }

    /**
     * 设置页面大小为 A3，横向
     */
    private void setPageSizeA3Landscape(XWPFDocument document) {
        // 获取文档的主样式表
        CTSectPr sectPr = document.getDocument().getBody().getSectPr();
        if (sectPr == null) {
            sectPr = document.getDocument().getBody().addNewSectPr();
        }

        // 设置页面大小
        CTPageSz pageSize = sectPr.isSetPgSz() ? sectPr.getPgSz() : sectPr.addNewPgSz();
        pageSize.setW(16838); // A3 宽度（twip）
        pageSize.setH(23838); // A3 高度（twip）
        pageSize.setOrient(STPageOrientation.LANDSCAPE); // 横向

        // 设置页边距
        CTPageMar margins = sectPr.isSetPgMar() ? sectPr.getPgMar() : sectPr.addNewPgMar();
        margins.setLeft(720);   // 0.5 英寸
        margins.setRight(720);
        margins.setTop(720);
        margins.setBottom(720);
    }


}

