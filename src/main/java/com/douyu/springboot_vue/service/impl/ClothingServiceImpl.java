package com.douyu.springboot_vue.service.impl;

import com.douyu.springboot_vue.entities.Clothing;
import com.douyu.springboot_vue.entities.ClothingStockRecord;
import com.douyu.springboot_vue.repository.ClothingRepository;
import com.douyu.springboot_vue.repository.ClothingStockRecordRepository;
import com.douyu.springboot_vue.service.ClothingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 服装服务实现类
 */
@Service
public class ClothingServiceImpl implements ClothingService {

    private final ClothingRepository clothingRepository;
    private final ClothingStockRecordRepository clothingStockRecordRepository;

    @Autowired
    public ClothingServiceImpl(ClothingRepository clothingRepository,
                               ClothingStockRecordRepository clothingStockRecordRepository) {
        this.clothingRepository = clothingRepository;
        this.clothingStockRecordRepository = clothingStockRecordRepository;
    }

    @Override
    public List<Clothing> getAllClothings() {
        return clothingRepository.findAll();
    }

    @Override
    public Optional<Clothing> getClothingById(Long id) {
        return clothingRepository.findById(id);
    }

    @Override
    public Clothing saveClothing(Clothing clothing) {
        return clothingRepository.save(clothing);
    }

    @Override
    public void deleteClothing(Long id) {
        clothingRepository.deleteById(id);
    }

    @Override
    public void activateClothing(Long id) {
        Optional<Clothing> clothingOpt = clothingRepository.findById(id);
        if (clothingOpt.isPresent()) {
            Clothing clothing = clothingOpt.get();
            clothing.setValid(true);
            clothingRepository.save(clothing);
        }
    }

    @Override
    public void deactivateClothing(Long id) {
        Optional<Clothing> clothingOpt = clothingRepository.findById(id);
        if (clothingOpt.isPresent()) {
            Clothing clothing = clothingOpt.get();
            clothing.setValid(false);
            clothingRepository.save(clothing);
        }
    }

    @Override
    public boolean inboundClothing(Long clothingId, Clothing.Size size, Long quantity, String operator) {
        Optional<Clothing> clothingOpt = clothingRepository.findById(clothingId);
        if (clothingOpt.isPresent()) {
            Clothing clothing = clothingOpt.get();
            // 执行入库操作
            clothing.inbound(size, quantity, operator);

            // 保存服装信息
            Clothing savedClothing = clothingRepository.save(clothing);

            // 创建库存记录
            ClothingStockRecord record = new ClothingStockRecord(
                    savedClothing.getId(),
                    savedClothing.getName(), // 添加服装名称
                    size,
                    ClothingStockRecord.StockOperationType.INBOUND,
                    quantity,
                    clothing.getCurrentStockBySize(size) - quantity,
                    clothing.getCurrentStockBySize(size),
                    operator
            );

            // 保存库存记录
            ClothingStockRecord savedRecord = clothingStockRecordRepository.save(record);

            // 将记录ID添加到服装的记录ID列表中
            savedClothing.addStockRecordId(String.valueOf(savedRecord.getId()));
            clothingRepository.save(savedClothing);

            return true;
        }
        return false;
    }

    @Override
    public boolean outboundClothing(Long clothingId, Clothing.Size size, Long quantity, String operator) {
        Optional<Clothing> clothingOpt = clothingRepository.findById(clothingId);
        if (clothingOpt.isPresent()) {
            Clothing clothing = clothingOpt.get();
            // 执行出库操作
            boolean success = clothing.outbound(size, quantity, operator);
            if (success) {
                // 保存服装信息
                Clothing savedClothing = clothingRepository.save(clothing);

                // 创建库存记录
                ClothingStockRecord record = new ClothingStockRecord(
                        savedClothing.getId(),
                        savedClothing.getName(), // 添加服装名称
                        size,
                        ClothingStockRecord.StockOperationType.OUTBOUND,
                        quantity,
                        clothing.getCurrentStockBySize(size) + quantity,
                        clothing.getCurrentStockBySize(size),
                        operator
                );

                // 保存库存记录
                ClothingStockRecord savedRecord = clothingStockRecordRepository.save(record);

                // 将记录ID添加到服装的记录ID列表中
                savedClothing.addStockRecordId(String.valueOf(savedRecord.getId()));
                clothingRepository.save(savedClothing);
            }
            return success;
        }
        return false;
    }

    @Override
    public boolean inboundClothingBatch(Long clothingId, Map<Clothing.Size, Long> sizeQuantities, String operator) {
        Optional<Clothing> clothingOpt = clothingRepository.findById(clothingId);
        if (clothingOpt.isPresent()) {
            Clothing clothing = clothingOpt.get();
            // 记录每种尺寸操作前的库存量
            Map<Clothing.Size, Long> stockBeforeOperation = new HashMap<>();
            for (Clothing.Size size : sizeQuantities.keySet()) {
                stockBeforeOperation.put(size, clothing.getCurrentStockBySize(size));
            }

            // 批量入库操作
            for (Map.Entry<Clothing.Size, Long> entry : sizeQuantities.entrySet()) {
                Clothing.Size size = entry.getKey();
                Long quantity = entry.getValue();
                if (quantity > 0) {
                    clothing.inbound(size, quantity, operator);
                }
            }

            // 保存服装信息
            Clothing savedClothing = clothingRepository.save(clothing);

            // 为每个尺寸创建库存记录
            for (Map.Entry<Clothing.Size, Long> entry : sizeQuantities.entrySet()) {
                Clothing.Size size = entry.getKey();
                Long quantity = entry.getValue();
                if (quantity > 0) {
                    // 创建库存记录
                    ClothingStockRecord record = new ClothingStockRecord(
                            savedClothing.getId(),
                            savedClothing.getName(), // 添加服装名称
                            size,
                            ClothingStockRecord.StockOperationType.INBOUND,
                            quantity,
                            stockBeforeOperation.get(size),
                            clothing.getCurrentStockBySize(size),
                            operator
                    );

                    // 保存库存记录
                    ClothingStockRecord savedRecord = clothingStockRecordRepository.save(record);

                    // 将记录ID添加到服装的记录ID列表中
                    savedClothing.addStockRecordId(String.valueOf(savedRecord.getId()));
                }
            }

            clothingRepository.save(savedClothing);
            return true;
        }
        return false;
    }

    @Override
    public boolean outboundClothingBatch(Long clothingId, Map<Clothing.Size, Long> sizeQuantities, String operator) {
        Optional<Clothing> clothingOpt = clothingRepository.findById(clothingId);
        if (clothingOpt.isPresent()) {
            Clothing clothing = clothingOpt.get();
            // 检查所有尺寸的库存是否足够
            for (Map.Entry<Clothing.Size, Long> entry : sizeQuantities.entrySet()) {
                Clothing.Size size = entry.getKey();
                Long quantity = entry.getValue();
                if (quantity > 0 && clothing.getCurrentStockBySize(size) < quantity) {
                    return false; // 库存不足
                }
            }

            // 记录每种尺寸操作前的库存量
            Map<Clothing.Size, Long> stockBeforeOperation = new HashMap<>();
            for (Clothing.Size size : sizeQuantities.keySet()) {
                stockBeforeOperation.put(size, clothing.getCurrentStockBySize(size));
            }

            // 执行批量出库操作
            for (Map.Entry<Clothing.Size, Long> entry : sizeQuantities.entrySet()) {
                Clothing.Size size = entry.getKey();
                Long quantity = entry.getValue();
                if (quantity > 0) {
                    clothing.outbound(size, quantity, operator);
                }
            }

            // 保存服装信息
            Clothing savedClothing = clothingRepository.save(clothing);

            // 为每个尺寸创建库存记录
            for (Map.Entry<Clothing.Size, Long> entry : sizeQuantities.entrySet()) {
                Clothing.Size size = entry.getKey();
                Long quantity = entry.getValue();
                if (quantity > 0) {
                    // 创建库存记录
                    ClothingStockRecord record = new ClothingStockRecord(
                            savedClothing.getId(),
                            savedClothing.getName(), // 添加服装名称
                            size,
                            ClothingStockRecord.StockOperationType.OUTBOUND,
                            quantity,
                            stockBeforeOperation.get(size),
                            clothing.getCurrentStockBySize(size),
                            operator
                    );

                    // 保存库存记录
                    ClothingStockRecord savedRecord = clothingStockRecordRepository.save(record);

                    // 将记录ID添加到服装的记录ID列表中
                    savedClothing.addStockRecordId(String.valueOf(savedRecord.getId()));
                }
            }

            clothingRepository.save(savedClothing);
            return true;
        }
        return false;
    }

    @Override
    public Long getCurrentStockBySize(Long clothingId, Clothing.Size size) {
        Optional<Clothing> clothingOpt = clothingRepository.findById(clothingId);
        if (clothingOpt.isPresent()) {
            Clothing clothing = clothingOpt.get();
            return clothing.getCurrentStockBySize(size);
        }
        return 0L;
    }

    @Override
    public Long getTotalQuantityBySize(Long clothingId, Clothing.Size size) {
        Optional<Clothing> clothingOpt = clothingRepository.findById(clothingId);
        if (clothingOpt.isPresent()) {
            Clothing clothing = clothingOpt.get();
            return clothing.getTotalQuantityBySize(size);
        }
        return 0L;
    }

    @Override
    public Long getOutboundQuantityBySize(Long clothingId, Clothing.Size size) {
        Optional<Clothing> clothingOpt = clothingRepository.findById(clothingId);
        if (clothingOpt.isPresent()) {
            Clothing clothing = clothingOpt.get();
            return clothing.getOutboundQuantityBySize(size);
        }
        return 0L;
    }
}
