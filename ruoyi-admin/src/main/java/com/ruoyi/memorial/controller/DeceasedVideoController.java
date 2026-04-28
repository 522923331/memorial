package com.ruoyi.memorial.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.memorial.domain.DeceasedVideo;
import com.ruoyi.memorial.service.IDeceasedVideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/memorial/video")
public class DeceasedVideoController extends BaseController {
    @Autowired
    private IDeceasedVideoService deceasedVideoService;

    @PreAuthorize("@ss.hasPermi('memorial:video:list')")
    @GetMapping("/list")
    public TableDataInfo list(DeceasedVideo video) {
        startPage();
        List<DeceasedVideo> list = deceasedVideoService.selectDeceasedVideoList(video);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('memorial:video:query')")
    @GetMapping("/{videoId}")
    public AjaxResult getInfo(@PathVariable Long videoId) {
        return AjaxResult.success(deceasedVideoService.selectDeceasedVideoById(videoId));
    }

    @PreAuthorize("@ss.hasPermi('memorial:video:add')")
    @Log(title = "视频管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody DeceasedVideo video) {
        return toAjax(deceasedVideoService.insertDeceasedVideo(video));
    }

    @PreAuthorize("@ss.hasPermi('memorial:video:edit')")
    @Log(title = "视频管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody DeceasedVideo video) {
        return toAjax(deceasedVideoService.updateDeceasedVideo(video));
    }

    @PreAuthorize("@ss.hasPermi('memorial:video:remove')")
    @Log(title = "视频管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{videoIds}")
    public AjaxResult remove(@PathVariable Long[] videoIds) {
        return toAjax(deceasedVideoService.deleteDeceasedVideoByIds(videoIds));
    }
}
