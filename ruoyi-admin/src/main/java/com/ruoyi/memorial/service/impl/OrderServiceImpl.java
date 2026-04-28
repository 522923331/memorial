package com.ruoyi.memorial.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.memorial.domain.MemOrder;
import com.ruoyi.memorial.mapper.OrderMapper;
import com.ruoyi.memorial.service.IOrderService;

@Service
public class OrderServiceImpl implements IOrderService {
    @Autowired
    private OrderMapper orderMapper;

    @Override
    public List<MemOrder> selectOrderList(MemOrder order) {
        return orderMapper.selectOrderList(order);
    }

    @Override
    public MemOrder selectOrderById(Long orderId) {
        return orderMapper.selectOrderById(orderId);
    }

    @Override
    public int insertOrder(MemOrder order) {
        return orderMapper.insertOrder(order);
    }

    @Override
    public int updateOrder(MemOrder order) {
        return orderMapper.updateOrder(order);
    }

    @Override
    public int deleteOrderByIds(Long[] orderIds) {
        return orderMapper.deleteOrderByIds(orderIds);
    }

    @Override
    public int deleteOrderById(Long orderId) {
        return orderMapper.deleteOrderById(orderId);
    }
}
