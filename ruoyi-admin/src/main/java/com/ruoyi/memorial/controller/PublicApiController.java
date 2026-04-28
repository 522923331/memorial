package com.ruoyi.memorial.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.ip.IpUtils;
import com.ruoyi.memorial.domain.*;
import com.ruoyi.memorial.service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 访客端公开API（无需认证）
 */
@RestController
@RequestMapping("/api")
public class PublicApiController {

    @Autowired
    private IDeceasedService deceasedService;

    @Autowired
    private IDeceasedAlbumService deceasedAlbumService;

    @Autowired
    private IDeceasedVideoService deceasedVideoService;

    @Autowired
    private IMessageService messageService;

    @Autowired
    private IFlowerService flowerService;

    @Autowired
    private IStatisticsService statisticsService;

    /**
     * 扫码获取逝者公开信息
     */
    @GetMapping("/qrcode/{code}")
    public AjaxResult getDeceasedByQrcode(@PathVariable String code) {
        Deceased deceased = deceasedService.selectDeceasedByQrcode(code);
        if (deceased == null) {
            return AjaxResult.error("未找到对应的逝者信息");
        }
        if (!"0".equals(deceased.getStatus())) {
            return AjaxResult.error("该纪念页暂不可访问");
        }

        Long deceasedId = deceased.getDeceasedId();

        // 记录访问统计
        statisticsService.incrementVisitCount(deceasedId);

        // 获取统计数据
        Integer totalVisit = statisticsService.getTotalVisitByDeceasedId(deceasedId);
        Integer messageCount = statisticsService.getTotalMessageByDeceasedId(deceasedId);
        Integer flowerCount = statisticsService.getTotalFlowerByDeceasedId(deceasedId);

        // 获取相册
        List<DeceasedAlbum> albums = deceasedAlbumService.selectAlbumByDeceasedId(deceasedId);
        // 获取视频
        List<DeceasedVideo> videos = deceasedVideoService.selectDeceasedVideoByDeceasedId(deceasedId);
        // 获取已审核留言
        List<Message> messages = messageService.selectApprovedMessagesByDeceasedId(deceasedId);
        // 获取献花
        List<Flower> flowers = flowerService.selectFlowersByDeceasedId(deceasedId);

        Map<String, Object> data = new HashMap<>();
        data.put("deceased", deceased);
        data.put("albums", albums);
        data.put("videos", videos);
        data.put("messages", messages);
        data.put("flowers", flowers);
        data.put("totalVisit", totalVisit);
        data.put("messageCount", messageCount);
        data.put("flowerCount", flowerCount);

        return AjaxResult.success(data);
    }

    /**
     * 提交留言
     */
    @PostMapping("/message")
    public AjaxResult submitMessage(@RequestBody Message message, HttpServletRequest request) {
        if (message.getDeceasedId() == null || message.getContent() == null) {
            return AjaxResult.error("参数不完整");
        }
        message.setIpAddress(IpUtils.getIpAddr(request));
        message.setIsAudited("0");
        message.setCreateTime(DateUtils.getNowDate());
        int result = messageService.insertMessage(message);
        if (result > 0) {
            // 判断是否需要审核，不需要则自动通过
            Deceased deceased = deceasedService.selectDeceasedById(message.getDeceasedId());
            if (deceased != null && "0".equals(deceased.getMessageAudit())) {
                messageService.auditMessage(message.getMessageId(), "1");
            }
            statisticsService.incrementMessageCount(message.getDeceasedId());
        }
        return toAjax(result);
    }

    /**
     * 提交献花
     */
    @PostMapping("/flower")
    public AjaxResult submitFlower(@RequestBody Flower flower, HttpServletRequest request) {
        if (flower.getDeceasedId() == null || flower.getFlowerType() == null) {
            return AjaxResult.error("参数不完整");
        }
        flower.setIpAddress(IpUtils.getIpAddr(request));
        flower.setCreateTime(DateUtils.getNowDate());
        int result = flowerService.insertFlower(flower);
        if (result > 0) {
            statisticsService.incrementFlowerCount(flower.getDeceasedId());
        }
        return toAjax(result);
    }

    /**
     * 获取已审核留言列表
     */
    @GetMapping("/messages/{deceasedId}")
    public AjaxResult getMessages(@PathVariable Long deceasedId) {
        List<Message> messages = messageService.selectApprovedMessagesByDeceasedId(deceasedId);
        return AjaxResult.success(messages);
    }

    /**
     * 获取献花列表
     */
    @GetMapping("/flowers/{deceasedId}")
    public AjaxResult getFlowers(@PathVariable Long deceasedId) {
        List<Flower> flowers = flowerService.selectFlowersByDeceasedId(deceasedId);
        return AjaxResult.success(flowers);
    }

    private AjaxResult toAjax(int result) {
        return result > 0 ? AjaxResult.success() : AjaxResult.error();
    }
}
