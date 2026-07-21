package com.ruoyi.memorial.clan.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.memorial.clan.domain.Clan;
import com.ruoyi.memorial.clan.mapper.ClanMapper;
import com.ruoyi.memorial.clan.mapper.ClanMemberMapper;
import com.ruoyi.memorial.clan.mapper.ClanRelationMapper;
import com.ruoyi.memorial.clan.service.IClanService;

@Service
public class ClanServiceImpl implements IClanService {

    @Autowired
    private ClanMapper clanMapper;

    @Autowired
    private ClanMemberMapper clanMemberMapper;

    @Autowired
    private ClanRelationMapper clanRelationMapper;

    @Override
    public List<Clan> selectClanList(Clan clan) {
        return clanMapper.selectClanList(clan);
    }

    @Override
    public Clan selectClanById(Long clanId) {
        return clanMapper.selectClanById(clanId);
    }

    @Override
    public Clan selectClanByQrcode(String qrcodeCode) {
        return clanMapper.selectClanByQrcode(qrcodeCode);
    }

    @Override
    public List<Clan> selectClanByFamilyId(Long familyUserId) {
        return clanMapper.selectClanByFamilyId(familyUserId);
    }

    @Override
    public List<Clan> searchPublicClan(String keyword) {
        return clanMapper.searchPublicClan(keyword);
    }

    @Override
    public int insertClan(Clan clan) {
        return clanMapper.insertClan(clan);
    }

    @Override
    public int updateClan(Clan clan) {
        return clanMapper.updateClan(clan);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteClanByIds(Long[] clanIds) {
        // 级联：物理删关系 + 逻辑删成员 + 逻辑删族谱
        for (Long clanId : clanIds) {
            clanRelationMapper.deleteRelationsByClanId(clanId);
            clanMemberMapper.deleteMembersByClanId(clanId);
        }
        return clanMapper.deleteClanByIds(clanIds);
    }
}
