package com.ruoyi.memorial.shared.statistics.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;

/**
 * 访问统计对象 mem_statistics（通用 subject 统计）
 * <p>subject_type: 0逝者 1族谱（未来可扩）；subject_id: deceased_id 或 clan_id</p>
 */
public class Statistics extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long statId;
    private Integer subjectType;
    private Long subjectId;
    private Date visitDate;
    private Integer visitCount;
    private Integer messageCount;
    private Integer flowerCount;
    /** 逝者名（subject_type=0 时 left join mem_deceased 取，仅供展示） */
    private String deceasedName;

    public Long getStatId() { return statId; }
    public void setStatId(Long statId) { this.statId = statId; }
    public Integer getSubjectType() { return subjectType; }
    public void setSubjectType(Integer subjectType) { this.subjectType = subjectType; }
    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }
    public Date getVisitDate() { return visitDate; }
    public void setVisitDate(Date visitDate) { this.visitDate = visitDate; }
    public Integer getVisitCount() { return visitCount; }
    public void setVisitCount(Integer visitCount) { this.visitCount = visitCount; }
    public Integer getMessageCount() { return messageCount; }
    public void setMessageCount(Integer messageCount) { this.messageCount = messageCount; }
    public Integer getFlowerCount() { return flowerCount; }
    public void setFlowerCount(Integer flowerCount) { this.flowerCount = flowerCount; }
    public String getDeceasedName() { return deceasedName; }
    public void setDeceasedName(String deceasedName) { this.deceasedName = deceasedName; }
}
