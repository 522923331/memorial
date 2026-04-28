package com.ruoyi.memorial.service;

import java.util.List;
import com.ruoyi.memorial.domain.Flower;

public interface IFlowerService {
    public List<Flower> selectFlowerList(Flower flower);
    public Flower selectFlowerById(Long flowerId);
    public List<Flower> selectFlowersByDeceasedId(Long deceasedId);
    public int insertFlower(Flower flower);
    public int deleteFlowerByIds(Long[] flowerIds);
    public int deleteFlowerById(Long flowerId);
}
