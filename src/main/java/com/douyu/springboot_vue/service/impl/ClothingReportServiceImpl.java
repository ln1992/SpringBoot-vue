package com.douyu.springboot_vue.service.impl;

import com.douyu.springboot_vue.entities.Clothing;
import com.douyu.springboot_vue.entities.ClothingStockRecord;
import com.douyu.springboot_vue.repository.ClothingRepository;
import com.douyu.springboot_vue.repository.ClothingStockRecordRepository;
import com.douyu.springboot_vue.service.ClothingReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
    
    @Override
    public List<Map<String, Object>> getMonthlyStatsReport(String clothingName, Date startDate, Date endDate) {
        // 如果没有提供时间范围，则使用默认范围（最近一年）
        if (startDate == null) {
            startDate = new Date(System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000); // 默认一年前
        }
        if (endDate == null) {
            endDate = new Date(); // 默认当前时间
        }
        
        List<Object[]> stats = null;
        try {
            stats = clothingStockRecordRepository.findMonthlyStatsByClothingName(
                    clothingName, startDate, endDate);
        } catch (Exception e) {
            System.err.println("查询月度统计报表时发生错误: " + e.getMessage());
            e.printStackTrace();
            throw e; // 重新抛出异常，让Controller处理
        }
        
        // 创建一个包含所有月份的映射
        Map<String, Map<String, Object>> monthlyStatsMap = new HashMap<>();
        
        // 生成指定时间范围内的所有月份
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        cal.set(Calendar.DAY_OF_MONTH, 1); // 设置为月份第一天
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(endDate);
        endCal.set(Calendar.DAY_OF_MONTH, 1); // 设置为月份第一天
        
        // 初始化所有月份的数据为0
        while (!cal.after(endCal)) {
            String month = String.format("%04d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1);
            Map<String, Object> monthStat = new HashMap<>();
            monthStat.put("month", month);
            monthStat.put("inbound", 0L);
            monthStat.put("outbound", 0L);
            monthlyStatsMap.put(month, monthStat);
            
            cal.add(Calendar.MONTH, 1); // 下一个月
        }
        
        // 用实际查询结果填充数据
        for (Object[] stat : stats) {
            if (stat == null || stat.length < 3) {
                System.err.println("跳过无效的统计记录: " + stat);
                continue;
            }
            
            String month = stat[0] != null ? stat[0].toString() : "未知";
            String operationType = stat[1] != null ? stat[1].toString() : "未知";
            // 安全地处理数量值，转换为Long类型
            Long quantity = stat[2] != null ? 
                (stat[2] instanceof Long ? (Long) stat[2] : Long.valueOf(((Number) stat[2]).longValue())) : 0L;
            
            // 更新对应月份的数据
            Map<String, Object> monthStat = monthlyStatsMap.get(month);
            if (monthStat != null) {
                if ("INBOUND".equals(operationType)) {
                    monthStat.put("inbound", (Long) monthStat.get("inbound") + quantity);
                } else if ("OUTBOUND".equals(operationType)) {
                    monthStat.put("outbound", (Long) monthStat.get("outbound") + quantity);
                }
            }
        }
        
        // 按月份排序返回
        List<Map<String, Object>> result = new ArrayList<>(monthlyStatsMap.values());
        result.sort((a, b) -> ((String) a.get("month")).compareTo((String) b.get("month")));
        
        return result;
    }
}