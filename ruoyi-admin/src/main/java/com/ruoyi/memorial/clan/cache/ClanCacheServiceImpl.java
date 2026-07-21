package com.ruoyi.memorial.clan.cache;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.memorial.clan.domain.Clan;
import com.ruoyi.memorial.clan.domain.ClanMember;
import com.ruoyi.memorial.clan.domain.ClanRelation;
import com.ruoyi.memorial.clan.mapper.ClanMapper;
import com.ruoyi.memorial.clan.mapper.ClanMemberMapper;
import com.ruoyi.memorial.clan.mapper.ClanRelationMapper;
import com.ruoyi.memorial.domain.Deceased;
import com.ruoyi.memorial.service.IDeceasedService;
import com.ruoyi.memorial.shared.statistics.service.IStatisticsService;

@Service
public class ClanCacheServiceImpl implements IClanCacheService {

    @Autowired
    private ClanMapper clanMapper;

    @Autowired
    private ClanMemberMapper clanMemberMapper;

    @Autowired
    private ClanRelationMapper clanRelationMapper;

    @Autowired
    private IStatisticsService statisticsService;

    @Autowired
    private IDeceasedService deceasedService;

    private static final int TYPE_FATHER = 1;
    private static final int TYPE_MOTHER = 2;
    private static final int TYPE_SPOUSE = 3;

    @Override
    public void refreshGeneration(Long clanId) {
        Clan clan = clanMapper.selectClanById(clanId);
        if (clan == null) return;
        List<ClanMember> members = clanMemberMapper.selectMembersByClanId(clanId);
        if (members.isEmpty()) return;
        List<ClanRelation> relations = clanRelationMapper.selectRelationsByClanId(clanId);

        Map<Long, List<Long>> parentToChildren = new HashMap<>();
        Set<Long> hasParent = new HashSet<>();
        for (ClanRelation r : relations) {
            Integer t = r.getRelationType();
            if (t != null && (t == TYPE_FATHER || t == TYPE_MOTHER)) {
                parentToChildren.computeIfAbsent(r.getToMemberId(), k -> new ArrayList<>()).add(r.getFromMemberId());
                hasParent.add(r.getFromMemberId());
            }
        }

        // 根集合：指定始祖优先；否则取无生父母者
        List<Long> roots = new ArrayList<>();
        if (clan.getRootMemberId() != null) {
            roots.add(clan.getRootMemberId());
        } else {
            for (ClanMember m : members) {
                if (!hasParent.contains(m.getMemberId())) roots.add(m.getMemberId());
            }
        }

        // BFS 标世代（始祖=1）
        Map<Long, Integer> genMap = new HashMap<>();
        Deque<Long> queue = new ArrayDeque<>();
        for (Long root : roots) {
            if (!genMap.containsKey(root)) {
                genMap.put(root, 1);
                queue.add(root);
            }
        }
        while (!queue.isEmpty()) {
            Long cur = queue.poll();
            int nextGen = genMap.get(cur) + 1;
            List<Long> children = parentToChildren.get(cur);
            if (children != null) {
                for (Long child : children) {
                    if (!genMap.containsKey(child)) {
                        genMap.put(child, nextGen);
                        queue.add(child);
                    }
                }
            }
        }

        // 写回（未达成员兜底 generation=1）
        for (ClanMember m : members) {
            int gen = genMap.getOrDefault(m.getMemberId(), 1);
            if (m.getGeneration() == null || m.getGeneration() != gen) {
                clanMemberMapper.updateGeneration(m.getMemberId(), gen);
            }
        }
    }

    @Override
    public void refreshCounts(Long clanId) {
        int memberCount = clanMemberMapper.countByClanId(clanId);
        List<ClanMember> members = clanMemberMapper.selectMembersByClanId(clanId);
        int maxGen = 0;
        for (ClanMember m : members) {
            if (m.getGeneration() != null && m.getGeneration() > maxGen) maxGen = m.getGeneration();
        }
        Clan update = new Clan();
        update.setClanId(clanId);
        update.setMemberCount(memberCount);
        update.setGenerationCount(maxGen);
        clanMapper.updateClan(update);
    }

    @Override
    public ClanMember buildTree(Long clanId, Long viewerFamilyUserId) {
        Clan clan = clanMapper.selectClanById(clanId);
        if (clan == null) return null;
        boolean isOwner = viewerFamilyUserId != null && viewerFamilyUserId.equals(clan.getFamilyUserId());

        List<ClanMember> members = clanMemberMapper.selectMembersByClanId(clanId);
        if (members.isEmpty()) return null;
        List<ClanRelation> relations = clanRelationMapper.selectRelationsByClanId(clanId);

        for (ClanMember m : members) {
            if (!isOwner) desensitize(m, clan);
            m.setHasMemorial(m.getDeceasedId() != null);
            if (m.getDeceasedId() != null) {
                Deceased d = deceasedService.selectDeceasedById(m.getDeceasedId());
                if (d != null) {
                    m.setDeceasedQrcodeCode(d.getQrcodeCode());
                }
            }
        }

        Map<Long, ClanMember> memberMap = new LinkedHashMap<>();
        for (ClanMember m : members) memberMap.put(m.getMemberId(), m);

        Map<Long, List<Long>> parentToChildren = new HashMap<>();
        Map<Long, List<Long>> spouseOf = new HashMap<>();
        Set<Long> hasParent = new HashSet<>();
        for (ClanRelation r : relations) {
            Integer t = r.getRelationType();
            if (t != null && (t == TYPE_FATHER || t == TYPE_MOTHER)) {
                parentToChildren.computeIfAbsent(r.getToMemberId(), k -> new ArrayList<>()).add(r.getFromMemberId());
                hasParent.add(r.getFromMemberId());
            } else if (t != null && t == TYPE_SPOUSE) {
                spouseOf.computeIfAbsent(r.getFromMemberId(), k -> new ArrayList<>()).add(r.getToMemberId());
                spouseOf.computeIfAbsent(r.getToMemberId(), k -> new ArrayList<>()).add(r.getFromMemberId());
            }
        }

        // 根：始祖优先；否则取第一个无生父母者
        Long rootId = clan.getRootMemberId();
        if (rootId == null || !memberMap.containsKey(rootId)) {
            rootId = null;
            for (ClanMember m : members) {
                if (!hasParent.contains(m.getMemberId())) { rootId = m.getMemberId(); break; }
            }
        }
        if (rootId == null) return null;

        Set<Long> visited = new HashSet<>();
        return buildNode(rootId, memberMap, parentToChildren, spouseOf, visited);
    }

    private ClanMember buildNode(Long memberId, Map<Long, ClanMember> memberMap,
                                 Map<Long, List<Long>> parentToChildren,
                                 Map<Long, List<Long>> spouseOf, Set<Long> visited) {
        if (visited.contains(memberId)) return null;
        visited.add(memberId);
        ClanMember node = memberMap.get(memberId);
        if (node == null) return null;

        // 配偶：浅拷贝，避免循环引用（配偶不展开子树，子女挂主线）
        List<Long> spouseIds = spouseOf.get(memberId);
        if (spouseIds != null && !spouseIds.isEmpty()) {
            List<ClanMember> spouses = new ArrayList<>();
            for (Long sid : spouseIds) {
                ClanMember s = memberMap.get(sid);
                if (s != null) spouses.add(shallowCopy(s));
            }
            node.setSpouses(spouses);
        }
        // 子代：递归
        List<Long> childIds = parentToChildren.get(memberId);
        if (childIds != null && !childIds.isEmpty()) {
            List<ClanMember> children = new ArrayList<>();
            for (Long cid : childIds) {
                ClanMember child = buildNode(cid, memberMap, parentToChildren, spouseOf, visited);
                if (child != null) children.add(child);
            }
            node.setChildren(children);
        }
        return node;
    }

    /** 浅拷贝成员（仅基本信息），用于配偶展示，避免树循环引用 */
    private ClanMember shallowCopy(ClanMember s) {
        ClanMember c = new ClanMember();
        c.setMemberId(s.getMemberId());
        c.setClanId(s.getClanId());
        c.setName(s.getName());
        c.setGender(s.getGender());
        c.setBirthDate(s.getBirthDate());
        c.setDeathDate(s.getDeathDate());
        c.setIsAlive(s.getIsAlive());
        c.setBirthPlace(s.getBirthPlace());
        c.setTitle(s.getTitle());
        c.setGeneration(s.getGeneration());
        c.setAvatar(s.getAvatar());
        c.setBio(s.getBio());
        c.setDeceasedId(s.getDeceasedId());
        c.setSortOrder(s.getSortOrder());
        c.setHasMemorial(s.getHasMemorial());
        c.setDeceasedQrcodeCode(s.getDeceasedQrcodeCode());
        return c;
    }

    /** 在世成员对访客脱敏：隐生卒/简介/字号，肖像按族谱 show_alive_avatar 开关 */
    private void desensitize(ClanMember m, Clan clan) {
        if ("0".equals(m.getIsAlive())) {
            m.setBirthDate(null);
            m.setDeathDate(null);
            m.setBio(null);
            m.setTitle(null);
            if ("1".equals(clan.getShowAliveAvatar())) {
                m.setAvatar("");
            }
        }
    }

    @Override
    public void incrementVisit(Long clanId) {
        statisticsService.incrementVisitCount(IStatisticsService.SUBJECT_TYPE_CLAN, clanId);
    }
}
