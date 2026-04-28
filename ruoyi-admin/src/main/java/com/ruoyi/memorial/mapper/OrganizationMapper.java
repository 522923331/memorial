package com.ruoyi.memorial.mapper;

import java.util.List;
import com.ruoyi.memorial.domain.Organization;

public interface OrganizationMapper {
    public List<Organization> selectOrganizationList(Organization organization);
    public Organization selectOrganizationById(Long orgId);
    public Organization selectOrganizationByCode(String orgCode);
    public int insertOrganization(Organization organization);
    public int updateOrganization(Organization organization);
    public int deleteOrganizationById(Long orgId);
    public int deleteOrganizationByIds(Long[] orgIds);
    public Long countOrdersByOrgId(Long orgId);
}
