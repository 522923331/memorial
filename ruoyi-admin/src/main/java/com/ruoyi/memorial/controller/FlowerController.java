package com.ruoyi.memorial.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.memorial.domain.Flower;
import com.ruoyi.memorial.service.IFlowerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/memorial/flower")
public class FlowerController extends BaseController {
    @Autowired
    private IFlowerService flowerService;

    @PreAuthorize("@ss.hasPermi('memorial:flower:list')")
    @GetMapping("/list")
    public TableDataInfo list(Flower flower) {
        startPage();
        List<Flower> list = flowerService.selectFlowerList(flower);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('memorial:flower:query')")
    @GetMapping("/{flowerId}")
    public AjaxResult getInfo(@PathVariable Long flowerId) {
        return AjaxResult.success(flowerService.selectFlowerById(flowerId));
    }

    @PreAuthorize("@ss.hasPermi('memorial:flower:remove')")
    @Log(title = "献花管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{flowerIds}")
    public AjaxResult remove(@PathVariable Long[] flowerIds) {
        return toAjax(flowerService.deleteFlowerByIds(flowerIds));
    }

    @PreAuthorize("@ss.hasPermi('memorial:flower:export')")
    @Log(title = "献花管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public AjaxResult export(Flower flower) {
        List<Flower> list = flowerService.selectFlowerList(flower);
        ExcelUtil<Flower> util = new ExcelUtil<Flower>(Flower.class);
        return util.exportExcel(list, "献花数据");
    }
}
