package com.douyu.springboot_vue.repository;

import com.douyu.springboot_vue.entities.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {
    List<Material> findByIsValid(Boolean isValid);

    // 检查材料明细和版本号的组合是否存在
    @Query("SELECT COUNT(m) FROM Material m WHERE m.materialDetail = :materialDetail AND m.version = :version")
    long countByMaterialDetailAndVersion(@Param("materialDetail") String materialDetail, @Param("version") Long version);

    // 检查材料明细和版本号的组合是否存在（排除指定ID）
    @Query("SELECT COUNT(m) FROM Material m WHERE m.materialDetail = :materialDetail AND m.version = :version AND m.id != :id")
    long countByMaterialDetailAndVersionExcludingId(@Param("materialDetail") String materialDetail,
                                                    @Param("version") Long version,
                                                    @Param("id") Long id);

    // 查找特定材料明细和版本号的材料
    Optional<Material> findByMaterialDetailAndVersion(String materialDetail, Long version);

    /**
     * 查询所有唯一的版本号
     * @return 版本号列表
     */
    @Query("SELECT DISTINCT m.version FROM Material m WHERE m.version IS NOT NULL ORDER BY m.version")
    List<Long> findAllVersions();
}
