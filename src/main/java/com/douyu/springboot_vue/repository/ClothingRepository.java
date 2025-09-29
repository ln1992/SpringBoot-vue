package com.douyu.springboot_vue.repository;

import com.douyu.springboot_vue.entities.Clothing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 服装实体的数据库访问接口
 */
@Repository
public interface ClothingRepository extends JpaRepository<Clothing, Long> {
    // 可以在此添加自定义查询方法
}