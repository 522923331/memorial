package com.ruoyi.memorial.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.memorial.domain.Message;
import com.ruoyi.memorial.service.IMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/memorial/message")
public class MessageController extends BaseController {
    @Autowired
    private IMessageService messageService;

    @PreAuthorize("@ss.hasPermi('memorial:message:list')")
    @GetMapping("/list")
    public TableDataInfo list(Message message) {
        startPage();
        List<Message> list = messageService.selectMessageList(message);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('memorial:message:query')")
    @GetMapping("/{messageId}")
    public AjaxResult getInfo(@PathVariable Long messageId) {
        return AjaxResult.success(messageService.selectMessageById(messageId));
    }

    @PreAuthorize("@ss.hasPermi('memorial:message:audit')")
    @Log(title = "留言审核", businessType = BusinessType.UPDATE)
    @PutMapping("/audit")
    public AjaxResult audit(@RequestBody Message message) {
        return toAjax(messageService.auditMessage(message.getMessageId(), message.getIsAudited()));
    }

    @PreAuthorize("@ss.hasPermi('memorial:message:audit')")
    @Log(title = "留言批量审核", businessType = BusinessType.UPDATE)
    @PutMapping("/batchAudit")
    public AjaxResult batchAudit(@RequestParam String status, @RequestParam Long[] messageIds) {
        return toAjax(messageService.batchAuditMessage(messageIds, status));
    }

    @PreAuthorize("@ss.hasPermi('memorial:message:remove')")
    @Log(title = "留言管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{messageIds}")
    public AjaxResult remove(@PathVariable Long[] messageIds) {
        return toAjax(messageService.deleteMessageByIds(messageIds));
    }

    @PreAuthorize("@ss.hasPermi('memorial:message:export')")
    @Log(title = "留言管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public AjaxResult export(Message message) {
        List<Message> list = messageService.selectMessageList(message);
        ExcelUtil<Message> util = new ExcelUtil<Message>(Message.class);
        return util.exportExcel(list, "留言数据");
    }
}
