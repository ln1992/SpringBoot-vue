package com.douyu.springboot_vue.service;

import com.douyu.springboot_vue.entities.Clothing;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 服装报表服务接口
 */
@Service
public interface ClothingReportService {
    /**
     * 获取库存汇总报表
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return 库存汇总报表数据，包含：
     *         - summary: 汇总数据
     *           - totalItems: 服装数量
     *           - activeItems: 服装总量（件）
     *           - totalStock: 库存总数
     *         - byBrand: 按品牌统计的数据
     */
    Map<String, Object> getInventorySummaryReport(Date startDate, Date endDate);

    /**
     * 获取低库存报表
     * @return 低库存服装列表
     */
    List<Clothing> getLowStockReport();

    /**
     * 获取出入库统计报表
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return 出入库统计报表数据
     */
    Map<String, Object> getInboundOutboundReport(Date startDate, Date endDate);
}