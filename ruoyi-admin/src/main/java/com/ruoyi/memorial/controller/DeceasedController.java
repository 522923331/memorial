package com.ruoyi.memorial.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.memorial.domain.Deceased;
import com.ruoyi.memorial.domain.DeceasedAlbum;
import com.ruoyi.memorial.service.IDeceasedAlbumService;
import com.ruoyi.memorial.service.IDeceasedService;
import com.ruoyi.memorial.utils.QrCodeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/memorial/deceased")
public class DeceasedController extends BaseController {
    @Autowired
    private IDeceasedService deceasedService;

    @Autowired
    private IDeceasedAlbumService deceasedAlbumService;

    @Autowired
    private QrCodeUtil qrCodeUtil;

    @Value("${ruoyi.profile}")
    private String profilePath;

    @PreAuthorize("@ss.hasPermi('memorial:deceased:list')")
    @GetMapping("/list")
    public TableDataInfo list(Deceased deceased) {
        startPage();
        List<Deceased> list = deceasedService.selectDeceasedList(deceased);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('memorial:deceased:query')")
    @GetMapping("/listByFamily/{familyUserId}")
    public AjaxResult listByFamily(@PathVariable Long familyUserId) {
        List<Deceased> list = deceasedService.selectDeceasedByFamilyId(familyUserId);
        return AjaxResult.success(list);
    }

    @PreAuthorize("@ss.hasPermi('memorial:deceased:query')")
    @GetMapping("/{deceasedId}")
    public AjaxResult getInfo(@PathVariable Long deceasedId) {
        Deceased deceased = deceasedService.selectDeceasedById(deceasedId);
        List<DeceasedAlbum> albums = deceasedAlbumService.selectAlbumByDeceasedId(deceasedId);
        AjaxResult ajax = AjaxResult.success();
        ajax.put("deceased", deceased);
        ajax.put("albums", albums);
        return ajax;
    }

    @PreAuthorize("@ss.hasPermi('memorial:deceased:add')")
    @Log(title = "逝者管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Deceased deceased) throws IOException {
        String qrcodeCode = deceasedService.generateQrcodeCode();
        deceased.setQrcodeCode(qrcodeCode);

        try {
            byte[] qrBytes = qrCodeUtil.generateQrCode("/memorial/" + qrcodeCode);
            String fileName = qrcodeCode + ".png";
            String savePath = profilePath + "/memorial/qrcode";
            File dir = new File(savePath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File qrFile = new File(savePath, fileName);
            try (FileOutputStream fos = new FileOutputStream(qrFile)) {
                fos.write(qrBytes);
            }
            deceased.setQrcodeUrl("/memorial/qrcode/" + fileName);
        } catch (Exception e) {
            logger.error("生成二维码失败", e);
        }

        deceased.setStatus("0");
        deceased.setDelFlag("0");
        deceased.setCreateBy(String.valueOf(getUserId()));
        deceased.setCreateTime(DateUtils.getNowDate());
        return toAjax(deceasedService.insertDeceased(deceased));
    }

    @PreAuthorize("@ss.hasPermi('memorial:deceased:edit')")
    @Log(title = "逝者管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Deceased deceased) {
        deceased.setUpdateBy(String.valueOf(getUserId()));
        deceased.setUpdateTime(DateUtils.getNowDate());
        return toAjax(deceasedService.updateDeceased(deceased));
    }

    @PreAuthorize("@ss.hasPermi('memorial:deceased:remove')")
    @Log(title = "逝者管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{deceasedIds}")
    public AjaxResult remove(@PathVariable Long[] deceasedIds) {
        return toAjax(deceasedService.deleteDeceasedByIds(deceasedIds));
    }

    @PreAuthorize("@ss.hasPermi('memorial:deceased:edit')")
    @Log(title = "逝者相册", businessType = BusinessType.INSERT)
    @PostMapping("/album/upload")
    public AjaxResult uploadAlbum(@RequestParam("deceasedId") Long deceasedId,
                                   @RequestParam("file") MultipartFile file) throws IOException {
        String imageUrl = FileUploadUtils.upload(profilePath + "/memorial/album", file);
        DeceasedAlbum album = new DeceasedAlbum();
        album.setDeceasedId(deceasedId);
        album.setImageUrl(imageUrl);
        album.setSortOrder(0);
        deceasedAlbumService.insertAlbum(album);
        return AjaxResult.success(imageUrl);
    }

    @PreAuthorize("@ss.hasPermi('memorial:deceased:edit')")
    @Log(title = "逝者相册", businessType = BusinessType.DELETE)
    @DeleteMapping("/album/{albumIds}")
    public AjaxResult deleteAlbum(@PathVariable Long[] albumIds) {
        return toAjax(deceasedAlbumService.deleteAlbumByIds(albumIds));
    }

    @PreAuthorize("@ss.hasPermi('memorial:deceased:export')")
    @Log(title = "逝者管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public AjaxResult export(Deceased deceased) {
        List<Deceased> list = deceasedService.selectDeceasedList(deceased);
        ExcelUtil<Deceased> util = new ExcelUtil<Deceased>(Deceased.class);
        return util.exportExcel(list, "逝者数据");
    }
}
