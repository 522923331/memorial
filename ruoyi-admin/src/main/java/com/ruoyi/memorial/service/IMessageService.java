package com.ruoyi.memorial.service;

import java.util.List;
import com.ruoyi.memorial.domain.Message;

public interface IMessageService {
    public List<Message> selectMessageList(Message message);
    public Message selectMessageById(Long messageId);
    public List<Message> selectApprovedMessagesByDeceasedId(Long deceasedId);
    public int insertMessage(Message message);
    public int auditMessage(Long messageId, String isAudited);
    public int batchAuditMessage(Long[] messageIds, String isAudited);
    public int deleteMessageByIds(Long[] messageIds);
    public int deleteMessageById(Long messageId);
}
