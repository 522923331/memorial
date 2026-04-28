package com.ruoyi.memorial.mapper;

import java.util.List;
import com.ruoyi.memorial.domain.DeceasedVideo;

public interface DeceasedVideoMapper {
    public List<DeceasedVideo> selectDeceasedVideoList(DeceasedVideo video);
    public DeceasedVideo selectDeceasedVideoById(Long videoId);
    public List<DeceasedVideo> selectDeceasedVideoByDeceasedId(Long deceasedId);
    public int countByDeceasedId(Long deceasedId);
    public int insertDeceasedVideo(DeceasedVideo video);
    public int updateDeceasedVideo(DeceasedVideo video);
    public int deleteDeceasedVideoById(Long videoId);
    public int deleteDeceasedVideoByIds(Long[] videoIds);
    public int deleteDeceasedVideoByDeceasedId(Long deceasedId);
}
