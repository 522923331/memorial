package com.ruoyi.memorial.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.memorial.domain.Message;

public interface MessageMapper {
    public List<Message> selectMessageList(Message message);
    public Message selectMessageById(Long messageId);
    public List<Message> selectApprovedMessagesByDeceasedId(Long deceasedId);
    public int countByDeceasedId(Long deceasedId);
    public int insertMessage(Message message);
    public int updateMessage(Message message);
    public int batchUpdateStatus(@Param("status") String status, @Param("array") Long[] messageIds);
    public int deleteMessageById(Long messageId);
    public int deleteMessageByIds(Long[] messageIds);
    public Long countByOrgId(Long orgId);
}
