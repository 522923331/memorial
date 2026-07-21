package com.ruoyi.memorial.clan.service;

import java.util.List;
import com.ruoyi.memorial.clan.domain.ClanMember;

public interface IClanMemberService {
    public List<ClanMember> selectMemberList(ClanMember member);

    public ClanMember selectMemberById(Long memberId);

    public ClanMember selectByDeceasedId(Long deceasedId);

    public List<ClanMember> selectMembersByClanId(Long clanId);

    public int insertMember(ClanMember member);

    public int updateMember(ClanMember member);

    public int deleteMemberByIds(Long[] memberIds);
}
