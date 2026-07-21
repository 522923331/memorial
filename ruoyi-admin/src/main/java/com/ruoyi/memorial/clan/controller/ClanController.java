package com.ruoyi.memorial.clan.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.memorial.clan.cache.IClanCacheService;
import com.ruoyi.memorial.clan.domain.Clan;
import com.ruoyi.memorial.clan.domain.ClanMember;
import com.ruoyi.memorial.clan.service.IClanMemberService;
import com.ruoyi.memorial.clan.service.IClanService;

/**
 * 族谱管理（后台） /memorial/clan/**
 */
@RestController
@RequestMapping("/memorial/clan")
public class ClanController extends BaseController {

    @Autowired
    private IClanService clanService;

    @Autowired
    private IClanMemberService clanMemberService;

    @Autowired
    private IClanCacheService clanCacheService;

    @PreAuthorize("@ss.hasPermi('memorial:clan:list')")
    @GetMapping("/list")
    public TableDataInfo list(Clan clan) {
        startPage();
        List<Clan> list = clanService.selectClanList(clan);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('memorial:clan:query')")
    @GetMapping("/{clanId}")
    public AjaxResult getInfo(@PathVariable Long clanId) {
        AjaxResult ajax = AjaxResult.success();
        ajax.put("clan", clanService.selectClanById(clanId));
        ajax.put("members", clanMemberService.selectMembersByClanId(clanId));
        return ajax;
    }

    @PreAuthorize("@ss.hasPermi('memorial:clan:query')")
    @GetMapping("/members/{clanId}")
    public AjaxResult listMembers(@PathVariable Long clanId) {
        return AjaxResult.success(clanMemberService.selectMembersByClanId(clanId));
    }

    @PreAuthorize("@ss.hasPermi('memorial:clan:add')")
    @Log(title = "族谱成员", businessType = BusinessType.INSERT)
    @PostMapping("/member")
    public AjaxResult addMember(@RequestBody ClanMember member) {
        if (member.getClanId() == null) {
            return AjaxResult.error("参数不完整");
        }
        if (member.getIsAlive() == null) member.setIsAlive("1");
        member.setGeneration(1);
        member.setDelFlag("0");
        if (member.getSortOrder() == null) member.setSortOrder(0);
        member.setCreateBy(String.valueOf(getUserId()));
        member.setCreateTime(DateUtils.getNowDate());
        int rows = clanMemberService.insertMember(member);
        if (rows > 0) {
            clanCacheService.refreshGeneration(member.getClanId());
            clanCacheService.refreshCounts(member.getClanId());
        }
        return toAjax(rows);
    }

    @PreAuthorize("@ss.hasPermi('memorial:clan:edit')")
    @Log(title = "族谱成员", businessType = BusinessType.UPDATE)
    @PutMapping("/member")
    public AjaxResult editMember(@RequestBody ClanMember member) {
        if (member.getMemberId() == null) {
            return AjaxResult.error("参数不完整");
        }
        ClanMember exist = clanMemberService.selectMemberById(member.getMemberId());
        if (exist == null) {
            return AjaxResult.error("未找到成员");
        }
        member.setClanId(exist.getClanId());
        member.setUpdateBy(String.valueOf(getUserId()));
        member.setUpdateTime(DateUtils.getNowDate());
        int rows = clanMemberService.updateMember(member);
        if (rows > 0) {
            clanCacheService.refreshGeneration(exist.getClanId());
            clanCacheService.refreshCounts(exist.getClanId());
        }
        return toAjax(rows);
    }

    @PreAuthorize("@ss.hasPermi('memorial:clan:remove')")
    @Log(title = "族谱成员", businessType = BusinessType.DELETE)
    @DeleteMapping("/member/{memberIds}")
    public AjaxResult removeMember(@PathVariable Long[] memberIds) {
        if (memberIds == null || memberIds.length == 0) {
            return AjaxResult.error("参数不完整");
        }
        ClanMember exist = clanMemberService.selectMemberById(memberIds[0]);
        int rows = clanMemberService.deleteMemberByIds(memberIds);
        if (rows > 0 && exist != null) {
            clanCacheService.refreshGeneration(exist.getClanId());
            clanCacheService.refreshCounts(exist.getClanId());
        }
        return toAjax(rows);
    }

    @PreAuthorize("@ss.hasPermi('memorial:clan:edit')")
    @Log(title = "族谱管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Clan clan) {
        clan.setUpdateBy(String.valueOf(getUserId()));
        clan.setUpdateTime(DateUtils.getNowDate());
        return toAjax(clanService.updateClan(clan));
    }

    @PreAuthorize("@ss.hasPermi('memorial:clan:remove')")
    @Log(title = "族谱管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{clanIds}")
    public AjaxResult remove(@PathVariable Long[] clanIds) {
        return toAjax(clanService.deleteClanByIds(clanIds));
    }
}
