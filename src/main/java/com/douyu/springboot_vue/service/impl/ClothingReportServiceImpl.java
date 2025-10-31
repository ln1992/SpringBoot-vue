package com.douyu.springboot_vue.service.impl;

import com.douyu.springboot_vue.entities.Clothing;
import com.douyu.springboot_vue.entities.ClothingStockRecord;
import com.douyu.springboot_vue.repository.ClothingRepository;
import com.douyu.springboot_vue.repository.ClothingStockRecordRepository;
import com.douyu.springboot_vue.service.ClothingReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 服装报表服务实现类
 */
@Service
public class ClothingReportServiceImpl implements ClothingReportService {

    private final ClothingRepository clothingRepository;
    private final ClothingStockRecordRepository clothingStockRecordRepository;

    @Autowired
    public ClothingReportServiceImpl(ClothingRepository clothingRepository,
                                     ClothingStockRecordRepository clothingStockRecordRepository) {
        this.clothingRepository = clothingRepository;
        this.clothingStockRecordRepository = clothingStockRecordRepository;
    }

    @Override
    public Map<String, Object> getInventorySummaryReport(Date startDate, Date endDate) {
        Map<String, Object> result = new HashMap<>();
        
        // 获取所有服装
        List<Clothing> clothings = clothingRepository.findAll();
        
        // 统计总数
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalItems", clothings.size()); // 服装总数量
        
        // 计算所有服装的总入库数量作为服装总量（件）
        long totalInbound = clothings.stream()
            .mapToLong(c -> c.getTotalQuantityBySize().values().stream().mapToLong(Long::longValue).sum())
            .sum();
        summary.put("activeItems", totalInbound); // 服装总量（件）= 所有服装的总入库数量
    
        summary.put("totalStock", clothings.stream().mapToLong(c -> c.getCurrentStockBySize().values().stream().mapToLong(Long::longValue).sum()).sum()); // 库存总数
        
        result.put("summary", summary);
        
        // 按品牌统计
        Map<String, Map<String, Object>> brandStatsMap = new HashMap<>();
        for (Clothing clothing : clothings) {
            String brand = clothing.getBrand() != null ? clothing.getBrand() : "未分类";
            Map<String, Object> brandStat = brandStatsMap.computeIfAbsent(brand, k -> {
                Map<String, Object> stat = new HashMap<>();
                stat.put("brand", brand);
                stat.put("itemCount", 0);
                stat.put("totalStock", 0L);
                return stat;
            });
            
            brandStat.put("itemCount", (Integer) brandStat.get("itemCount") + 1);
            brandStat.put("totalStock", (Long) brandStat.get("totalStock") + clothing.getCurrentStockBySize().values().stream().mapToLong(Long::longValue).sum());
        }
        
        result.put("byBrand", brandStatsMap.values());
        
        return result;
    }

    @Override
    public List<Clothing> getLowStockReport() {
        return clothingRepository.findByCurrentStockLessThanEqualSafetyStock();
    }

    @Override
    public Map<String, Object> getInboundOutboundReport(Date startDate, Date endDate) {
        Map<String, Object> result = new HashMap<>();
        
        // 如果没有提供时间范围，则查询所有记录
        List<Object[]> inboundStats;
        List<Object[]> outboundStats;
        
        // 总是使用时间范围查询，如果为null则使用数据库默认范围
        if (startDate == null && endDate == null) {
            // 查询所有记录
            inboundStats = clothingStockRecordRepository.findInboundStatsAll();
            outboundStats = clothingStockRecordRepository.findOutboundStatsAll();
        } else {
            // 使用默认时间范围
            if (startDate == null) {
                startDate = new Date(System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000); // 默认一年前
            }
            if (endDate == null) {
                endDate = new Date(); // 默认当前时间
            }
            
            inboundStats = clothingStockRecordRepository.findInboundStats(startDate, endDate);
            outboundStats = clothingStockRecordRepository.findOutboundStats(startDate, endDate);
        }
        
        // 构建结果
        Map<String, Object> summary = new HashMap<>();
        long totalInbound = inboundStats.stream().mapToLong(arr -> (Long) arr[1]).sum();
        long totalOutbound = outboundStats.stream().mapToLong(arr -> (Long) arr[1]).sum();
        
        summary.put("totalInbound", totalInbound);
        summary.put("totalOutbound", totalOutbound);
        summary.put("netChange", totalInbound - totalOutbound);
        
        result.put("summary", summary);
        
        // 合并出入库详情
        Map<String, Map<String, Object>> detailMap = new HashMap<>();
        
        // 处理入库数据
        for (Object[] inbound : inboundStats) {
            String clothingName = (String) inbound[0];
            Long quantity = (Long) inbound[1];
            
            Map<String, Object> detail = detailMap.computeIfAbsent(clothingName, k -> {
                Map<String, Object> d = new HashMap<>();
                d.put("clothingName", clothingName);
                d.put("inboundQuantity", 0L);
                d.put("outboundQuantity", 0L);
                return d;
            });
            
            detail.put("inboundQuantity", (Long) detail.get("inboundQuantity") + quantity);
        }
        
        // 处理出库数据
        for (Object[] outbound : outboundStats) {
            String clothingName = (String) outbound[0];
            Long quantity = (Long) outbound[1];
            
            Map<String, Object> detail = detailMap.computeIfAbsent(clothingName, k -> {
                Map<String, Object> d = new HashMap<>();
                d.put("clothingName", clothingName);
                d.put("inboundQuantity", 0L);
                d.put("outboundQuantity", 0L);
                return d;
            });
            
            detail.put("outboundQuantity", (Long) detail.get("outboundQuantity") + quantity);
        }
        
        result.put("details", detailMap.values());
        
        return result;
    }
}