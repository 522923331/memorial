package com.ruoyi.memorial.clan.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;
import java.util.List;

/**
 * 族谱成员对象 mem_clan_member
 * <p>既是实体也是世系树节点：spouses/children/hasMemorial 为构建树时填充的展示字段（非 DB 列）。</p>
 */
public class ClanMember extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long memberId;
    private Long clanId;
    private String name;
    /** 性别（0男 1女 2未知） */
    private String gender;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date birthDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date deathDate;
    /** 是否在世（0在世 1已故） */
    private String isAlive;
    private String birthPlace;
    private String title;
    private Integer generation;
    private String avatar;
    private String bio;
    /** 关联逝者纪念馆ID（仅已故+已建馆） */
    private Long deceasedId;
    private Integer sortOrder;
    private String delFlag;

    // ===== 世系树展示字段（非 DB 列，buildTree 时填充） =====
    /** 是否已建纪念馆（deceasedId 非空且逝者存在） */
    private Boolean hasMemorial;
    /** 关联逝者的二维码编码（hasMemorial 时填充，供跳转纪念馆） */
    private String deceasedQrcodeCode;
    private List<ClanMember> spouses;
    private List<ClanMember> children;

    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public Long getClanId() { return clanId; }
    public void setClanId(Long clanId) { this.clanId = clanId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public Date getBirthDate() { return birthDate; }
    public void setBirthDate(Date birthDate) { this.birthDate = birthDate; }
    public Date getDeathDate() { return deathDate; }
    public void setDeathDate(Date deathDate) { this.deathDate = deathDate; }
    public String getIsAlive() { return isAlive; }
    public void setIsAlive(String isAlive) { this.isAlive = isAlive; }
    public String getBirthPlace() { return birthPlace; }
    public void setBirthPlace(String birthPlace) { this.birthPlace = birthPlace; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Integer getGeneration() { return generation; }
    public void setGeneration(Integer generation) { this.generation = generation; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public Long getDeceasedId() { return deceasedId; }
    public void setDeceasedId(Long deceasedId) { this.deceasedId = deceasedId; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }

    public Boolean getHasMemorial() { return hasMemorial; }
    public void setHasMemorial(Boolean hasMemorial) { this.hasMemorial = hasMemorial; }
    public String getDeceasedQrcodeCode() { return deceasedQrcodeCode; }
    public void setDeceasedQrcodeCode(String deceasedQrcodeCode) { this.deceasedQrcodeCode = deceasedQrcodeCode; }
    public List<ClanMember> getSpouses() { return spouses; }
    public void setSpouses(List<ClanMember> spouses) { this.spouses = spouses; }
    public List<ClanMember> getChildren() { return children; }
    public void setChildren(List<ClanMember> children) { this.children = children; }
}
