package com.ruoyi.memorial.clan.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 族谱关系对象 mem_clan_relation
 * <p>relation_type: 1生父 2生母 3配偶 4养父 5养母 6继父 7继母；
 * 方向 from=子辈/本人，to=父辈/配偶（配偶双向）。</p>
 */
public class ClanRelation extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long relationId;
    private Long clanId;
    private Long fromMemberId;
    private Long toMemberId;
    private Integer relationType;
    /** 配偶第几段婚姻（默认0，父母类为0） */
    private Integer relationOrder;
    private String extra;

    public Long getRelationId() { return relationId; }
    public void setRelationId(Long relationId) { this.relationId = relationId; }
    public Long getClanId() { return clanId; }
    public void setClanId(Long clanId) { this.clanId = clanId; }
    public Long getFromMemberId() { return fromMemberId; }
    public void setFromMemberId(Long fromMemberId) { this.fromMemberId = fromMemberId; }
    public Long getToMemberId() { return toMemberId; }
    public void setToMemberId(Long toMemberId) { this.toMemberId = toMemberId; }
    public Integer getRelationType() { return relationType; }
    public void setRelationType(Integer relationType) { this.relationType = relationType; }
    public Integer getRelationOrder() { return relationOrder; }
    public void setRelationOrder(Integer relationOrder) { this.relationOrder = relationOrder; }
    public String getExtra() { return extra; }
    public void setExtra(String extra) { this.extra = extra; }
}
