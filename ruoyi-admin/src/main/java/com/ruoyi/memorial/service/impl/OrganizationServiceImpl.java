package com.ruoyi.memorial.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.memorial.domain.Organization;
import com.ruoyi.memorial.mapper.OrganizationMapper;
import com.ruoyi.memorial.service.IOrganizationService;

@Service
public class OrganizationServiceImpl implements IOrganizationService {
    @Autowired
    private OrganizationMapper organizationMapper;

    @Override
    public List<Organization> selectOrganizationList(Organization organization) {
        return organizationMapper.selectOrganizationList(organization);
    }

    @Override
    public Organization selectOrganizationById(Long orgId) {
        return organizationMapper.selectOrganizationById(orgId);
    }

    @Override
    public Organization selectOrganizationByCode(String orgCode) {
        return organizationMapper.selectOrganizationByCode(orgCode);
    }

    @Override
    public int insertOrganization(Organization organization) {
        return organizationMapper.insertOrganization(organization);
    }

    @Override
    public int updateOrganization(Organization organization) {
        return organizationMapper.updateOrganization(organization);
    }

    @Override
    public int deleteOrganizationByIds(Long[] orgIds) {
        return organizationMapper.deleteOrganizationByIds(orgIds);
    }

    @Override
    public int deleteOrganizationById(Long orgId) {
        return organizationMapper.deleteOrganizationById(orgId);
    }
}
