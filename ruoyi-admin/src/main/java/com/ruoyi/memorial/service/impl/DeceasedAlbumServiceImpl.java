package com.ruoyi.memorial.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.memorial.domain.DeceasedAlbum;
import com.ruoyi.memorial.mapper.DeceasedAlbumMapper;
import com.ruoyi.memorial.service.IDeceasedAlbumService;

@Service
public class DeceasedAlbumServiceImpl implements IDeceasedAlbumService {
    @Autowired
    private DeceasedAlbumMapper deceasedAlbumMapper;

    @Override
    public List<DeceasedAlbum> selectAlbumByDeceasedId(Long deceasedId) {
        return deceasedAlbumMapper.selectAlbumByDeceasedId(deceasedId);
    }

    @Override
    public DeceasedAlbum selectAlbumById(Long albumId) {
        return deceasedAlbumMapper.selectAlbumById(albumId);
    }

    @Override
    public int insertAlbum(DeceasedAlbum album) {
        return deceasedAlbumMapper.insertAlbum(album);
    }

    @Override
    public int updateAlbum(DeceasedAlbum album) {
        return deceasedAlbumMapper.updateAlbum(album);
    }

    @Override
    public int deleteAlbumByIds(Long[] albumIds) {
        return deceasedAlbumMapper.deleteAlbumByIds(albumIds);
    }

    @Override
    public int deleteAlbumById(Long albumId) {
        return deceasedAlbumMapper.deleteAlbumById(albumId);
    }
}
