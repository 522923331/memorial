package com.ruoyi.memorial.clan.cache;

import com.ruoyi.memorial.clan.domain.ClanMember;

/**
 * 族谱派生数据维护层（统一缓存入口）
 * <p>generation / counts / 世系树 / 访问统计均经此层，业务 Service 写操作后委托刷新。</p>
 */
public interface IClanCacheService {

    /** 从始祖 BFS 沿生父母链重算所有成员 generation（始祖=1） */
    void refreshGeneration(Long clanId);

    /** 重算 member_count / generation_count 并写回 mem_clan */
    void refreshCounts(Long clanId);

    /**
     * 构建世系树（宝塔式嵌套结构）。
     * @param clanId 族谱ID
     * @param viewerFamilyUserId 访问者用户ID；为族长时不脱敏，否则在世成员脱敏
     * @return 根节点（含 spouses/children 递归）；无成员返回 null
     */
    ClanMember buildTree(Long clanId, Long viewerFamilyUserId);

    /** 族谱访问 +1（委托 IStatisticsService，subjectType=族谱） */
    void incrementVisit(Long clanId);
}
