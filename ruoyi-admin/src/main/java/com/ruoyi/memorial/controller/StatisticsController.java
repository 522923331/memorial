package com.ruoyi.memorial.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.memorial.shared.statistics.domain.Statistics;
import com.ruoyi.memorial.shared.statistics.service.IStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/memorial/statistics")
public class StatisticsController extends BaseController {
    @Autowired
    private IStatisticsService statisticsService;

    @PreAuthorize("@ss.hasPermi('memorial:statistics:list')")
    @GetMapping("/list")
    public TableDataInfo list(Statistics statistics) {
        startPage();
        List<Statistics> list = statisticsService.selectStatisticsList(statistics);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('memorial:statistics:query')")
    @GetMapping("/deceased/{deceasedId}")
    public AjaxResult getByDeceased(@PathVariable Long deceasedId) {
        AjaxResult ajax = AjaxResult.success();
        ajax.put("totalVisit", statisticsService.getTotalVisit(IStatisticsService.SUBJECT_TYPE_DECEASED, deceasedId));
        ajax.put("totalMessage", statisticsService.getTotalMessage(IStatisticsService.SUBJECT_TYPE_DECEASED, deceasedId));
        ajax.put("totalFlower", statisticsService.getTotalFlower(IStatisticsService.SUBJECT_TYPE_DECEASED, deceasedId));
        return ajax;
    }

    @PreAuthorize("@ss.hasPermi('memorial:statistics:remove')")
    @DeleteMapping("/{statIds}")
    public AjaxResult remove(@PathVariable Long[] statIds) {
        return toAjax(statisticsService.deleteStatisticsByIds(statIds));
    }
}
