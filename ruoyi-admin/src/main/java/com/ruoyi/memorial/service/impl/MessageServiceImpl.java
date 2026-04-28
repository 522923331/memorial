package com.ruoyi.memorial.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.memorial.domain.Message;
import com.ruoyi.memorial.mapper.MessageMapper;
import com.ruoyi.memorial.service.IMessageService;

@Service
public class MessageServiceImpl implements IMessageService {
    @Autowired
    private MessageMapper messageMapper;

    @Override
    public List<Message> selectMessageList(Message message) {
        return messageMapper.selectMessageList(message);
    }

    @Override
    public Message selectMessageById(Long messageId) {
        return messageMapper.selectMessageById(messageId);
    }

    @Override
    public List<Message> selectApprovedMessagesByDeceasedId(Long deceasedId) {
        return messageMapper.selectApprovedMessagesByDeceasedId(deceasedId);
    }

    @Override
    public int insertMessage(Message message) {
        return messageMapper.insertMessage(message);
    }

    @Override
    public int auditMessage(Long messageId, String isAudited) {
        Message message = new Message();
        message.setMessageId(messageId);
        message.setIsAudited(isAudited);
        return messageMapper.updateMessage(message);
    }

    @Override
    public int batchAuditMessage(Long[] messageIds, String isAudited) {
        return messageMapper.batchUpdateStatus(isAudited, messageIds);
    }

    @Override
    public int deleteMessageByIds(Long[] messageIds) {
        return messageMapper.deleteMessageByIds(messageIds);
    }

    @Override
    public int deleteMessageById(Long messageId) {
        return messageMapper.deleteMessageById(messageId);
    }
}
