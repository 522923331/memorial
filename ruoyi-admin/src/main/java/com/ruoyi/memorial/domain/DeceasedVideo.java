package com.ruoyi.memorial.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 逝者视频对象 mem_deceased_video
 */
public class DeceasedVideo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long videoId;
    private Long deceasedId;
    private String title;
    private String videoUrl;
    private String coverUrl;
    private String description;
    private Integer sortOrder;

    public Long getVideoId() { return videoId; }
    public void setVideoId(Long videoId) { this.videoId = videoId; }
    public Long getDeceasedId() { return deceasedId; }
    public void setDeceasedId(Long deceasedId) { this.deceasedId = deceasedId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
