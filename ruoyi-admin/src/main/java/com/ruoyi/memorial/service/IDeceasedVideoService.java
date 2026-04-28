package com.ruoyi.memorial.service;

import java.util.List;
import com.ruoyi.memorial.domain.DeceasedVideo;

public interface IDeceasedVideoService {
    public List<DeceasedVideo> selectDeceasedVideoList(DeceasedVideo video);
    public DeceasedVideo selectDeceasedVideoById(Long videoId);
    public List<DeceasedVideo> selectDeceasedVideoByDeceasedId(Long deceasedId);
    public int insertDeceasedVideo(DeceasedVideo video);
    public int updateDeceasedVideo(DeceasedVideo video);
    public int deleteDeceasedVideoByIds(Long[] videoIds);
    public int deleteDeceasedVideoById(Long videoId);
}
