package com.enumerate.disease_detection.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.enumerate.disease_detection.Local.UserContextHolder;
import com.enumerate.disease_detection.Mapper.NotificationMapper;
import com.enumerate.disease_detection.POJO.PO.NotificationPO;
import com.enumerate.disease_detection.POJO.VO.NotificationVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    public List<NotificationVO> list(String type, Boolean read) {
        Long userId = UserContextHolder.getUserId();
        QueryWrapper<NotificationPO> qw = new QueryWrapper<>();
        qw.eq("user_id", userId);
        if (type != null) {
            qw.eq("type", type);
        }
        if (read != null) {
            qw.eq("is_read", read);
        }
        qw.orderByDesc("created_at");

        List<NotificationPO> list = notificationMapper.selectList(qw);
        List<NotificationVO> result = new ArrayList<>();
        for (NotificationPO po : list) {
            result.add(NotificationVO.builder()
                    .id(String.valueOf(po.getId()))
                    .type(po.getType())
                    .title(po.getTitle())
                    .content(po.getContent())
                    .read(po.getRead())
                    .createdAt(po.getCreatedAt())
                    .link(po.getLink())
                    .build());
        }
        return result;
    }

    public Integer unreadCount() {
        Long userId = UserContextHolder.getUserId();
        QueryWrapper<NotificationPO> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).eq("is_read", false);
        return Math.toIntExact(notificationMapper.selectCount(qw));
    }

    public boolean markRead(Long id) {
        NotificationPO po = notificationMapper.selectById(id);
        po.setRead(true);
        notificationMapper.updateById(po);
        return true;
    }

    public boolean markAllRead() {
        Long userId = UserContextHolder.getUserId();
        UpdateWrapper<NotificationPO> uw = new UpdateWrapper<>();
        uw.eq("user_id", userId).eq("is_read", false).set("is_read", true);
        notificationMapper.update(null, uw);
        return true;
    }

    public boolean delete(Long id) {
        notificationMapper.deleteById(id);
        return true;
    }
}
