package com.ruoyi.memorial.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;

/**
 * 访问统计对象 mem_statistics
 */
public class Statistics extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long statId;
    private Long deceasedId;
    private Date visitDate;
    private Integer visitCount;
    private Integer messageCount;
    private Integer flowerCount;
    private String deceasedName;

    public Long getStatId() { return statId; }
    public void setStatId(Long statId) { this.statId = statId; }
    public Long getDeceasedId() { return deceasedId; }
    public void setDeceasedId(Long deceasedId) { this.deceasedId = deceasedId; }
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
