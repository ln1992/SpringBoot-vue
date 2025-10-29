package com.douyu.springboot_vue.repository;

import com.douyu.springboot_vue.entities.Clothing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 服装实体的数据库访问接口
 */
@Repository
public interface ClothingRepository extends JpaRepository<Clothing, Long> {
    // 可以在此添加自定义查询方法
    
    /**
     * 查找低库存的服装（当前库存小于等于安全库存）
     * @return 低库存服装列表
     */
    @Query(value = "SELECT * FROM clothing c WHERE (" +
           "(SELECT COALESCE(SUM(quantity), 0) FROM clothing_current_stock WHERE clothing_id = c.id) " +
           "<= COALESCE(c.safety_stock, 10))", nativeQuery = true)
    List<Clothing> findByCurrentStockLessThanEqualSafetyStock();
}