package com.ruoyi.memorial.clan.mapper;

import java.util.List;
import com.ruoyi.memorial.clan.domain.ClanRelation;

public interface ClanRelationMapper {
    public List<ClanRelation> selectRelationsByClanId(Long clanId);

    public ClanRelation selectRelationById(Long relationId);

    public List<ClanRelation> selectRelationsByFromMember(Long fromMemberId);

    public List<ClanRelation> selectRelationsByToMember(Long toMemberId);

    public int insertRelation(ClanRelation relation);

    public int deleteRelationById(Long relationId);

    /** 删族谱时清理关系（物理删除，关系表无 del_flag） */
    public int deleteRelationsByClanId(Long clanId);

    /** 删成员时清理其相关关系（from 或 to 命中） */
    public int deleteRelationsByMember(Long memberId);
}
