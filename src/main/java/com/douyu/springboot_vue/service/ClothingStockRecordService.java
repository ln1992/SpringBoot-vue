package com.douyu.springboot_vue.service;

import com.douyu.springboot_vue.entities.ClothingStockRecord;
import java.util.List;
import java.util.Optional;

/**
 * 服装库存记录服务接口
 */
public interface ClothingStockRecordService {
    /**
     * 获取所有库存记录
     * @return 库存记录列表
     */
    List<ClothingStockRecord> getAllStockRecords();

    /**
     * 根据ID获取库存记录
     * @param id 库存记录ID
     * @return 库存记录对象（如果存在）
     */
    Optional<ClothingStockRecord> getStockRecordById(Long id);

    /**
     * 保存库存记录
     * @param stockRecord 库存记录对象
     * @return 保存后的库存记录对象
     */
    ClothingStockRecord saveStockRecord(ClothingStockRecord stockRecord);

    /**
     * 删除库存记录
     * @param id 库存记录ID
     */
    void deleteStockRecord(Long id);

    /**
     * 根据服装ID获取库存记录
     * @param clothingId 服装ID
     * @return 库存记录列表
     */
    List<ClothingStockRecord> getStockRecordsByClothingId(Long clothingId);
}