package com.ruoyi.memorial.clan.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.memorial.clan.domain.ClanMember;

public interface ClanMemberMapper {
    public List<ClanMember> selectMemberList(ClanMember member);

    public ClanMember selectMemberById(Long memberId);

    /** 按关联逝者ID查询成员（纪念馆->族谱入口用） */
    public ClanMember selectByDeceasedId(Long deceasedId);

    public List<ClanMember> selectMembersByClanId(Long clanId);

    public int countByClanId(Long clanId);

    public int insertMember(ClanMember member);

    public int updateMember(ClanMember member);

    public int deleteMemberByIds(Long[] memberIds);

    /** 按族谱逻辑删除全部成员（删族谱级联用） */
    public int deleteMembersByClanId(Long clanId);

    /** 更新单个成员世代（refreshGeneration 用） */
    public int updateGeneration(@Param("memberId") Long memberId, @Param("generation") Integer generation);
}
