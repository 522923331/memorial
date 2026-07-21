package com.ruoyi.memorial.clan.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 族谱信息对象 mem_clan
 */
public class Clan extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long clanId;
    private String clanName;
    private String surname;
    private Long familyUserId;
    private Long orgId;
    private Long rootMemberId;
    private String coverImage;
    private String description;
    /** 是否公开（0公开 1不公开） */
    private String isPublic;
    /** 在世成员肖像对访客可见（0显示 1不显示） */
    private String showAliveAvatar;
    private String qrcodeCode;
    private String qrcodeUrl;
    private Integer memberCount;
    private Integer generationCount;
    /** 状态（0正常 1停用） */
    private String status;
    private String delFlag;

    public Long getClanId() { return clanId; }
    public void setClanId(Long clanId) { this.clanId = clanId; }
    public String getClanName() { return clanName; }
    public void setClanName(String clanName) { this.clanName = clanName; }
    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }
    public Long getFamilyUserId() { return familyUserId; }
    public void setFamilyUserId(Long familyUserId) { this.familyUserId = familyUserId; }
    public Long getOrgId() { return orgId; }
    public void setOrgId(Long orgId) { this.orgId = orgId; }
    public Long getRootMemberId() { return rootMemberId; }
    public void setRootMemberId(Long rootMemberId) { this.rootMemberId = rootMemberId; }
    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getIsPublic() { return isPublic; }
    public void setIsPublic(String isPublic) { this.isPublic = isPublic; }
    public String getShowAliveAvatar() { return showAliveAvatar; }
    public void setShowAliveAvatar(String showAliveAvatar) { this.showAliveAvatar = showAliveAvatar; }
    public String getQrcodeCode() { return qrcodeCode; }
    public void setQrcodeCode(String qrcodeCode) { this.qrcodeCode = qrcodeCode; }
    public String getQrcodeUrl() { return qrcodeUrl; }
    public void setQrcodeUrl(String qrcodeUrl) { this.qrcodeUrl = qrcodeUrl; }
    public Integer getMemberCount() { return memberCount; }
    public void setMemberCount(Integer memberCount) { this.memberCount = memberCount; }
    public Integer getGenerationCount() { return generationCount; }
    public void setGenerationCount(Integer generationCount) { this.generationCount = generationCount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
