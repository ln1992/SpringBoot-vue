package com.douyu.springboot_vue.service.impl;

import com.douyu.springboot_vue.aop.annotation.RecordUpdate;
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
    @RecordUpdate(operation = RecordUpdate.OperationType.CREATE, description = "创建服装")
    public Clothing createClothing(Clothing clothing) {
        return clothingRepository.save(clothing);
    }

    @Override
    @RecordUpdate(operation = RecordUpdate.OperationType.UPDATE, description = "更新服装")
    public Clothing updateClothing(Long id, Clothing clothing) {
        clothing.setId(id);
        return clothingRepository.save(clothing);
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
    @RecordUpdate(operation = RecordUpdate.OperationType.DELETE, description = "删除服装")
    public void deleteClothing(Long id) {
        clothingRepository.deleteById(id);
    }

    @Override
    @RecordUpdate(operation = RecordUpdate.OperationType.UPDATE, description = "上线服装")
    public void activateClothing(Long id) {
        Optional<Clothing> clothingOpt = clothingRepository.findById(id);
        if (clothingOpt.isPresent()) {
            Clothing clothing = clothingOpt.get();
            clothing.setValid(true);
            clothingRepository.save(clothing);
        }
    }

    @Override
    @RecordUpdate(operation = RecordUpdate.OperationType.UPDATE, description = "下线服装")
    public void deactivateClothing(Long id) {
        Optional<Clothing> clothingOpt = clothingRepository.findById(id);
        if (clothingOpt.isPresent()) {
            Clothing clothing = clothingOpt.get();
            clothing.setValid(false);
            clothingRepository.save(clothing);
        }
    }

    @Override
    @RecordUpdate(operation = RecordUpdate.OperationType.UPDATE, description = "服装入库")
    public boolean inboundClothing(Long clothingId, Clothing.Size size, Long quantity, String operator) {
        Optional<Clothing> clothingOpt = clothingRepository.findById(clothingId);
        if (clothingOpt.isPresent()) {
            Clothing clothing = clothingOpt.get();
            // 记录操作前的库存量
            Long previousStock = clothing.getCurrentStockBySize(size);
            
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
                    previousStock,
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
    @RecordUpdate(operation = RecordUpdate.OperationType.UPDATE, description = "服装出库")
    public boolean outboundClothing(Long clothingId, Clothing.Size size, Long quantity, String operator) {
        Optional<Clothing> clothingOpt = clothingRepository.findById(clothingId);
        if (clothingOpt.isPresent()) {
            Clothing clothing = clothingOpt.get();
            // 记录操作前的库存量
            Long previousStock = clothing.getCurrentStockBySize(size);
            
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
                        previousStock,
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
    @RecordUpdate(operation = RecordUpdate.OperationType.UPDATE, description = "服装批量入库")
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
    @RecordUpdate(operation = RecordUpdate.OperationType.UPDATE, description = "服装批量出库")
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

    @Override
    @RecordUpdate(operation = RecordUpdate.OperationType.UPDATE, description = "根据库存记录恢复库存")
    public boolean restoreStockFromRecords(Long clothingId) {
        Optional<Clothing> clothingOpt = clothingRepository.findById(clothingId);
        if (!clothingOpt.isPresent()) {
            return false;
        }

        Clothing clothing = clothingOpt.get();
        
        // 获取该服装的所有库存记录
        List<ClothingStockRecord> records = clothingStockRecordRepository.findByClothingId(clothingId);
        
        // 初始化各尺码的库存为0
        Map<Clothing.Size, Long> restoredStock = new HashMap<>();
        for (Clothing.Size size : Clothing.Size.values()) {
            restoredStock.put(size, 0L);
        }
        
        // 根据记录重新计算各尺码的当前库存
        for (ClothingStockRecord record : records) {
            Clothing.Size size;
            try {
                size = Clothing.Size.valueOfCode(record.getSize());
            } catch (IllegalArgumentException e) {
                // 如果尺寸代码无效，跳过该记录
                continue;
            }
            
            Long currentStock = restoredStock.get(size);
            if (record.getOperationType() == ClothingStockRecord.StockOperationType.INBOUND) {
                // 入库操作，增加库存
                restoredStock.put(size, currentStock + record.getQuantity());
            } else if (record.getOperationType() == ClothingStockRecord.StockOperationType.OUTBOUND) {
                // 出库操作，减少库存
                restoredStock.put(size, currentStock - record.getQuantity());
            }
        }
        
        // 更新服装的当前库存
        clothing.setCurrentStockBySize(restoredStock);
        
        // 同时更新总入库数量（重新计算各尺码的总入库数量）
        Map<Clothing.Size, Long> totalQuantityBySize = new HashMap<>();
        for (Clothing.Size size : Clothing.Size.values()) {
            totalQuantityBySize.put(size, 0L);
        }
        
        // 根据记录重新计算各尺码的总入库数量
        for (ClothingStockRecord record : records) {
            Clothing.Size size;
            try {
                size = Clothing.Size.valueOfCode(record.getSize());
            } catch (IllegalArgumentException e) {
                // 如果尺寸代码无效，跳过该记录
                continue;
            }
            
            // 只有入库记录才计入总入库数量
            if (record.getOperationType() == ClothingStockRecord.StockOperationType.INBOUND) {
                Long currentTotal = totalQuantityBySize.get(size);
                totalQuantityBySize.put(size, currentTotal + record.getQuantity());
            }
        }
        clothing.setTotalQuantityBySize(totalQuantityBySize);
        
        // 保存更新后的服装信息
        clothingRepository.save(clothing);
        
        return true;
    }
}