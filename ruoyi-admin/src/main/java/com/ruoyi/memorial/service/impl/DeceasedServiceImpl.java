package com.ruoyi.memorial.service.impl;

import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
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
        // mem_deceased 中 birth_date / death_date 为 NOT NULL 且无列默认值，name 同样必填；
        // 若直接 insert 会抛 SQLIntegrityConstraintViolationException，前端只能看到 500（见 sys-error.log 的
        // "Column 'birth_date' cannot be null"）。在此做必填校验，返回友好错误；
        // 其余 NOT NULL 字段补默认值，避免显式 NULL 覆盖列默认值同样报错。
        if (deceased.getName() == null || deceased.getName().trim().isEmpty()) {
            throw new ServiceException("逝者姓名不能为空");
        }
        if (deceased.getBirthDate() == null) {
            throw new ServiceException("出生日期不能为空");
        }
        if (deceased.getDeathDate() == null) {
            throw new ServiceException("逝世日期不能为空");
        }
        fillRequiredDefaults(deceased);
        return deceasedMapper.insertDeceased(deceased);
    }

    private void fillRequiredDefaults(Deceased deceased) {
        if (deceased.getOrgId() == null) deceased.setOrgId(0L);
        if (deceased.getFamilyUserId() == null) deceased.setFamilyUserId(0L);
        if (deceased.getGender() == null) deceased.setGender("0");
        if (deceased.getCemeteryArea() == null) deceased.setCemeteryArea("");
        if (deceased.getCemeteryNumber() == null) deceased.setCemeteryNumber("");
        if (deceased.getCemeteryPhoto() == null) deceased.setCemeteryPhoto("");
        if (deceased.getMonumentEraser() == null) deceased.setMonumentEraser("");
        if (deceased.getCoverImage() == null) deceased.setCoverImage("");
        if (deceased.getQrcodeCode() == null) deceased.setQrcodeCode("");
        if (deceased.getQrcodeUrl() == null) deceased.setQrcodeUrl("");
        if (deceased.getIsPublic() == null) deceased.setIsPublic("0");
        if (deceased.getAllowMessage() == null) deceased.setAllowMessage("0");
        if (deceased.getMessageAudit() == null) deceased.setMessageAudit("1");
        if (deceased.getStatus() == null) deceased.setStatus("0");
        if (deceased.getDelFlag() == null) deceased.setDelFlag("0");
        if (deceased.getCreateBy() == null) deceased.setCreateBy("");
        if (deceased.getCreateTime() == null) deceased.setCreateTime(DateUtils.getNowDate());
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
