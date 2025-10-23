package com.douyu.springboot_vue.service;

import com.douyu.springboot_vue.entities.Clothing;
import com.douyu.springboot_vue.entities.ClothingStockRecord;

import java.util.List;
import java.util.Optional;

/**
 * 服装服务接口
 */
public interface ClothingService {
    /**
     * 获取所有服装
     * @return 服装列表
     */
    List<Clothing> getAllClothings();

    /**
     * 根据ID获取服装
     * @param id 服装ID
     * @return 服装对象（如果存在）
     */
    Optional<Clothing> getClothingById(Long id);

    /**
     * 创建服装
     * @param clothing 服装对象
     * @return 创建后的服装对象
     */
    Clothing createClothing(Clothing clothing);

    /**
     * 更新服装
     * @param id 服装ID
     * @param clothing 服装对象
     * @return 更新后的服装对象
     */
    Clothing updateClothing(Long id, Clothing clothing);

    /**
     * 删除服装
     * @param id 服装ID
     */
    void deleteClothing(Long id);

    /**
     * 上线服装
     * @param id 服装ID
     */
    void activateClothing(Long id);

    /**
     * 下线服装
     * @param id 服装ID
     */
    void deactivateClothing(Long id);

    /**
     * 入库操作
     * @param clothingId 服装ID
     * @param size 尺寸
     * @param quantity 数量
     * @param operator 操作员
     * @return 操作是否成功
     */
    boolean inboundClothing(Long clothingId, Clothing.Size size, Long quantity, String operator);

    /**
     * 出库操作
     * @param clothingId 服装ID
     * @param size 尺寸
     * @param quantity 数量
     * @param operator 操作员
     * @return 操作是否成功
     */
    boolean outboundClothing(Long clothingId, Clothing.Size size, Long quantity, String operator);

    /**
     * 批量入库操作
     * @param clothingId 服装ID
     * @param sizeQuantities 尺寸和数量的映射
     * @param operator 操作员
     * @return 操作是否成功
     */
    boolean inboundClothingBatch(Long clothingId, java.util.Map<Clothing.Size, Long> sizeQuantities, String operator);

    /**
     * 批量出库操作
     * @param clothingId 服装ID
     * @param sizeQuantities 尺寸和数量的映射
     * @param operator 操作员
     * @return 操作是否成功
     */
    boolean outboundClothingBatch(Long clothingId, java.util.Map<Clothing.Size, Long> sizeQuantities, String operator);

    /**
     * 获取指定尺寸的当前库存
     * @param clothingId 服装ID
     * @param size 尺寸
     * @return 当前库存数量
     */
    Long getCurrentStockBySize(Long clothingId, Clothing.Size size);

    /**
     * 获取指定尺寸的总入库数量
     * @param clothingId 服装ID
     * @param size 尺寸
     * @return 总入库数量
     */
    Long getTotalQuantityBySize(Long clothingId, Clothing.Size size);

    /**
     * 获取指定尺寸的总出库数量
     * @param clothingId 服装ID
     * @param size 尺寸
     * @return 总出库数量
     */
    Long getOutboundQuantityBySize(Long clothingId, Clothing.Size size);

}
