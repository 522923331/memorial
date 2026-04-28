package com.ruoyi.memorial.service;

import java.util.List;
import com.ruoyi.memorial.domain.DeceasedAlbum;

public interface IDeceasedAlbumService {
    public List<DeceasedAlbum> selectAlbumByDeceasedId(Long deceasedId);
    public DeceasedAlbum selectAlbumById(Long albumId);
    public int insertAlbum(DeceasedAlbum album);
    public int updateAlbum(DeceasedAlbum album);
    public int deleteAlbumByIds(Long[] albumIds);
    public int deleteAlbumById(Long albumId);
}
