package com.ruoyi.memorial.clan.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.memorial.clan.cache.IClanCacheService;
import com.ruoyi.memorial.clan.domain.Clan;
import com.ruoyi.memorial.clan.domain.ClanMember;
import com.ruoyi.memorial.clan.service.IClanMemberService;
import com.ruoyi.memorial.clan.service.IClanService;

/**
 * 族谱访客端公开API（/api/** permitAll，带 token 可识别族长身份）
 */
@RestController
@RequestMapping("/api/clan")
public class ClanPublicController {

    @Autowired
    private IClanService clanService;

    @Autowired
    private IClanCacheService clanCacheService;

    @Autowired
    private IClanMemberService clanMemberService;

    /** 扫码进入族谱：聚合族谱信息 + 世系树（在世脱敏），记访问 */
    @GetMapping("/qrcode/{code}")
    public AjaxResult getByQrcode(@PathVariable String code) {
        Clan clan = clanService.selectClanByQrcode(code);
        if (clan == null) {
            return AjaxResult.error("未找到对应的族谱信息");
        }
        if (!"0".equals(clan.getStatus())) {
            return AjaxResult.error("该族谱暂不可访问");
        }
        Long userId = requireUserId();
        boolean isOwner = userId != null && userId.equals(clan.getFamilyUserId());
        if (!isOwner && "1".equals(clan.getIsPublic())) {
            return AjaxResult.error("该族谱为私密族谱，不可访问");
        }
        clanCacheService.incrementVisit(clan.getClanId());
        ClanMember tree = clanCacheService.buildTree(clan.getClanId(), userId);
        AjaxResult ajax = AjaxResult.success();
        ajax.put("clan", clan);
        ajax.put("root", tree);
        return ajax;
    }

    /** 浏览公开族谱（在世脱敏） */
    @GetMapping("/{clanId}")
    public AjaxResult getClan(@PathVariable Long clanId) {
        Clan clan = clanService.selectClanById(clanId);
        if (clan == null) {
            return AjaxResult.error("未找到对应的族谱信息");
        }
        if (!"0".equals(clan.getStatus())) {
            return AjaxResult.error("该族谱暂不可访问");
        }
        Long userId = requireUserId();
        boolean isOwner = userId != null && userId.equals(clan.getFamilyUserId());
        if (!isOwner && "1".equals(clan.getIsPublic())) {
            return AjaxResult.error("该族谱为私密族谱，不可访问");
        }
        ClanMember tree = clanCacheService.buildTree(clanId, userId);
        AjaxResult ajax = AjaxResult.success();
        ajax.put("clan", clan);
        ajax.put("root", tree);
        return ajax;
    }

    /** 搜索公开族谱（仅 is_public=0/status=0，限 20 条） */
    @GetMapping("/search")
    public AjaxResult search(@RequestParam String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return AjaxResult.error("请输入搜索关键词");
        }
        List<Clan> list = clanService.searchPublicClan(keyword.trim());
        return AjaxResult.success(list);
    }

    /** 按逝者ID查关联族谱（纪念馆->族谱入口用，无关联返回空） */
    @GetMapping("/by-deceased/{deceasedId}")
    public AjaxResult getByDeceased(@PathVariable Long deceasedId) {
        ClanMember member = clanMemberService.selectByDeceasedId(deceasedId);
        if (member == null) {
            return AjaxResult.success();
        }
        Clan clan = clanService.selectClanById(member.getClanId());
        if (clan == null || !"0".equals(clan.getStatus())) {
            return AjaxResult.success();
        }
        AjaxResult ajax = AjaxResult.success();
        ajax.put("clanId", clan.getClanId());
        ajax.put("clanName", clan.getClanName());
        return ajax;
    }

    private Long requireUserId() {
        try {
            LoginUser u = SecurityUtils.getLoginUser();
            if (u != null) return u.getUserId();
        } catch (Exception ignored) {
        }
        return null;
    }
}
