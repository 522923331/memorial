package com.ruoyi.memorial.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.memorial.domain.Statistics;
import com.ruoyi.memorial.mapper.StatisticsMapper;
import com.ruoyi.memorial.service.IStatisticsService;

@Service
public class StatisticsServiceImpl implements IStatisticsService {
    @Autowired
    private StatisticsMapper statisticsMapper;

    @Override
    public List<Statistics> selectStatisticsList(Statistics statistics) {
        return statisticsMapper.selectStatisticsList(statistics);
    }

    @Override
    public Statistics selectStatisticsById(Long statId) {
        return statisticsMapper.selectStatisticsById(statId);
    }

    @Override
    public Integer getTotalVisitByDeceasedId(Long deceasedId) {
        return statisticsMapper.getTotalVisitByDeceasedId(deceasedId);
    }

    @Override
    public Integer getTotalMessageByDeceasedId(Long deceasedId) {
        return statisticsMapper.getTotalMessageByDeceasedId(deceasedId);
    }

    @Override
    public Integer getTotalFlowerByDeceasedId(Long deceasedId) {
        return statisticsMapper.getTotalFlowerByDeceasedId(deceasedId);
    }

    @Override
    public void incrementVisitCount(Long deceasedId) {
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        statisticsMapper.incrementVisitCount(deceasedId, today);
    }

    @Override
    public void incrementMessageCount(Long deceasedId) {
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        statisticsMapper.incrementMessageCount(deceasedId, today);
    }

    @Override
    public void incrementFlowerCount(Long deceasedId) {
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        statisticsMapper.incrementFlowerCount(deceasedId, today);
    }

    @Override
    public int deleteStatisticsByIds(Long[] statIds) {
        return statisticsMapper.deleteStatisticsByIds(statIds);
    }
}
