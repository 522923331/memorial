package com.ruoyi.memorial.mapper;

import java.util.List;
import com.ruoyi.memorial.domain.DeceasedAlbum;

public interface DeceasedAlbumMapper {
    public List<DeceasedAlbum> selectAlbumByDeceasedId(Long deceasedId);
    public DeceasedAlbum selectAlbumById(Long albumId);
    public int insertAlbum(DeceasedAlbum album);
    public int batchInsertAlbum(List<DeceasedAlbum> albumList);
    public int updateAlbum(DeceasedAlbum album);
    public int deleteAlbumById(Long albumId);
    public int deleteAlbumByIds(Long[] albumIds);
    public int deleteAlbumByDeceasedId(Long deceasedId);
}
