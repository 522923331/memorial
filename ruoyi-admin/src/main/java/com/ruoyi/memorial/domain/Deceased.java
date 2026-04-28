package com.ruoyi.memorial.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;

/**
 * 逝者信息对象 mem_deceased
 */
public class Deceased extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long deceasedId;
    private Long orgId;
    private Long familyUserId;
    private String name;
    private String gender;
    private Date birthDate;
    private Date deathDate;
    private String cemeteryArea;
    private String cemeteryNumber;
    private String bio;
    private String coverImage;
    private String qrcodeCode;
    private String qrcodeUrl;
    private String isPublic;
    private String allowMessage;
    private String messageAudit;
    private String status;
    private String delFlag;

    private String orgName;
    private Integer totalVisit;
    private Integer messageCount;
    private Integer flowerCount;

    public Long getDeceasedId() { return deceasedId; }
    public void setDeceasedId(Long deceasedId) { this.deceasedId = deceasedId; }
    public Long getOrgId() { return orgId; }
    public void setOrgId(Long orgId) { this.orgId = orgId; }
    public Long getFamilyUserId() { return familyUserId; }
    public void setFamilyUserId(Long familyUserId) { this.familyUserId = familyUserId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public Date getBirthDate() { return birthDate; }
    public void setBirthDate(Date birthDate) { this.birthDate = birthDate; }
    public Date getDeathDate() { return deathDate; }
    public void setDeathDate(Date deathDate) { this.deathDate = deathDate; }
    public String getCemeteryArea() { return cemeteryArea; }
    public void setCemeteryArea(String cemeteryArea) { this.cemeteryArea = cemeteryArea; }
    public String getCemeteryNumber() { return cemeteryNumber; }
    public void setCemeteryNumber(String cemeteryNumber) { this.cemeteryNumber = cemeteryNumber; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }
    public String getQrcodeCode() { return qrcodeCode; }
    public void setQrcodeCode(String qrcodeCode) { this.qrcodeCode = qrcodeCode; }
    public String getQrcodeUrl() { return qrcodeUrl; }
    public void setQrcodeUrl(String qrcodeUrl) { this.qrcodeUrl = qrcodeUrl; }
    public String getIsPublic() { return isPublic; }
    public void setIsPublic(String isPublic) { this.isPublic = isPublic; }
    public String getAllowMessage() { return allowMessage; }
    public void setAllowMessage(String allowMessage) { this.allowMessage = allowMessage; }
    public String getMessageAudit() { return messageAudit; }
    public void setMessageAudit(String messageAudit) { this.messageAudit = messageAudit; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getOrgName() { return orgName; }
    public void setOrgName(String orgName) { this.orgName = orgName; }
    public Integer getTotalVisit() { return totalVisit; }
    public void setTotalVisit(Integer totalVisit) { this.totalVisit = totalVisit; }
    public Integer getMessageCount() { return messageCount; }
    public void setMessageCount(Integer messageCount) { this.messageCount = messageCount; }
    public Integer getFlowerCount() { return flowerCount; }
    public void setFlowerCount(Integer flowerCount) { this.flowerCount = flowerCount; }
}
