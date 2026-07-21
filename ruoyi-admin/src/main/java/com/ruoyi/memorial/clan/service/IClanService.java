package com.ruoyi.memorial.clan.service;

import java.util.List;
import com.ruoyi.memorial.clan.domain.Clan;

public interface IClanService {
    public List<Clan> selectClanList(Clan clan);

    public Clan selectClanById(Long clanId);

    public Clan selectClanByQrcode(String qrcodeCode);

    public List<Clan> selectClanByFamilyId(Long familyUserId);

    public List<Clan> searchPublicClan(String keyword);

    public int insertClan(Clan clan);

    public int updateClan(Clan clan);

    public int deleteClanByIds(Long[] clanIds);
}
