package com.douyu.springboot_vue.repository;

import com.douyu.springboot_vue.entities.ClothingStockRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
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
    
    /**
     * 查询所有入库统计信息
     * @return 入库统计信息列表，包含服装名称和总数量
     */
    @Query("SELECT csr.clothingName, SUM(csr.quantity) FROM ClothingStockRecord csr " +
           "WHERE csr.operationType = 'INBOUND' " +
           "GROUP BY csr.clothingName")
    List<Object[]> findInboundStatsAll();
    
    /**
     * 查询所有出库统计信息
     * @return 出库统计信息列表，包含服装名称和总数量
     */
    @Query("SELECT csr.clothingName, SUM(csr.quantity) FROM ClothingStockRecord csr " +
           "WHERE csr.operationType = 'OUTBOUND' " +
           "GROUP BY csr.clothingName")
    List<Object[]> findOutboundStatsAll();
    
    /**
     * 查询指定时间范围内的入库统计信息
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return 入库统计信息列表，包含服装名称和总数量
     */
    @Query("SELECT csr.clothingName, SUM(csr.quantity) FROM ClothingStockRecord csr " +
           "WHERE csr.operationType = 'INBOUND' AND csr.createdTime BETWEEN :startDate AND :endDate " +
           "GROUP BY csr.clothingName")
    List<Object[]> findInboundStats(@Param("startDate") Date startDate, 
                                   @Param("endDate") Date endDate);
    
    /**
     * 查询指定时间范围内的出库统计信息
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return 出库统计信息列表，包含服装名称和总数量
     */
    @Query("SELECT csr.clothingName, SUM(csr.quantity) FROM ClothingStockRecord csr " +
           "WHERE csr.operationType = 'OUTBOUND' AND csr.createdTime BETWEEN :startDate AND :endDate " +
           "GROUP BY csr.clothingName")
    List<Object[]> findOutboundStats(@Param("startDate") Date startDate, 
                                    @Param("endDate") Date endDate);
}