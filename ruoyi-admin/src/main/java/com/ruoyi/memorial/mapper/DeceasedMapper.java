package com.ruoyi.memorial.mapper;

import java.util.List;
import com.ruoyi.memorial.domain.Deceased;

public interface DeceasedMapper {
    public List<Deceased> selectDeceasedList(Deceased deceased);
    public Deceased selectDeceasedById(Long deceasedId);
    public Deceased selectDeceasedByQrcode(String qrcodeCode);
    public List<Deceased> selectDeceasedByFamilyId(Long familyUserId);
    public int countByOrgId(Long orgId);
    public int insertDeceased(Deceased deceased);
    public int updateDeceased(Deceased deceased);
    public int deleteDeceasedById(Long deceasedId);
    public int deleteDeceasedByIds(Long[] deceasedIds);
}
