package com.ruoyi.memorial.service.impl;

import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.memorial.domain.Deceased;
import com.ruoyi.memorial.mapper.DeceasedMapper;
import com.ruoyi.memorial.service.IDeceasedService;

@Service
public class DeceasedServiceImpl implements IDeceasedService {
    @Autowired
    private DeceasedMapper deceasedMapper;

    @Override
    public List<Deceased> selectDeceasedList(Deceased deceased) {
        return deceasedMapper.selectDeceasedList(deceased);
    }

    @Override
    public Deceased selectDeceasedById(Long deceasedId) {
        return deceasedMapper.selectDeceasedById(deceasedId);
    }

    @Override
    public Deceased selectDeceasedByQrcode(String qrcodeCode) {
        return deceasedMapper.selectDeceasedByQrcode(qrcodeCode);
    }

    @Override
    public List<Deceased> selectDeceasedByFamilyId(Long familyUserId) {
        return deceasedMapper.selectDeceasedByFamilyId(familyUserId);
    }

    @Override
    public String generateQrcodeCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    @Override
    public int insertDeceased(Deceased deceased) {
        return deceasedMapper.insertDeceased(deceased);
    }

    @Override
    public int updateDeceased(Deceased deceased) {
        return deceasedMapper.updateDeceased(deceased);
    }

    @Override
    public int deleteDeceasedByIds(Long[] deceasedIds) {
        return deceasedMapper.deleteDeceasedByIds(deceasedIds);
    }

    @Override
    public int deleteDeceasedById(Long deceasedId) {
        return deceasedMapper.deleteDeceasedById(deceasedId);
    }
}
