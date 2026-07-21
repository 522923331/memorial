package com.ruoyi.memorial.clan.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.memorial.clan.cache.IClanCacheService;
import com.ruoyi.memorial.clan.domain.Clan;
import com.ruoyi.memorial.clan.domain.ClanMember;
import com.ruoyi.memorial.clan.domain.ClanRelation;
import com.ruoyi.memorial.clan.service.IClanMemberService;
import com.ruoyi.memorial.clan.service.IClanRelationService;
import com.ruoyi.memorial.clan.service.IClanService;
import com.ruoyi.memorial.domain.Deceased;
import com.ruoyi.memorial.service.IDeceasedService;
import com.ruoyi.memorial.utils.QrCodeUtil;

/**
 * 族谱家属端API（/api/family/** 手动校验 token + 归属校验）
 */
@RestController
@RequestMapping("/api/family")
public class ClanFamilyController {

    @Autowired
    private IClanService clanService;

    @Autowired
    private IClanMemberService clanMemberService;

    @Autowired
    private IClanRelationService clanRelationService;

    @Autowired
    private IClanCacheService clanCacheService;

    @Autowired
    private IDeceasedService deceasedService;

    @Autowired
    private QrCodeUtil qrCodeUtil;

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

    private AjaxResult verifyClanOwnership(Long clanId, Long userId) {
        Clan clan = clanService.selectClanById(clanId);
        if (clan == null) {
            return AjaxResult.error("未找到族谱信息");
        }
        if (!userId.equals(clan.getFamilyUserId())) {
            return AjaxResult.error("无权操作");
        }
        return null;
    }

    private AjaxResult verifyDeceasedOwnership(Long deceasedId, Long userId) {
        Deceased deceased = deceasedService.selectDeceasedById(deceasedId);
        if (deceased == null) {
            return AjaxResult.error("未找到逝者信息");
        }
        if (!userId.equals(deceased.getFamilyUserId())) {
            return AjaxResult.error("无权关联该纪念馆");
        }
        return null;
    }

    private AjaxResult toAjax(int result) {
        return result > 0 ? AjaxResult.success() : AjaxResult.error();
    }

    private String generateClanCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    private void insertRelation(Long clanId, Long fromMemberId, Long toMemberId, int type, Long userId) {
        ClanRelation r = new ClanRelation();
        r.setClanId(clanId);
        r.setFromMemberId(fromMemberId);
        r.setToMemberId(toMemberId);
        r.setRelationType(type);
        r.setRelationOrder(0);
        r.setCreateBy(String.valueOf(userId));
        r.setCreateTime(DateUtils.getNowDate());
        clanRelationService.insertRelation(r);
    }

    // ========== 族谱管理 ==========

    @GetMapping("/clans")
    public AjaxResult listMyClans() {
        Long userId = requireUserId();
        if (userId == null) {
            return AjaxResult.error(401, "请先登录");
        }
        return AjaxResult.success(clanService.selectClanByFamilyId(userId));
    }

    @GetMapping("/clan/{clanId}")
    public AjaxResult getClan(@PathVariable Long clanId) {
        Long userId = requireUserId();
        if (userId == null) {
            return AjaxResult.error(401, "请先登录");
        }
        AjaxResult ownership = verifyClanOwnership(clanId, userId);
        if (ownership != null) {
            return ownership;
        }
        Clan clan = clanService.selectClanById(clanId);
        ClanMember tree = clanCacheService.buildTree(clanId, userId);
        AjaxResult ajax = AjaxResult.success();
        ajax.put("clan", clan);
        ajax.put("root", tree);
        return ajax;
    }

    @PostMapping("/clan")
    public AjaxResult createClan(@RequestBody Clan clan) {
        Long userId = requireUserId();
        if (userId == null) {
            return AjaxResult.error(401, "请先登录");
        }
        clan.setFamilyUserId(userId);
        if (clan.getOrgId() == null) clan.setOrgId(0L);
        if (clan.getIsPublic() == null) clan.setIsPublic("0");
        if (clan.getShowAliveAvatar() == null) clan.setShowAliveAvatar("0");
        clan.setStatus("0");
        clan.setDelFlag("0");
        clan.setMemberCount(0);
        clan.setGenerationCount(0);

        String qrcodeCode = generateClanCode();
        clan.setQrcodeCode(qrcodeCode);
        QrCodeUtil.QrCodeResult qr = qrCodeUtil.generateForCode(qrcodeCode, userId, "pages/clan/detail");
        if (!qr.url().isEmpty()) {
            clan.setQrcodeUrl(qr.url());
        }

        clan.setCreateBy(String.valueOf(userId));
        clan.setCreateTime(DateUtils.getNowDate());
        return toAjax(clanService.insertClan(clan));
    }

    @PutMapping("/clan")
    public AjaxResult editClan(@RequestBody Clan clan) {
        Long userId = requireUserId();
        if (userId == null) {
            return AjaxResult.error(401, "请先登录");
        }
        if (clan.getClanId() == null) {
            return AjaxResult.error("参数不完整");
        }
        AjaxResult ownership = verifyClanOwnership(clan.getClanId(), userId);
        if (ownership != null) {
            return ownership;
        }
        clan.setFamilyUserId(userId);
        clan.setUpdateBy(String.valueOf(userId));
        clan.setUpdateTime(DateUtils.getNowDate());
        return toAjax(clanService.updateClan(clan));
    }

    @DeleteMapping("/clan/{clanId}")
    public AjaxResult deleteClan(@PathVariable Long clanId) {
        Long userId = requireUserId();
        if (userId == null) {
            return AjaxResult.error(401, "请先登录");
        }
        AjaxResult ownership = verifyClanOwnership(clanId, userId);
        if (ownership != null) {
            return ownership;
        }
        return toAjax(clanService.deleteClanByIds(new Long[]{clanId}));
    }

    @PostMapping("/clan/qrcode/{clanId}")
    public AjaxResult regenerateQrcode(@PathVariable Long clanId) {
        Long userId = requireUserId();
        if (userId == null) {
            return AjaxResult.error(401, "请先登录");
        }
        AjaxResult ownership = verifyClanOwnership(clanId, userId);
        if (ownership != null) {
            return ownership;
        }
        String qrcodeCode = generateClanCode();
        QrCodeUtil.QrCodeResult qr = qrCodeUtil.generateForCode(qrcodeCode, userId, "pages/clan/detail");
        Clan update = new Clan();
        update.setClanId(clanId);
        update.setQrcodeCode(qrcodeCode);
        if (!qr.url().isEmpty()) {
            update.setQrcodeUrl(qr.url());
        }
        update.setUpdateBy(String.valueOf(userId));
        update.setUpdateTime(DateUtils.getNowDate());
        clanService.updateClan(update);
        AjaxResult ajax = AjaxResult.success();
        ajax.put("qrcodeCode", qrcodeCode);
        ajax.put("qrcodeUrl", qr.url());
        return ajax;
    }

    // ========== 成员管理 ==========

    @GetMapping("/clan/{clanId}/members")
    public AjaxResult listMembers(@PathVariable Long clanId) {
        Long userId = requireUserId();
        if (userId == null) {
            return AjaxResult.error(401, "请先登录");
        }
        AjaxResult ownership = verifyClanOwnership(clanId, userId);
        if (ownership != null) {
            return ownership;
        }
        return AjaxResult.success(clanMemberService.selectMembersByClanId(clanId));
    }

    @PostMapping("/clan/member")
    public AjaxResult addMember(@RequestBody ClanMember member) {
        Long userId = requireUserId();
        if (userId == null) {
            return AjaxResult.error(401, "请先登录");
        }
        if (member.getClanId() == null) {
            return AjaxResult.error("参数不完整");
        }
        AjaxResult ownership = verifyClanOwnership(member.getClanId(), userId);
        if (ownership != null) {
            return ownership;
        }
        if (member.getDeceasedId() != null) {
            AjaxResult ref = verifyDeceasedOwnership(member.getDeceasedId(), userId);
            if (ref != null) {
                return ref;
            }
        }
        if (member.getIsAlive() == null) member.setIsAlive("1");
        member.setGeneration(1);
        member.setDelFlag("0");
        if (member.getSortOrder() == null) member.setSortOrder(0);
        member.setCreateBy(String.valueOf(userId));
        member.setCreateTime(DateUtils.getNowDate());
        int rows = clanMemberService.insertMember(member);
        if (rows > 0) {
            clanCacheService.refreshGeneration(member.getClanId());
            clanCacheService.refreshCounts(member.getClanId());
        }
        return toAjax(rows);
    }

    @PutMapping("/clan/member")
    public AjaxResult editMember(@RequestBody ClanMember member) {
        Long userId = requireUserId();
        if (userId == null) {
            return AjaxResult.error(401, "请先登录");
        }
        if (member.getMemberId() == null) {
            return AjaxResult.error("参数不完整");
        }
        ClanMember exist = clanMemberService.selectMemberById(member.getMemberId());
        if (exist == null) {
            return AjaxResult.error("未找到成员");
        }
        AjaxResult ownership = verifyClanOwnership(exist.getClanId(), userId);
        if (ownership != null) {
            return ownership;
        }
        if (member.getDeceasedId() != null) {
            AjaxResult ref = verifyDeceasedOwnership(member.getDeceasedId(), userId);
            if (ref != null) {
                return ref;
            }
        }
        member.setClanId(exist.getClanId());
        member.setUpdateBy(String.valueOf(userId));
        member.setUpdateTime(DateUtils.getNowDate());
        int rows = clanMemberService.updateMember(member);
        if (rows > 0) {
            clanCacheService.refreshGeneration(exist.getClanId());
            clanCacheService.refreshCounts(exist.getClanId());
        }
        return toAjax(rows);
    }

    @DeleteMapping("/clan/member/{memberId}")
    public AjaxResult deleteMember(@PathVariable Long memberId) {
        Long userId = requireUserId();
        if (userId == null) {
            return AjaxResult.error(401, "请先登录");
        }
        ClanMember exist = clanMemberService.selectMemberById(memberId);
        if (exist == null) {
            return AjaxResult.error("未找到成员");
        }
        AjaxResult ownership = verifyClanOwnership(exist.getClanId(), userId);
        if (ownership != null) {
            return ownership;
        }
        int rows = clanMemberService.deleteMemberByIds(new Long[]{memberId});
        if (rows > 0) {
            clanCacheService.refreshGeneration(exist.getClanId());
            clanCacheService.refreshCounts(exist.getClanId());
        }
        return toAjax(rows);
    }

    /** 批量设置某成员的生父生母 */
    @PutMapping("/clan/member/{memberId}/parents")
    public AjaxResult setParents(@PathVariable Long memberId, @RequestBody Map<String, Long> body) {
        Long userId = requireUserId();
        if (userId == null) {
            return AjaxResult.error(401, "请先登录");
        }
        ClanMember exist = clanMemberService.selectMemberById(memberId);
        if (exist == null) {
            return AjaxResult.error("未找到成员");
        }
        AjaxResult ownership = verifyClanOwnership(exist.getClanId(), userId);
        if (ownership != null) {
            return ownership;
        }
        Long fatherId = body.get("fatherId");
        Long motherId = body.get("motherId");
        if (fatherId != null) {
            ClanMember f = clanMemberService.selectMemberById(fatherId);
            if (f == null || !exist.getClanId().equals(f.getClanId())) {
                return AjaxResult.error("父亲成员不存在或不属于该族谱");
            }
        }
        if (motherId != null) {
            ClanMember m = clanMemberService.selectMemberById(motherId);
            if (m == null || !exist.getClanId().equals(m.getClanId())) {
                return AjaxResult.error("母亲成员不存在或不属于该族谱");
            }
        }
        // 删除旧生父生母关系（type 1/2）
        List<ClanRelation> fromRels = clanRelationService.selectRelationsByFromMember(memberId);
        for (ClanRelation r : fromRels) {
            if (r.getRelationType() != null && (r.getRelationType() == 1 || r.getRelationType() == 2)) {
                clanRelationService.deleteRelationById(r.getRelationId());
            }
        }
        if (fatherId != null) {
            insertRelation(exist.getClanId(), memberId, fatherId, 1, userId);
        }
        if (motherId != null) {
            insertRelation(exist.getClanId(), memberId, motherId, 2, userId);
        }
        clanCacheService.refreshGeneration(exist.getClanId());
        return AjaxResult.success();
    }

    // ========== 关系管理 ==========

    /** 查询某成员的全部关系（父母/配偶等，from 或 to 命中） */
    @GetMapping("/clan/member/{memberId}/relations")
    public AjaxResult listMemberRelations(@PathVariable Long memberId) {
        Long userId = requireUserId();
        if (userId == null) {
            return AjaxResult.error(401, "请先登录");
        }
        ClanMember exist = clanMemberService.selectMemberById(memberId);
        if (exist == null) {
            return AjaxResult.error("未找到成员");
        }
        AjaxResult ownership = verifyClanOwnership(exist.getClanId(), userId);
        if (ownership != null) {
            return ownership;
        }
        List<ClanRelation> from = clanRelationService.selectRelationsByFromMember(memberId);
        List<ClanRelation> to = clanRelationService.selectRelationsByToMember(memberId);
        List<ClanRelation> all = new ArrayList<>();
        all.addAll(from);
        all.addAll(to);
        return AjaxResult.success(all);
    }

    @PostMapping("/clan/relation")
    public AjaxResult addRelation(@RequestBody ClanRelation relation) {
        Long userId = requireUserId();
        if (userId == null) {
            return AjaxResult.error(401, "请先登录");
        }
        if (relation.getClanId() == null || relation.getFromMemberId() == null
                || relation.getToMemberId() == null || relation.getRelationType() == null) {
            return AjaxResult.error("参数不完整");
        }
        AjaxResult ownership = verifyClanOwnership(relation.getClanId(), userId);
        if (ownership != null) {
            return ownership;
        }
        if (relation.getRelationOrder() == null) relation.setRelationOrder(0);
        relation.setCreateBy(String.valueOf(userId));
        relation.setCreateTime(DateUtils.getNowDate());
        int rows = clanRelationService.insertRelation(relation);
        if (rows > 0) {
            clanCacheService.refreshGeneration(relation.getClanId());
        }
        return toAjax(rows);
    }

    @DeleteMapping("/clan/relation/{relationId}")
    public AjaxResult deleteRelation(@PathVariable Long relationId) {
        Long userId = requireUserId();
        if (userId == null) {
            return AjaxResult.error(401, "请先登录");
        }
        ClanRelation relation = clanRelationService.selectRelationById(relationId);
        if (relation == null) {
            return AjaxResult.error("未找到关系");
        }
        AjaxResult ownership = verifyClanOwnership(relation.getClanId(), userId);
        if (ownership != null) {
            return ownership;
        }
        int rows = clanRelationService.deleteRelationById(relationId);
        if (rows > 0) {
            clanCacheService.refreshGeneration(relation.getClanId());
        }
        return toAjax(rows);
    }

    // ========== 世系树 ==========

    @GetMapping("/clan/{clanId}/tree")
    public AjaxResult getTree(@PathVariable Long clanId) {
        Long userId = requireUserId();
        if (userId == null) {
            return AjaxResult.error(401, "请先登录");
        }
        AjaxResult ownership = verifyClanOwnership(clanId, userId);
        if (ownership != null) {
            return ownership;
        }
        return AjaxResult.success(clanCacheService.buildTree(clanId, userId));
    }
}
