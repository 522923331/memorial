package com.ruoyi.memorial.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.memorial.domain.Organization;
import com.ruoyi.memorial.service.IOrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/memorial/organization")
public class OrganizationController extends BaseController {
    @Autowired
    private IOrganizationService organizationService;

    @PreAuthorize("@ss.hasPermi('memorial:organization:list')")
    @GetMapping("/list")
    public TableDataInfo list(Organization organization) {
        startPage();
        List<Organization> list = organizationService.selectOrganizationList(organization);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('memorial:organization:query')")
    @GetMapping("/{orgId}")
    public AjaxResult getInfo(@PathVariable Long orgId) {
        return AjaxResult.success(organizationService.selectOrganizationById(orgId));
    }

    @PreAuthorize("@ss.hasPermi('memorial:organization:add')")
    @Log(title = "机构管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Organization organization) {
        organization.setDelFlag("0");
        organization.setCreateBy(String.valueOf(getUserId()));
        organization.setCreateTime(DateUtils.getNowDate());
        return toAjax(organizationService.insertOrganization(organization));
    }

    @PreAuthorize("@ss.hasPermi('memorial:organization:edit')")
    @Log(title = "机构管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Organization organization) {
        organization.setUpdateBy(String.valueOf(getUserId()));
        organization.setUpdateTime(DateUtils.getNowDate());
        return toAjax(organizationService.updateOrganization(organization));
    }

    @PreAuthorize("@ss.hasPermi('memorial:organization:remove')")
    @Log(title = "机构管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{orgIds}")
    public AjaxResult remove(@PathVariable Long[] orgIds) {
        return toAjax(organizationService.deleteOrganizationByIds(orgIds));
    }

    @PreAuthorize("@ss.hasPermi('memorial:organization:export')")
    @Log(title = "机构管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public AjaxResult export(Organization organization) {
        List<Organization> list = organizationService.selectOrganizationList(organization);
        ExcelUtil<Organization> util = new ExcelUtil<Organization>(Organization.class);
        return util.exportExcel(list, "机构数据");
    }
}
