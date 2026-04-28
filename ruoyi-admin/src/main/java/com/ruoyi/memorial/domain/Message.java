package com.ruoyi.memorial.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 留言寄语对象 mem_message
 */
public class Message extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long messageId;
    private Long deceasedId;
    private String visitorName;
    private String visitorPhone;
    private String relation;
    private String content;
    private String isAudited;
    private String ipAddress;
    private String deceasedName;

    public Long getMessageId() { return messageId; }
    public void setMessageId(Long messageId) { this.messageId = messageId; }
    public Long getDeceasedId() { return deceasedId; }
    public void setDeceasedId(Long deceasedId) { this.deceasedId = deceasedId; }
    public String getVisitorName() { return visitorName; }
    public void setVisitorName(String visitorName) { this.visitorName = visitorName; }
    public String getVisitorPhone() { return visitorPhone; }
    public void setVisitorPhone(String visitorPhone) { this.visitorPhone = visitorPhone; }
    public String getRelation() { return relation; }
    public void setRelation(String relation) { this.relation = relation; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getIsAudited() { return isAudited; }
    public void setIsAudited(String isAudited) { this.isAudited = isAudited; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getDeceasedName() { return deceasedName; }
    public void setDeceasedName(String deceasedName) { this.deceasedName = deceasedName; }
}
