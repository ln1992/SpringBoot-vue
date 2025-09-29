package com.douyu.springboot_vue.repository;

import com.douyu.springboot_vue.entities.ClothingStockRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 服装库存记录实体的数据库访问接口
 */
@Repository
public interface ClothingStockRecordRepository extends JpaRepository<ClothingStockRecord, Long> {
    /**
     * 根据服装ID查找相关库存记录
     * @param clothingId 服装ID
     * @return 库存记录列表
     */
    List<ClothingStockRecord> findByClothingId(Long clothingId);
}