package com.ruoyi.memorial.clan.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.memorial.clan.domain.ClanRelation;
import com.ruoyi.memorial.clan.mapper.ClanRelationMapper;
import com.ruoyi.memorial.clan.service.IClanRelationService;

@Service
public class ClanRelationServiceImpl implements IClanRelationService {

    @Autowired
    private ClanRelationMapper clanRelationMapper;

    @Override
    public ClanRelation selectRelationById(Long relationId) {
        return clanRelationMapper.selectRelationById(relationId);
    }

    @Override
    public List<ClanRelation> selectRelationsByClanId(Long clanId) {
        return clanRelationMapper.selectRelationsByClanId(clanId);
    }

    @Override
    public List<ClanRelation> selectRelationsByFromMember(Long fromMemberId) {
        return clanRelationMapper.selectRelationsByFromMember(fromMemberId);
    }

    @Override
    public List<ClanRelation> selectRelationsByToMember(Long toMemberId) {
        return clanRelationMapper.selectRelationsByToMember(toMemberId);
    }

    @Override
    public int insertRelation(ClanRelation relation) {
        return clanRelationMapper.insertRelation(relation);
    }

    @Override
    public int deleteRelationById(Long relationId) {
        return clanRelationMapper.deleteRelationById(relationId);
    }

    @Override
    public int deleteRelationsByMember(Long memberId) {
        return clanRelationMapper.deleteRelationsByMember(memberId);
    }
}
