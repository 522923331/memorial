package com.ruoyi.memorial.service;

import java.util.List;
import com.ruoyi.memorial.domain.Deceased;

public interface IDeceasedService {
    public List<Deceased> selectDeceasedList(Deceased deceased);
    public Deceased selectDeceasedById(Long deceasedId);
    public Deceased selectDeceasedByQrcode(String qrcodeCode);
    public List<Deceased> selectDeceasedByFamilyId(Long familyUserId);
    public String generateQrcodeCode();
    public int insertDeceased(Deceased deceased);
    public int updateDeceased(Deceased deceased);
    public int deleteDeceasedByIds(Long[] deceasedIds);
    public int deleteDeceasedById(Long deceasedId);
}
