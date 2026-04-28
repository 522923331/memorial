package com.ruoyi.memorial.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 献花记录对象 mem_flower
 */
public class Flower extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long flowerId;
    private Long deceasedId;
    private String visitorName;
    private Integer flowerType;
    private String message;
    private String ipAddress;
    private String deceasedName;

    public Long getFlowerId() { return flowerId; }
    public void setFlowerId(Long flowerId) { this.flowerId = flowerId; }
    public Long getDeceasedId() { return deceasedId; }
    public void setDeceasedId(Long deceasedId) { this.deceasedId = deceasedId; }
    public String getVisitorName() { return visitorName; }
    public void setVisitorName(String visitorName) { this.visitorName = visitorName; }
    public Integer getFlowerType() { return flowerType; }
    public void setFlowerType(Integer flowerType) { this.flowerType = flowerType; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getDeceasedName() { return deceasedName; }
    public void setDeceasedName(String deceasedName) { this.deceasedName = deceasedName; }
}
