package com.ruoyi.memorial.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.memorial.domain.Flower;
import com.ruoyi.memorial.mapper.FlowerMapper;
import com.ruoyi.memorial.service.IFlowerService;

@Service
public class FlowerServiceImpl implements IFlowerService {
    @Autowired
    private FlowerMapper flowerMapper;

    @Override
    public List<Flower> selectFlowerList(Flower flower) {
        return flowerMapper.selectFlowerList(flower);
    }

    @Override
    public Flower selectFlowerById(Long flowerId) {
        return flowerMapper.selectFlowerById(flowerId);
    }

    @Override
    public List<Flower> selectFlowersByDeceasedId(Long deceasedId) {
        return flowerMapper.selectFlowersByDeceasedId(deceasedId);
    }

    @Override
    public int insertFlower(Flower flower) {
        return flowerMapper.insertFlower(flower);
    }

    @Override
    public int deleteFlowerByIds(Long[] flowerIds) {
        return flowerMapper.deleteFlowerByIds(flowerIds);
    }

    @Override
    public int deleteFlowerById(Long flowerId) {
        return flowerMapper.deleteFlowerById(flowerId);
    }
}
