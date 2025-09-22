package com.douyu.springboot_vue.util;

import com.boylegu.springboot_vue.entities.*;
import com.douyu.springboot_vue.entities.*;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MatterWordExportUtilTest {

    // 文件输出开关 - 设置为true启用文件保存，false禁用
    private static final boolean ENABLE_FILE_OUTPUT = false;

    private MatterWordExportUtil matterWordExportUtil;

    private Matter matter1;
    private Matter matter2;
    private Material material1;
    private Material material2;
    private ApprovalProcessDiagram approvalProcessDiagram;
    private BusinessProcessDiagram businessProcessDiagram;

    private Map<Long, Material> materialsMap;

    private Map<Long, ApprovalProcessDiagram> approvalDiagramsMap;

    private Map<Long, BusinessProcessDiagram> businessDiagramsMap;

    @BeforeEach
    void setUp() throws IOException {
        matterWordExportUtil = new MatterWordExportUtil(); // 需要添加这行

        // 创建测试用的 Matter 对象
        matter1 = new Matter();
        matter1.setId(1L);
        matter1.setMainItemName("主事项名称");
        matter1.setSubItemName("子事项名称");
        matter1.setGrandchildItemName("孙事项名称");
        matter1.setMainItemCode(1L);
        matter1.setSubItemCode(1L);
        matter1.setGrandchildItemCode(1L);
        matter1.setVersion(1L);
        matter1.setLegalTimeLimit(10L);
        matter1.setCommittedTimeLimit(5L);
        matter1.setApprovalLevel(Matter.ApprovalLevel.MUNICIPAL);
        matter1.setProvincialDepartmentOffice(Matter.ProvincialDepartmentOffice.PROVINCIAL_DEPARTMENT_ADMINISTRATIVE_APPROVAL);
        matter1.setApprovalProcessDiagramId(101L);
        matter1.setBusinessProcessDiagramId(102L);

        matter2 = new Matter();
        matter2.setId(1L);
        matter2.setMainItemName("主事项名称2");
        matter2.setSubItemName("子事项名称2");
        matter2.setMainItemCode(2L);
        matter2.setSubItemCode(1L);
        matter2.setVersion(1L);

        // 设置材料ID列表
        List<Long> materialIds = Arrays.asList(1001L, 1002L);
        matter1.setMaterialIds(materialIds);

        // 设置经办依据
        List<String> bases = Arrays.asList("依据1", "依据2");
        matter1.setBases(bases);

        // 创建测试用的 Material 对象
        material1 = new Material();
        material1.setId(1001L);
        material1.setMaterialDetail("材料1明细");
        material1.setReviewPoint("审核要点1");
        material1.setAutoApprovalCriteria("智能秒批标准1");
        material1.setShared(true);
        material1.setMaterialSource(Material.MaterialSource.PERSONAL_SUBMISSION);
        material1.setProcessingMethodAndInfoAccess(Material.ProcessingMethodAndInfoAccess.ONLINE_PROCESSING);
        material1.setEligibleForPromise(true);
        material1.setValid(true);

        material2 = new Material();
        material2.setId(1002L);
        material2.setMaterialDetail("材料2明细");
        material2.setReviewPoint("审核要点2");
        material2.setAutoApprovalCriteria("智能秒批标准2");
        material2.setShared(false);
        material2.setMaterialSource(Material.MaterialSource.SYSTEM_AUTO_SHARED);
        material2.setProcessingMethodAndInfoAccess(Material.ProcessingMethodAndInfoAccess.ONLINE_PROCESSING);
        material2.setEligibleForPromise(false);
        material2.setValid(true);

        // 加载测试图片文件
        ClassLoader classLoader = getClass().getClassLoader();

        // 创建测试用的 ApprovalProcessDiagram 对象
        approvalProcessDiagram = new ApprovalProcessDiagram();
        approvalProcessDiagram.setId(101L);
        approvalProcessDiagram.setImageName("审批业务流程图.png");
        approvalProcessDiagram.setImageData("审批流程图数据".getBytes());
        // 从resources/util目录加载图片
        if (ENABLE_FILE_OUTPUT) {
            try (InputStream is = classLoader.getResourceAsStream("util/审批业务流程图.png")) {
                if (is != null) {
                    approvalProcessDiagram.setImageData(is.readAllBytes());
                }
            }
        }
        approvalProcessDiagram.setImageType(ProcessDiagram.ImageType.PNG);

        // 创建测试用的 BusinessProcessDiagram 对象
        businessProcessDiagram = new BusinessProcessDiagram();
        businessProcessDiagram.setId(102L);
        businessProcessDiagram.setImageName("审批业务经办流程图.png");
        businessProcessDiagram.setImageData("业务流程图数据".getBytes());
        // 从resources/util目录加载图片
        if (ENABLE_FILE_OUTPUT) {
            try (InputStream is = classLoader.getResourceAsStream("util/审批业务经办流程图.png")) {
                if (is != null) {
                    businessProcessDiagram.setImageData(is.readAllBytes());
                }
            }
        }
        businessProcessDiagram.setImageType(ProcessDiagram.ImageType.PNG);

        materialsMap = Map.of(
                1001L, material1,
                1002L, material2
        );

        approvalDiagramsMap = Map.of(
                101L, approvalProcessDiagram
        );

        businessDiagramsMap = Map.of(
                102L, businessProcessDiagram
        );
    }

    @Test
    void testExportMattersCatalogToWordSaveToFile() throws IOException {

        List<Matter> matters = Arrays.asList(matter1, matter2);
        byte[] wordBytes = matterWordExportUtil.exportMattersCatalogToWord(matters);

        // 验证返回的字节数组不为空
        assertNotNull(wordBytes);
        assertTrue(wordBytes.length > 0);

        // 只有在启用时才执行文件保存操作
        if (ENABLE_FILE_OUTPUT) {
            // 保存到文件以便手动检查
            try (FileOutputStream fos = new FileOutputStream("test_matters_catalog.docx")) {
                fos.write(wordBytes);
            }
        }

        // 使用 POI 读取生成的文档验证内容
        try (InputStream inputStream = new ByteArrayInputStream(wordBytes);
             XWPFDocument document = new XWPFDocument(inputStream)) {

            // 验证文档不为空
            assertNotNull(document);

            // 验证段落内容
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            assertFalse(paragraphs.isEmpty());

            // 验证包含目录标题
            boolean found = false;
            for (XWPFParagraph paragraph : paragraphs) {
                if (paragraph.getText() != null &&
                        paragraph.getText().contains("全省交通运输系统群众和企业到政府办事事项目录")) {
                    found = true;
                    break;
                }
            }
            assertTrue(found, "文档应该包含目录标题");

            // 验证至少包含两个事项条目
            long matterCount = paragraphs.stream()
                    .map(XWPFParagraph::getText)
                    .filter(text -> text != null && text.matches("\\d+\\..*"))
                    .count();

            assertEquals(5, matterCount, "应该包含两个事项条目");
        }

    }

    @Test
    void testExportMattersCatalogToWordWithMultipleMatters() throws IOException {
        // 准备测试数据
        List<Matter> matters = Arrays.asList(matter1, matter2);

        // 执行导出操作
        byte[] wordBytes = matterWordExportUtil.exportMattersCatalogToWord(matters);

        // 验证返回的字节数组不为空
        assertNotNull(wordBytes);
        assertTrue(wordBytes.length > 0, "导出的目录文档字节数组应该不为空");

        // 使用 POI 读取生成的文档验证内容
        try (InputStream inputStream = new ByteArrayInputStream(wordBytes);
             XWPFDocument document = new XWPFDocument(inputStream)) {

            // 验证文档不为空
            assertNotNull(document, "创建的Word文档不应该为null");

            // 验证段落存在
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            assertFalse(paragraphs.isEmpty(), "文档应该包含段落");

            // 验证标题
            XWPFParagraph titleParagraph = paragraphs.get(0);
            assertNotNull(titleParagraph, "应该存在标题段落");
            String titleText = titleParagraph.getText();
            assertNotNull(titleText, "标题段落文本不应该为null");
            assertTrue(titleText.contains("全省交通运输系统群众和企业到政府办事事项目录"),
                    "文档应该包含正确的标题，实际标题: " + titleText);

            // 验证至少包含3个段落（标题 + 2个事项条目）
            assertTrue(paragraphs.size() >= 3, "文档应该包含标题段落和两个内容段落");

            // 验证第一行内容
            boolean foundFirstMatter = false;
            for (XWPFParagraph paragraph : paragraphs) {
                String text = paragraph.getText();
                if (text != null && text.contains("主事项名称")) {
                    foundFirstMatter = true;
                    assertTrue(text.contains("1."), "第一行内容应该以序号1开始");
                    break;
                }
            }
            assertTrue(foundFirstMatter, "应该找到第一个事项");

            // 验证第二行内容
            boolean foundSecondMatter = false;
            for (XWPFParagraph paragraph : paragraphs) {
                String text = paragraph.getText();
                if (text != null && text.contains("主事项名称2")) {
                    foundSecondMatter = true;
                    assertTrue(text.contains("2."), "第二行内容应该以序号2开始");
                    break;
                }
            }
            assertTrue(foundSecondMatter, "应该找到第二个事项");
        }
    }

    @Test
    void testExportMattersCatalogToWordWithEmptyList() throws IOException {
        // 准备测试数据
        List<Matter> matters = Arrays.asList();

        // 执行导出操作
        byte[] wordBytes = matterWordExportUtil.exportMattersCatalogToWord(matters);

        // 验证返回的字节数组不为空
        assertNotNull(wordBytes);
        assertTrue(wordBytes.length > 0, "导出的目录文档字节数组应该不为空");

        // 使用 POI 读取生成的文档验证内容
        try (InputStream inputStream = new ByteArrayInputStream(wordBytes);
             XWPFDocument document = new XWPFDocument(inputStream)) {

            // 验证文档不为空
            assertNotNull(document, "创建的Word文档不应该为null");

            // 验证段落存在
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            assertFalse(paragraphs.isEmpty(), "文档应该包含段落");

            // 验证标题
            XWPFParagraph titleParagraph = paragraphs.get(0);
            assertNotNull(titleParagraph, "应该存在标题段落");
            String titleText = titleParagraph.getText();
            assertNotNull(titleText, "标题段落文本不应该为null");
            assertTrue(titleText.contains("全省交通运输系统群众和企业到政府办事事项目录"),
                    "文档应该包含正确的标题，实际标题: " + titleText);

            // 验证没有内容行（只有标题和一个空行）
            assertEquals(2, paragraphs.size(), "文档应该只包含标题段落和一个空行");
        }
    }

    @Test
    void testExportMattersToWord() throws IOException {
        // 准备测试数据
        List<Matter> matters = Arrays.asList(matter1);

        byte[] wordBytes = matterWordExportUtil.exportMattersToWord(matters, materialsMap, approvalDiagramsMap, businessDiagramsMap);

        // 验证返回的字节数组不为空
        assertNotNull(wordBytes);
        assertTrue(wordBytes.length > 0);

        // 使用 POI 读取生成的文档验证内容
        try (InputStream inputStream = new ByteArrayInputStream(wordBytes);
             XWPFDocument document = new XWPFDocument(inputStream)) {

            // 验证文档不为空
            assertNotNull(document);

            // 验证段落数量
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            assertFalse(paragraphs.isEmpty());

            // 验证标题
            XWPFParagraph titleParagraph = paragraphs.get(0);
            assertNotNull(titleParagraph);
            assertFalse(titleParagraph.getText().isEmpty());
            assertTrue(titleParagraph.getText().contains("全省交通运输系统群众和企业到政府办事事项"));

            // 验证表格存在
            List<XWPFTable> tables = document.getTables();
            assertFalse(tables.isEmpty());

            // 验证主信息表格
            XWPFTable basicInfoTable = tables.get(0);
            assertNotNull(basicInfoTable);

            // 验证图片表格
            if (tables.size() > 1) {
                XWPFTable imageTable = tables.get(1);
                assertNotNull(imageTable);
            }
        }
    }

    @Test
    void testExportMattersToWordMultipleMatters() throws IOException {
        // 准备测试数据
        List<Matter> matters = Arrays.asList(matter1, matter2);

        byte[] wordBytes = matterWordExportUtil.exportMattersToWord(matters, materialsMap, approvalDiagramsMap, businessDiagramsMap);

        // 验证返回的字节数组不为空
        assertNotNull(wordBytes);
        assertTrue(wordBytes.length > 0);

        // 使用 POI 读取生成的文档验证内容
        try (InputStream inputStream = new ByteArrayInputStream(wordBytes);
             XWPFDocument document = new XWPFDocument(inputStream)) {

            // 验证文档不为空
            assertNotNull(document);

            // 验证段落内容
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            assertFalse(paragraphs.isEmpty());

            // 验证包含两个主标题（每个Matter一个）
            long titleCount = 0;
            for (XWPFParagraph paragraph : paragraphs) {
                if (paragraph.getText() != null &&
                        paragraph.getText().contains("全省交通运输系统群众和企业到政府办事事项")) {
                    titleCount++;
                }
            }
            assertEquals(2, titleCount, "应该包含两个主标题");

            // 验证表格存在
            List<XWPFTable> tables = document.getTables();
            assertFalse(tables.isEmpty(), "应该包含表格");
        }
    }
    @Test
    void testExportMattersToWordSaveToFile() throws IOException {

        List<Matter> matters = Arrays.asList(matter1, matter1);

        byte[] wordBytes = matterWordExportUtil.exportMattersToWord(matters, materialsMap, approvalDiagramsMap, businessDiagramsMap);

        // 验证返回的字节数组不为空
        assertNotNull(wordBytes);
        assertTrue(wordBytes.length > 0);

        // 只有在启用时才执行文件保存操作
        if (ENABLE_FILE_OUTPUT) {
            // 保存到文件以便手动检查
            try (FileOutputStream fos = new FileOutputStream("test_matters.docx")) {
                fos.write(wordBytes);
            }
        }

        // 使用 POI 读取生成的文档验证内容
        try (InputStream inputStream = new ByteArrayInputStream(wordBytes);
             XWPFDocument document = new XWPFDocument(inputStream)) {

            // 验证文档不为空
            assertNotNull(document);

            // 验证段落数量应该很少（只有很少的内容或空内容）
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            // 空列表应该几乎没有内容
            assertTrue(paragraphs.size() < 15, "空列表导出的文档段落数量应该很少，实际有" + paragraphs.size() + "个段落");
        }

    }

    @Test
    void testExportMattersToWordWithNullList() {
        // 验证传入null列表时抛出异常
        assertThrows(NullPointerException.class, () -> {
            matterWordExportUtil.exportMattersToWord((List<Matter>) null, null, null, null);
        });
    }

}