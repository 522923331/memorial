package com.ruoyi.memorial.mapper;

import java.util.List;
import com.ruoyi.memorial.domain.MemOrder;
import java.math.BigDecimal;

public interface OrderMapper {
    public List<MemOrder> selectOrderList(MemOrder order);
    public MemOrder selectOrderById(Long orderId);
    public MemOrder selectOrderByNo(String orderNo);
    public int countByOrgId(Long orgId);
    public BigDecimal sumAmountByOrgId(Long orgId);
    public int insertOrder(MemOrder order);
    public int updateOrder(MemOrder order);
    public int deleteOrderById(Long orderId);
    public int deleteOrderByIds(Long[] orderIds);
}
