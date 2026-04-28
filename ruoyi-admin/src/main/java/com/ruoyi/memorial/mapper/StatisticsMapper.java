package com.ruoyi.memorial.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.memorial.domain.Statistics;

public interface StatisticsMapper {
    public List<Statistics> selectStatisticsList(Statistics statistics);
    public Statistics selectStatisticsById(Long statId);
    public Statistics selectStatisticsByDeceasedAndDate(@Param("deceasedId") Long deceasedId, @Param("visitDate") String visitDate);
    public Integer getTotalVisitByDeceasedId(Long deceasedId);
    public Integer getTotalMessageByDeceasedId(Long deceasedId);
    public Integer getTotalFlowerByDeceasedId(Long deceasedId);
    public List<Long> selectAllDeceasedIds();
    public int insertStatistics(Statistics statistics);
    public int updateStatistics(Statistics statistics);
    public int incrementVisitCount(@Param("deceasedId") Long deceasedId, @Param("visitDate") String visitDate);
    public int incrementMessageCount(@Param("deceasedId") Long deceasedId, @Param("visitDate") String visitDate);
    public int incrementFlowerCount(@Param("deceasedId") Long deceasedId, @Param("visitDate") String visitDate);
    public int deleteStatisticsById(Long statId);
    public int deleteStatisticsByIds(Long[] statIds);
}
