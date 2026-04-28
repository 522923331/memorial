package com.ruoyi.memorial.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.memorial.domain.MemOrder;
import com.ruoyi.memorial.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/memorial/order")
public class OrderController extends BaseController {
    @Autowired
    private IOrderService orderService;

    @PreAuthorize("@ss.hasPermi('memorial:order:list')")
    @GetMapping("/list")
    public TableDataInfo list(MemOrder order) {
        startPage();
        List<MemOrder> list = orderService.selectOrderList(order);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('memorial:order:query')")
    @GetMapping("/{orderId}")
    public AjaxResult getInfo(@PathVariable Long orderId) {
        return AjaxResult.success(orderService.selectOrderById(orderId));
    }

    @PreAuthorize("@ss.hasPermi('memorial:order:add')")
    @Log(title = "订单管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MemOrder order) {
        return toAjax(orderService.insertOrder(order));
    }

    @PreAuthorize("@ss.hasPermi('memorial:order:edit')")
    @Log(title = "订单管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MemOrder order) {
        return toAjax(orderService.updateOrder(order));
    }

    @PreAuthorize("@ss.hasPermi('memorial:order:remove')")
    @Log(title = "订单管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{orderIds}")
    public AjaxResult remove(@PathVariable Long[] orderIds) {
        return toAjax(orderService.deleteOrderByIds(orderIds));
    }

    @PreAuthorize("@ss.hasPermi('memorial:order:export')")
    @Log(title = "订单管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public AjaxResult export(MemOrder order) {
        List<MemOrder> list = orderService.selectOrderList(order);
        ExcelUtil<MemOrder> util = new ExcelUtil<MemOrder>(MemOrder.class);
        return util.exportExcel(list, "订单数据");
    }
}
