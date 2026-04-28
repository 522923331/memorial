package com.ruoyi.memorial.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 机构信息对象 mem_organization
 */
public class Organization extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long orgId;
    private String orgName;
    private String orgCode;
    private String contactName;
    private String contactPhone;
    private String address;
    private String status;
    private Integer packageType;
    private String expireTime;
    private String delFlag;

    public Long getOrgId() { return orgId; }
    public void setOrgId(Long orgId) { this.orgId = orgId; }
    public String getOrgName() { return orgName; }
    public void setOrgName(String orgName) { this.orgName = orgName; }
    public String getOrgCode() { return orgCode; }
    public void setOrgCode(String orgCode) { this.orgCode = orgCode; }
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getPackageType() { return packageType; }
    public void setPackageType(Integer packageType) { this.packageType = packageType; }
    public String getExpireTime() { return expireTime; }
    public void setExpireTime(String expireTime) { this.expireTime = expireTime; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
