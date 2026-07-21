package com.ruoyi.memorial.clan.mapper;

import java.util.List;
import com.ruoyi.memorial.clan.domain.Clan;

public interface ClanMapper {
    public List<Clan> selectClanList(Clan clan);

    public Clan selectClanById(Long clanId);

    public Clan selectClanByQrcode(String qrcodeCode);

    public List<Clan> selectClanByFamilyId(Long familyUserId);

    /** 公开搜索（is_public=0, status=0，限 20 条） */
    public List<Clan> searchPublicClan(String keyword);

    public int insertClan(Clan clan);

    public int updateClan(Clan clan);

    public int deleteClanByIds(Long[] clanIds);
}
