package com.ruoyi.memorial.service;

import java.util.List;
import com.ruoyi.memorial.domain.MemOrder;

public interface IOrderService {
    public List<MemOrder> selectOrderList(MemOrder order);
    public MemOrder selectOrderById(Long orderId);
    public int insertOrder(MemOrder order);
    public int updateOrder(MemOrder order);
    public int deleteOrderByIds(Long[] orderIds);
    public int deleteOrderById(Long orderId);
}
