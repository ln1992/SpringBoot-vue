package com.douyu.springboot_vue.service.impl;

import com.douyu.springboot_vue.aop.annotation.RecordUpdate;
import com.douyu.springboot_vue.entities.ClothingStockRecord;
import com.douyu.springboot_vue.repository.ClothingStockRecordRepository;
import com.douyu.springboot_vue.service.ClothingStockRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 服装库存记录服务实现类
 */
@Service
public class ClothingStockRecordServiceImpl implements ClothingStockRecordService {

    private final ClothingStockRecordRepository clothingStockRecordRepository;

    @Autowired
    public ClothingStockRecordServiceImpl(ClothingStockRecordRepository clothingStockRecordRepository) {
        this.clothingStockRecordRepository = clothingStockRecordRepository;
    }

    @Override
    public List<ClothingStockRecord> getAllStockRecords() {
        return clothingStockRecordRepository.findAll();
    }

    @Override
    public Optional<ClothingStockRecord> getStockRecordById(Long id) {
        return clothingStockRecordRepository.findById(id);
    }

    @Override
    @RecordUpdate(operation = RecordUpdate.OperationType.CREATE, description = "创建库存记录")
    public ClothingStockRecord saveStockRecord(ClothingStockRecord stockRecord) {
        return clothingStockRecordRepository.save(stockRecord);
    }

    @Override
    @RecordUpdate(operation = RecordUpdate.OperationType.DELETE, description = "删除库存记录")
    public void deleteStockRecord(Long id) {
        clothingStockRecordRepository.deleteById(id);
    }

    @Override
    public List<ClothingStockRecord> getStockRecordsByClothingId(Long clothingId) {
        return clothingStockRecordRepository.findByClothingId(clothingId);
    }
}