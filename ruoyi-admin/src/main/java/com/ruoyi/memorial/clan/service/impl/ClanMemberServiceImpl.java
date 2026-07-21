package com.ruoyi.memorial.clan.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.memorial.clan.domain.ClanMember;
import com.ruoyi.memorial.clan.mapper.ClanMemberMapper;
import com.ruoyi.memorial.clan.mapper.ClanRelationMapper;
import com.ruoyi.memorial.clan.service.IClanMemberService;

@Service
public class ClanMemberServiceImpl implements IClanMemberService {

    @Autowired
    private ClanMemberMapper clanMemberMapper;

    @Autowired
    private ClanRelationMapper clanRelationMapper;

    @Override
    public List<ClanMember> selectMemberList(ClanMember member) {
        return clanMemberMapper.selectMemberList(member);
    }

    @Override
    public ClanMember selectMemberById(Long memberId) {
        return clanMemberMapper.selectMemberById(memberId);
    }

    @Override
    public ClanMember selectByDeceasedId(Long deceasedId) {
        return clanMemberMapper.selectByDeceasedId(deceasedId);
    }

    @Override
    public List<ClanMember> selectMembersByClanId(Long clanId) {
        return clanMemberMapper.selectMembersByClanId(clanId);
    }

    @Override
    public int insertMember(ClanMember member) {
        return clanMemberMapper.insertMember(member);
    }

    @Override
    public int updateMember(ClanMember member) {
        return clanMemberMapper.updateMember(member);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteMemberByIds(Long[] memberIds) {
        // 级联：物理删该成员相关关系 + 逻辑删成员
        for (Long memberId : memberIds) {
            clanRelationMapper.deleteRelationsByMember(memberId);
        }
        return clanMemberMapper.deleteMemberByIds(memberIds);
    }
}
