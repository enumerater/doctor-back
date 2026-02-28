package com.enumerate.disease_detection.MVC.Service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enumerate.disease_detection.Local.UserContextHolder;
import com.enumerate.disease_detection.MVC.Mapper.NotificationMapper;
import com.enumerate.disease_detection.MVC.POJO.PO.AnnouncementPO;
import com.enumerate.disease_detection.MVC.POJO.PO.NotificationPO;
import com.enumerate.disease_detection.MVC.POJO.VO.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationService {
    @Autowired
    private NotificationMapper notificationMapper;

    public NotificationPageQueryVO listAnnouncements(Integer page, Integer pageSize, String type,
                                                     Integer readStatus, String priority, String keyword) {
        // 1. 分页参数校验
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1 || pageSize > 100) {
            pageSize = 20;
        }

        // 2. 构建分页对象
        Page<NotificationPO> pageParam = new Page<>(page, pageSize);

        // 3. 构建查询条件（使用 StringUtils.isNotBlank 判断非空且非空白）
        QueryWrapper<NotificationPO> queryWrapper = new QueryWrapper<>();
        // 仅当 type 非空且非空白字符串时，添加 type 条件
        queryWrapper.eq(StringUtils.isNotBlank(type), "type", type);
        // readStatus 仅当不为 null 时添加（整型参数）
        queryWrapper.eq(readStatus != null, "is_readd", readStatus);
        // keyword 仅当非空且非空白时，模糊查询 title
        queryWrapper.like(StringUtils.isNotBlank(keyword), "title", keyword);
        // priority 仅当非空且非空白时添加
        queryWrapper.eq(StringUtils.isNotBlank(priority), "priority", priority);

        queryWrapper.eq("user_id", UserContextHolder.getUserId());

        // 4. 计算未读数量（创建新的 QueryWrapper，避免污染原查询条件）
        QueryWrapper<NotificationPO> unreadQueryWrapper = new QueryWrapper<>();
        // 复制原查询的基础条件（type/priority/keyword）
        if (StringUtils.isNotBlank(type)) {
            unreadQueryWrapper.eq("type", type);
        }
        if (StringUtils.isNotBlank(priority)) {
            unreadQueryWrapper.eq("priority", priority);
        }
        if (StringUtils.isNotBlank(keyword)) {
            unreadQueryWrapper.like("title", keyword);
        }
        // 添加未读条件（readd = 0，适配数据库整型字段）
        unreadQueryWrapper.eq("is_readd", 0);

        unreadQueryWrapper.eq("user_id", UserContextHolder.getUserId());

        int unreadCount = Math.toIntExact(notificationMapper.selectCount(unreadQueryWrapper));

        // 5. 执行分页查询
        Page<NotificationPO> notificationPage = notificationMapper.selectPage(pageParam, queryWrapper);
        List<NotificationPO> records = notificationPage.getRecords();
        List<NotificationVO> notificationVOList = new ArrayList<>();
        for (NotificationPO record : records) {
            notificationVOList.add(NotificationVO.builder()
                    .id(record.getId().toString())
                    .type(record.getType())
                    .title(record.getTitle())
                    .content(record.getContent())
                    .priority(record.getPriority())
                    .read(record.getIsRead())
                    .createdAt(record.getCreatedAt())
                    .link(record.getLink())
                    .build());
        }

        // 6. 封装返回结果
        return NotificationPageQueryVO.builder()
                .list(notificationVOList)
                .total((int) notificationPage.getTotal())
                .unreadTotal(unreadCount)
                .build();
    }

    public void markRead(String[] ids) {
        for (String id : ids) {
            NotificationPO notificationPO = notificationMapper.selectById(id);
            notificationPO.setIsRead(true);
            notificationMapper.updateById(notificationPO);
        }
    }

    public void markAllRead() {
        notificationMapper.update( new UpdateWrapper<NotificationPO>().eq("user_id", UserContextHolder.getUserId()).set("is_readd", true));
    }

    public void delete(String[] ids) {
        for (String id : ids){
            notificationMapper.deleteById(id);
        }
    }

    public void deleteAll() {
        notificationMapper.delete(new QueryWrapper<NotificationPO>().eq("user_id", UserContextHolder.getUserId()));
    }


    public AnnouncementReadVO getUnreadSummary() {
        AnnouncementReadVO announcementReadVO = new AnnouncementReadVO();
        Integer total = Math.toIntExact(notificationMapper.selectCount(new QueryWrapper<NotificationPO>().eq("is_readd", false).eq("user_id", UserContextHolder.getUserId())));
        announcementReadVO.setTotal(total);
        announcementReadVO.setTypes(AnnounceTypes.builder()
                .system(Math.toIntExact(notificationMapper.selectCount(new QueryWrapper<NotificationPO>().eq("type", "system").eq("is_readd", false))))
                .disease_alert(Math.toIntExact(notificationMapper.selectCount(new QueryWrapper<NotificationPO>().eq("type", "disease_alert").eq("is_readd", false))))
                .treatment_remind(Math.toIntExact(notificationMapper.selectCount(new QueryWrapper<NotificationPO>().eq("type", "treatment_remind").eq("is_readd", false))))
                .build());

        return announcementReadVO;
    }
}
