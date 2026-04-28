package com.ruoyi.memorial.mapper;

import java.util.List;
import com.ruoyi.memorial.domain.Flower;

public interface FlowerMapper {
    public List<Flower> selectFlowerList(Flower flower);
    public Flower selectFlowerById(Long flowerId);
    public List<Flower> selectFlowersByDeceasedId(Long deceasedId);
    public int countByDeceasedId(Long deceasedId);
    public int insertFlower(Flower flower);
    public int deleteFlowerById(Long flowerId);
    public int deleteFlowerByIds(Long[] flowerIds);
    public Long countByOrgId(Long orgId);
}
