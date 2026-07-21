package com.ruoyi.memorial.clan.service;

import java.util.List;
import com.ruoyi.memorial.clan.domain.ClanRelation;

public interface IClanRelationService {
    public List<ClanRelation> selectRelationsByClanId(Long clanId);

    public ClanRelation selectRelationById(Long relationId);

    public List<ClanRelation> selectRelationsByFromMember(Long fromMemberId);

    public List<ClanRelation> selectRelationsByToMember(Long toMemberId);

    public int insertRelation(ClanRelation relation);

    public int deleteRelationById(Long relationId);

    public int deleteRelationsByMember(Long memberId);
}
