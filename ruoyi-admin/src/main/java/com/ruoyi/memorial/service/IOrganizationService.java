package com.ruoyi.memorial.service;

import java.util.List;
import com.ruoyi.memorial.domain.Organization;

public interface IOrganizationService {
    public List<Organization> selectOrganizationList(Organization organization);
    public Organization selectOrganizationById(Long orgId);
    public Organization selectOrganizationByCode(String orgCode);
    public int insertOrganization(Organization organization);
    public int updateOrganization(Organization organization);
    public int deleteOrganizationByIds(Long[] orgIds);
    public int deleteOrganizationById(Long orgId);
}
