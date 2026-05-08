package com.ruoyi.memorial.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.memorial.domain.*;
import com.ruoyi.memorial.service.*;
import com.ruoyi.memorial.utils.QrCodeUtil;
import com.ruoyi.system.service.ISysUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * 家属端API（需token，但无需RuoYi权限）
 */
@RestController
@RequestMapping("/api/family")
public class FamilyApiController {

    private static final Logger log = LoggerFactory.getLogger(FamilyApiController.class);

    @Autowired
    private IDeceasedService deceasedService;

    @Autowired
    private IDeceasedAlbumService deceasedAlbumService;

    @Autowired
    private IDeceasedVideoService deceasedVideoService;

    @Autowired
    private IMessageService messageService;

    @Autowired
    private IStatisticsService statisticsService;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private QrCodeUtil qrCodeUtil;

    @Value("${ruoyi.profile}")
    private String profilePath;

    // ========== 辅助方法 ==========

    private Long requireUserId() {
        try {
            LoginUser loginUser = SecurityUtils.getLoginUser();
            if (loginUser != null) {
                return loginUser.getUserId();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private AjaxResult verifyOwnership(Long deceasedId, Long userId) {
        Deceased deceased = deceasedService.selectDeceasedById(deceasedId);
        if (deceased == null) {
            return AjaxResult.error("未找到逝者信息");
        }
        if (!userId.equals(deceased.getFamilyUserId())) {
            return AjaxResult.error("无权操作");
        }
        return null;
    }

    private AjaxResult toAjax(int result) {
        return result > 0 ? AjaxResult.success() : AjaxResult.error();
    }

    // ========== 纪念馆管理 ==========

    /**
     * 获取我的纪念馆列表
     */
    @GetMapping("/memorials")
    public AjaxResult listMyMemorials() {
        Long userId = requireUserId();
        if (userId == null) {
            return AjaxResult.error(401, "请先登录");
        }
        List<Deceased> list = deceasedService.selectDeceasedByFamilyId(userId);
        return AjaxResult.success(list);
    }

    /**
     * 获取纪念馆详情（家属视角）
     */
    @GetMapping("/memorial/{deceasedId}")
    public AjaxResult getMemorial(@PathVariable Long deceasedId) {
        Long userId = requireUserId();
        if (userId == null) {
            return AjaxResult.error(401, "请先登录");
        }
        AjaxResult ownership = verifyOwnership(deceasedId, userId);
        if (ownership != null) {
            return ownership;
        }
        Deceased deceased = deceasedService.selectDeceasedById(deceasedId);
        List<DeceasedAlbum> albums = deceasedAlbumService.selectAlbumByDeceasedId(deceasedId);
        List<DeceasedVideo> videos = deceasedVideoService.selectDeceasedVideoByDeceasedId(deceasedId);
        AjaxResult ajax = AjaxResult.success(deceased);
        ajax.put("albums", albums);
        ajax.put("videos", videos);
        return ajax;
    }

    /**
     * 创建纪念馆
     */
    @PostMapping("/memorial")
    public AjaxResult createMemorial(@RequestBody Deceased deceased) throws IOException {
        Long userId = requireUserId();
        if (userId == null) {
            return AjaxResult.error(401, "请先登录");
        }

        deceased.setFamilyUserId(userId);
        if (deceased.getOrgId() == null) {
            deceased.setOrgId(0L);
        }
        if (deceased.getCoverImage() == null) {
            deceased.setCoverImage("");
        }
        if (deceased.getGender() == null) {
            deceased.setGender("0");
        }
        if (deceased.getCemeteryArea() == null) {
            deceased.setCemeteryArea("");
        }
        if (deceased.getCemeteryNumber() == null) {
            deceased.setCemeteryNumber("");
        }
        deceased.setStatus("0");
        deceased.setDelFlag("0");
        if (deceased.getIsPublic() == null) {
            deceased.setIsPublic("0");
        }
        if (deceased.getAllowMessage() == null) {
            deceased.setAllowMessage("0");
        }
        if (deceased.getMessageAudit() == null) {
            deceased.setMessageAudit("1");
        }
        deceased.setCreateBy(String.valueOf(userId));
        deceased.setCreateTime(DateUtils.getNowDate());

        // 生成二维码编码和图片
        String qrcodeCode = deceasedService.generateQrcodeCode();
        deceased.setQrcodeCode(qrcodeCode);
        try {
            byte[] qrBytes = qrCodeUtil.generateQrCode("/memorial/" + qrcodeCode);
            String fileName = qrcodeCode + ".png";
            String savePath = profilePath + "/memorial/qrcode";
            File dir = new File(savePath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File qrFile = new File(savePath, fileName);
            try (FileOutputStream fos = new FileOutputStream(qrFile)) {
                fos.write(qrBytes);
            }
            deceased.setQrcodeUrl("/memorial/qrcode/" + fileName);
        } catch (Exception e) {
            log.error("生成二维码失败", e);
        }

        int result = deceasedService.insertDeceased(deceased);
        if (result > 0) {
            return AjaxResult.success(deceased);
        }
        return AjaxResult.error("创建失败");
    }

    /**
     * 更新纪念馆
     */
    @PutMapping("/memorial")
    public AjaxResult updateMemorial(@RequestBody Deceased deceased) {
        Long userId = requireUserId();
        if (userId == null) {
            return AjaxResult.error(401, "请先登录");
        }
        AjaxResult ownership = verifyOwnership(deceased.getDeceasedId(), userId);
        if (ownership != null) {
            return ownership;
        }
        // 禁止修改 familyUserId
        deceased.setFamilyUserId(userId);
        deceased.setUpdateBy(String.valueOf(userId));
        deceased.setUpdateTime(DateUtils.getNowDate());
        return toAjax(deceasedService.updateDeceased(deceased));
    }

    /**
     * 重新生成二维码
     */
    @PostMapping("/memorial/qrcode/{deceasedId}")
    public AjaxResult regenerateQrcode(@PathVariable Long deceasedId) {
        Long userId = requireUserId();
        if (userId == null) {
            return AjaxResult.error(401, "请先登录");
        }
        AjaxResult ownership = verifyOwnership(deceasedId, userId);
        if (ownership != null) {
            return ownership;
        }

        Deceased deceased = deceasedService.selectDeceasedById(deceasedId);
        String qrcodeCode = deceasedService.generateQrcodeCode();
        deceased.setQrcodeCode(qrcodeCode);

        try {
            byte[] qrBytes = qrCodeUtil.generateQrCode("/memorial/" + qrcodeCode);
            String fileName = qrcodeCode + ".png";
            String savePath = profilePath + "/memorial/qrcode";
            File dir = new File(savePath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File qrFile = new File(savePath, fileName);
            try (FileOutputStream fos = new FileOutputStream(qrFile)) {
                fos.write(qrBytes);
            }
            deceased.setQrcodeUrl("/memorial/qrcode/" + fileName);
        } catch (Exception e) {
            log.error("生成二维码失败", e);
            return AjaxResult.error("生成二维码失败");
        }

        deceasedService.updateDeceased(deceased);
        AjaxResult ajax = AjaxResult.success();
        ajax.put("qrcodeUrl", deceased.getQrcodeUrl());
        ajax.put("qrcodeCode", deceased.getQrcodeCode());
        return ajax;
    }

    // ========== 文件上传 ==========

    /**
     * 通用文件上传（封面图等）
     */
    @PostMapping("/upload")
    public AjaxResult uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        Long userId = requireUserId();
        if (userId == null) {
            return AjaxResult.error(401, "请先登录");
        }
        String url = FileUploadUtils.upload(profilePath + "/memorial/cover", file);
        return AjaxResult.success(url);
    }

    // ========== 相册管理 ==========

    /**
     * 上传相册照片
     */
    @PostMapping("/album/upload")
    public AjaxResult uploadAlbum(@RequestParam("deceasedId") Long deceasedId,
                                  @RequestParam("file") MultipartFile file) throws IOException {
        Long userId = requireUserId();
        if (userId == null) {
            return AjaxResult.error(401, "请先登录");
        }
        AjaxResult ownership = verifyOwnership(deceasedId, userId);
        if (ownership != null) {
            return ownership;
        }

        String imageUrl = FileUploadUtils.upload(profilePath + "/memorial/album", file);
        DeceasedAlbum album = new DeceasedAlbum();
        album.setDeceasedId(deceasedId);
        album.setImageUrl(imageUrl);
        album.setThumbnailUrl("");
        album.setDescription("");
        album.setSortOrder(0);
        album.setCreateTime(DateUtils.dateTimeNow("yyyy-MM-dd HH:mm:ss"));
        deceasedAlbumService.insertAlbum(album);
        return AjaxResult.success(album);
    }

    /**
     * 删除相册照片
     */
    @DeleteMapping("/album/{albumId}")
    public AjaxResult deleteAlbum(@PathVariable Long albumId) {
        Long userId = requireUserId();
        if (userId == null) {
            return AjaxResult.error(401, "请先登录");
        }

        DeceasedAlbum album = deceasedAlbumService.selectAlbumById(albumId);
        if (album == null) {
            return AjaxResult.error("照片不存在");
        }
        AjaxResult ownership = verifyOwnership(album.getDeceasedId(), userId);
        if (ownership != null) {
            return ownership;
        }

        return toAjax(deceasedAlbumService.deleteAlbumById(albumId));
    }

    /**
     * 更新相册描述/排序
     */
    @PutMapping("/album")
    public AjaxResult updateAlbum(@RequestBody DeceasedAlbum album) {
        Long userId = requireUserId();
        if (userId == null) {
            return AjaxResult.error(401, "请先登录");
        }
        AjaxResult ownership = verifyOwnership(album.getDeceasedId(), userId);
        if (ownership != null) {
            return ownership;
        }
        return toAjax(deceasedAlbumService.updateAlbum(album));
    }

    // ========== 视频管理 ==========

    /**
     * 上传视频
     */
    @PostMapping("/video/upload")
    public AjaxResult uploadVideo(@RequestParam("deceasedId") Long deceasedId,
                                  @RequestParam("file") MultipartFile file,
                                  @RequestParam(value = "title", required = false) String title,
                                  @RequestParam(value = "description", required = false) String description) throws IOException {
        Long userId = requireUserId();
        if (userId == null) {
            return AjaxResult.error(401, "请先登录");
        }
        AjaxResult ownership = verifyOwnership(deceasedId, userId);
        if (ownership != null) {
            return ownership;
        }

        String videoUrl = FileUploadUtils.upload(profilePath + "/memorial/video", file);
        DeceasedVideo video = new DeceasedVideo();
        video.setDeceasedId(deceasedId);
        video.setVideoUrl(videoUrl);
        video.setTitle(title != null ? title : "纪念视频");
        video.setCoverUrl("");
        video.setDescription(description != null ? description : "");
        video.setSortOrder(0);
        video.setCreateTime(DateUtils.getNowDate());
        deceasedVideoService.insertDeceasedVideo(video);
        return AjaxResult.success(video);
    }

    /**
     * 删除视频
     */
    @DeleteMapping("/video/{videoId}")
    public AjaxResult deleteVideo(@PathVariable Long videoId) {
        Long userId = requireUserId();
        if (userId == null) {
            return AjaxResult.error(401, "请先登录");
        }

        DeceasedVideo video = deceasedVideoService.selectDeceasedVideoById(videoId);
        if (video == null) {
            return AjaxResult.error("视频不存在");
        }
        AjaxResult ownership = verifyOwnership(video.getDeceasedId(), userId);
        if (ownership != null) {
            return ownership;
        }

        return toAjax(deceasedVideoService.deleteDeceasedVideoById(videoId));
    }

    /**
     * 更新视频信息
     */
    @PutMapping("/video")
    public AjaxResult updateVideo(@RequestBody DeceasedVideo video) {
        Long userId = requireUserId();
        if (userId == null) {
            return AjaxResult.error(401, "请先登录");
        }

        // 先通过视频找到原始deceasedId验证权限
        DeceasedVideo existing = deceasedVideoService.selectDeceasedVideoById(video.getVideoId());
        if (existing == null) {
            return AjaxResult.error("视频不存在");
        }
        AjaxResult ownership = verifyOwnership(existing.getDeceasedId(), userId);
        if (ownership != null) {
            return ownership;
        }

        // 保持deceasedId不变
        video.setDeceasedId(existing.getDeceasedId());
        return toAjax(deceasedVideoService.updateDeceasedVideo(video));
    }

    // ========== 留言审核 ==========

    /**
     * 获取待审核留言
     */
    @GetMapping("/messages/pending/{deceasedId}")
    public AjaxResult getPendingMessages(@PathVariable Long deceasedId) {
        Long userId = requireUserId();
        if (userId == null) {
            return AjaxResult.error(401, "请先登录");
        }
        AjaxResult ownership = verifyOwnership(deceasedId, userId);
        if (ownership != null) {
            return ownership;
        }

        Message query = new Message();
        query.setDeceasedId(deceasedId);
        query.setIsAudited("0");
        List<Message> list = messageService.selectMessageList(query);
        return AjaxResult.success(list);
    }

    /**
     * 审核单条留言
     */
    @PutMapping("/message/audit")
    public AjaxResult auditMessage(@RequestBody Message message) {
        Long userId = requireUserId();
        if (userId == null) {
            return AjaxResult.error(401, "请先登录");
        }

        Message existing = messageService.selectMessageById(message.getMessageId());
        if (existing == null) {
            return AjaxResult.error("留言不存在");
        }
        AjaxResult ownership = verifyOwnership(existing.getDeceasedId(), userId);
        if (ownership != null) {
            return ownership;
        }

        return toAjax(messageService.auditMessage(message.getMessageId(), message.getIsAudited()));
    }

    /**
     * 批量审核留言
     */
    @PutMapping("/message/batchAudit")
    public AjaxResult batchAudit(@RequestParam String status, @RequestParam Long[] messageIds) {
        Long userId = requireUserId();
        if (userId == null) {
            return AjaxResult.error(401, "请先登录");
        }

        // 验证所有留言都属于该家属
        for (Long messageId : messageIds) {
            Message existing = messageService.selectMessageById(messageId);
            if (existing == null) {
                return AjaxResult.error("留言ID " + messageId + " 不存在");
            }
            AjaxResult ownership = verifyOwnership(existing.getDeceasedId(), userId);
            if (ownership != null) {
                return ownership;
            }
        }

        return toAjax(messageService.batchAuditMessage(messageIds, status));
    }

    // ========== 消息通知 ==========

    /**
     * 获取待审核留言总数（跨所有纪念馆）
     */
    @GetMapping("/messages/pendingCount")
    public AjaxResult getPendingMessageCount() {
        Long userId = requireUserId();
        if (userId == null) {
            return AjaxResult.error(401, "请先登录");
        }
        List<Deceased> myMemorials = deceasedService.selectDeceasedByFamilyId(userId);
        int totalCount = 0;
        for (Deceased d : myMemorials) {
            Message query = new Message();
            query.setDeceasedId(d.getDeceasedId());
            query.setIsAudited("0");
            List<Message> pending = messageService.selectMessageList(query);
            totalCount += pending.size();
        }
        AjaxResult ajax = AjaxResult.success();
        ajax.put("pendingCount", totalCount);
        return ajax;
    }

    // ========== 访问统计 ==========

    /**
     * 获取单个纪念馆的统计数据
     */
    @GetMapping("/statistics/{deceasedId}")
    public AjaxResult getMemorialStatistics(@PathVariable Long deceasedId) {
        Long userId = requireUserId();
        if (userId == null) {
            return AjaxResult.error(401, "请先登录");
        }
        AjaxResult ownership = verifyOwnership(deceasedId, userId);
        if (ownership != null) {
            return ownership;
        }

        AjaxResult ajax = AjaxResult.success();
        ajax.put("totalVisit", statisticsService.getTotalVisitByDeceasedId(deceasedId));
        ajax.put("totalMessage", statisticsService.getTotalMessageByDeceasedId(deceasedId));
        ajax.put("totalFlower", statisticsService.getTotalFlowerByDeceasedId(deceasedId));

        Statistics query = new Statistics();
        query.setDeceasedId(deceasedId);
        List<Statistics> dailyStats = statisticsService.selectStatisticsList(query);
        ajax.put("dailyStats", dailyStats);
        return ajax;
    }

    /**
     * 获取我的所有纪念馆统计摘要
     */
    @GetMapping("/memorials/statistics")
    public AjaxResult getMyMemorialsStatistics() {
        Long userId = requireUserId();
        if (userId == null) {
            return AjaxResult.error(401, "请先登录");
        }
        List<Deceased> myMemorials = deceasedService.selectDeceasedByFamilyId(userId);
        List<Map<String, Object>> summary = new ArrayList<>();
        for (Deceased d : myMemorials) {
            Map<String, Object> item = new HashMap<>();
            item.put("deceasedId", d.getDeceasedId());
            item.put("name", d.getName());
            item.put("coverImage", d.getCoverImage());
            item.put("totalVisit", statisticsService.getTotalVisitByDeceasedId(d.getDeceasedId()));
            item.put("totalMessage", statisticsService.getTotalMessageByDeceasedId(d.getDeceasedId()));
            item.put("totalFlower", statisticsService.getTotalFlowerByDeceasedId(d.getDeceasedId()));
            summary.add(item);
        }
        return AjaxResult.success(summary);
    }

    // ========== 个人信息 ==========

    /**
     * 更新个人信息
     */
    @PutMapping("/profile")
    public AjaxResult updateProfile(@RequestBody SysUser user) {
        Long userId = requireUserId();
        if (userId == null) {
            return AjaxResult.error(401, "请先登录");
        }
        user.setUserId(userId);
        user.setUserName(null);
        user.setPassword(null);
        user.setDelFlag(null);
        user.setStatus(null);
        user.setLoginIp(null);
        user.setLoginDate(null);
        user.setCreateBy(null);
        user.setCreateTime(null);
        if (userService.updateUserProfile(user) > 0) {
            SysUser updated = userService.selectUserById(userId);
            AjaxResult ajax = AjaxResult.success();
            ajax.put("userId", updated.getUserId());
            ajax.put("userName", updated.getUserName());
            ajax.put("nickName", updated.getNickName());
            ajax.put("phonenumber", updated.getPhonenumber());
            ajax.put("avatar", updated.getAvatar());
            ajax.put("sex", updated.getSex());
            return ajax;
        }
        return AjaxResult.error("更新失败");
    }
}
