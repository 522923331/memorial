package com.ruoyi.memorial.shared.statistics.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.memorial.shared.statistics.domain.Statistics;
import com.ruoyi.memorial.shared.statistics.mapper.StatisticsMapper;
import com.ruoyi.memorial.shared.statistics.service.IStatisticsService;

@Service
public class StatisticsServiceImpl implements IStatisticsService {

    @Autowired
    private StatisticsMapper statisticsMapper;

    @Override
    public List<Statistics> selectStatisticsList(Statistics statistics) {
        return statisticsMapper.selectStatisticsList(statistics);
    }

    @Override
    public Integer getTotalVisit(Integer subjectType, Long subjectId) {
        return statisticsMapper.getTotalVisit(subjectType, subjectId);
    }

    @Override
    public Integer getTotalMessage(Integer subjectType, Long subjectId) {
        return statisticsMapper.getTotalMessage(subjectType, subjectId);
    }

    @Override
    public Integer getTotalFlower(Integer subjectType, Long subjectId) {
        return statisticsMapper.getTotalFlower(subjectType, subjectId);
    }

    @Override
    public void incrementVisitCount(Integer subjectType, Long subjectId) {
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        statisticsMapper.incrementVisitCount(subjectType, subjectId, today);
    }

    @Override
    public void incrementMessageCount(Integer subjectType, Long subjectId) {
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        statisticsMapper.incrementMessageCount(subjectType, subjectId, today);
    }

    @Override
    public void incrementFlowerCount(Integer subjectType, Long subjectId) {
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        statisticsMapper.incrementFlowerCount(subjectType, subjectId, today);
    }

    @Override
    public int deleteStatisticsByIds(Long[] statIds) {
        return statisticsMapper.deleteStatisticsByIds(statIds);
    }
}
