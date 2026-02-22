package com.enumerate.disease_detection.Controller;

import com.enumerate.disease_detection.Common.Result;
import com.enumerate.disease_detection.POJO.VO.NotificationVO;
import com.enumerate.disease_detection.Service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notification")
@Slf4j
@CrossOrigin
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/list")
    public Result<List<NotificationVO>> list(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean read) {
        log.info("notification list: type={}, read={}", type, read);
        List<NotificationVO> result = notificationService.list(type, read);
        return Result.success(result);
    }

    @GetMapping("/unread-count")
    public Result<Integer> unreadCount() {
        log.info("unread count");
        Integer count = notificationService.unreadCount();
        return Result.success(count);
    }

    @PutMapping("/{id}/read")
    public Result<Boolean> markRead(@PathVariable Long id) {
        log.info("markRead: {}", id);
        boolean result = notificationService.markRead(id);
        return Result.success(result);
    }

    @PutMapping("/read-all")
    public Result<Boolean> markAllRead() {
        log.info("markAllRead");
        boolean result = notificationService.markAllRead();
        return Result.success(result);
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        log.info("delete notification: {}", id);
        boolean result = notificationService.delete(id);
        return Result.success(result);
    }
}
