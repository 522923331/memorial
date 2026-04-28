package com.ruoyi.memorial.service;

import java.util.List;
import com.ruoyi.memorial.domain.Statistics;

public interface IStatisticsService {
    public List<Statistics> selectStatisticsList(Statistics statistics);
    public Statistics selectStatisticsById(Long statId);
    public Integer getTotalVisitByDeceasedId(Long deceasedId);
    public Integer getTotalMessageByDeceasedId(Long deceasedId);
    public Integer getTotalFlowerByDeceasedId(Long deceasedId);
    public void incrementVisitCount(Long deceasedId);
    public void incrementMessageCount(Long deceasedId);
    public void incrementFlowerCount(Long deceasedId);
    public int deleteStatisticsByIds(Long[] statIds);
}
