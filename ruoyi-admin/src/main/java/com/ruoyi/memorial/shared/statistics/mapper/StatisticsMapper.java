package com.ruoyi.memorial.shared.statistics.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.memorial.shared.statistics.domain.Statistics;

public interface StatisticsMapper {
    public List<Statistics> selectStatisticsList(Statistics statistics);

    public Statistics selectStatisticsBySubjectAndDate(@Param("subjectType") Integer subjectType,
                                                       @Param("subjectId") Long subjectId,
                                                       @Param("visitDate") String visitDate);

    public Integer getTotalVisit(@Param("subjectType") Integer subjectType, @Param("subjectId") Long subjectId);

    public Integer getTotalMessage(@Param("subjectType") Integer subjectType, @Param("subjectId") Long subjectId);

    public Integer getTotalFlower(@Param("subjectType") Integer subjectType, @Param("subjectId") Long subjectId);

    public List<Long> selectAllSubjectIds(@Param("subjectType") Integer subjectType);

    public int insertStatistics(Statistics statistics);

    public int updateStatistics(Statistics statistics);

    public int incrementVisitCount(@Param("subjectType") Integer subjectType,
                                   @Param("subjectId") Long subjectId,
                                   @Param("visitDate") String visitDate);

    public int incrementMessageCount(@Param("subjectType") Integer subjectType,
                                     @Param("subjectId") Long subjectId,
                                     @Param("visitDate") String visitDate);

    public int incrementFlowerCount(@Param("subjectType") Integer subjectType,
                                     @Param("subjectId") Long subjectId,
                                     @Param("visitDate") String visitDate);

    public int deleteStatisticsById(Long statId);

    public int deleteStatisticsByIds(Long[] statIds);
}
