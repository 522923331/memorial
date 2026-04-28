package com.ruoyi.memorial.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.memorial.domain.DeceasedVideo;
import com.ruoyi.memorial.mapper.DeceasedVideoMapper;
import com.ruoyi.memorial.service.IDeceasedVideoService;

@Service
public class DeceasedVideoServiceImpl implements IDeceasedVideoService {
    @Autowired
    private DeceasedVideoMapper deceasedVideoMapper;

    @Override
    public List<DeceasedVideo> selectDeceasedVideoList(DeceasedVideo video) {
        return deceasedVideoMapper.selectDeceasedVideoList(video);
    }

    @Override
    public DeceasedVideo selectDeceasedVideoById(Long videoId) {
        return deceasedVideoMapper.selectDeceasedVideoById(videoId);
    }

    @Override
    public List<DeceasedVideo> selectDeceasedVideoByDeceasedId(Long deceasedId) {
        return deceasedVideoMapper.selectDeceasedVideoByDeceasedId(deceasedId);
    }

    @Override
    public int insertDeceasedVideo(DeceasedVideo video) {
        return deceasedVideoMapper.insertDeceasedVideo(video);
    }

    @Override
    public int updateDeceasedVideo(DeceasedVideo video) {
        return deceasedVideoMapper.updateDeceasedVideo(video);
    }

    @Override
    public int deleteDeceasedVideoByIds(Long[] videoIds) {
        return deceasedVideoMapper.deleteDeceasedVideoByIds(videoIds);
    }

    @Override
    public int deleteDeceasedVideoById(Long videoId) {
        return deceasedVideoMapper.deleteDeceasedVideoById(videoId);
    }
}
