package com.ruoyi.memorial.shared.statistics.service;

import java.util.List;
import com.ruoyi.memorial.shared.statistics.domain.Statistics;

/**
 * 统计服务（通用 subject）
 * <p>subject_type: {@link #SUBJECT_TYPE_DECEASED} 逝者 / {@link #SUBJECT_TYPE_CLAN} 族谱</p>
 */
public interface IStatisticsService {

    /** 统计主体类型：逝者 */
    int SUBJECT_TYPE_DECEASED = 0;
    /** 统计主体类型：族谱 */
    int SUBJECT_TYPE_CLAN = 1;

    public List<Statistics> selectStatisticsList(Statistics statistics);

    public Integer getTotalVisit(Integer subjectType, Long subjectId);

    public Integer getTotalMessage(Integer subjectType, Long subjectId);

    public Integer getTotalFlower(Integer subjectType, Long subjectId);

    public void incrementVisitCount(Integer subjectType, Long subjectId);

    public void incrementMessageCount(Integer subjectType, Long subjectId);

    public void incrementFlowerCount(Integer subjectType, Long subjectId);

    public int deleteStatisticsByIds(Long[] statIds);
}
