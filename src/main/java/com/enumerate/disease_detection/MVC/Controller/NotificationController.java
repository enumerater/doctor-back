package com.enumerate.disease_detection.MVC.Controller;

import com.enumerate.disease_detection.Common.Result;
import com.enumerate.disease_detection.MVC.Mapper.AnnouncementMapper;
import com.enumerate.disease_detection.MVC.POJO.DTO.AnnouncementReadDTO;
import com.enumerate.disease_detection.MVC.POJO.PO.NotificationPO;
import com.enumerate.disease_detection.MVC.POJO.VO.AnnouncementPageQueryVO;
import com.enumerate.disease_detection.MVC.POJO.VO.AnnouncementReadVO;
import com.enumerate.disease_detection.MVC.POJO.VO.NotificationPageQueryVO;
import com.enumerate.disease_detection.MVC.POJO.VO.NotificationVO;

import com.enumerate.disease_detection.MVC.Service.AnnouncementService;
import com.enumerate.disease_detection.MVC.Service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/notification")
@Slf4j
@CrossOrigin
public class NotificationController {

    @Autowired
    private NotificationService notificationService;



    @GetMapping("/unread-summary")
    public Result<AnnouncementReadVO> getUnreadSummary() {
        log.info("开始获取未读公告数量");
        AnnouncementReadVO res = notificationService.getUnreadSummary();
        return Result.success(res);
    }

    @GetMapping("/list")
    public Result<NotificationPageQueryVO> listAnnouncements(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer readStatus,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String keyword
    ) {
        log.info("开始列出所有公告");
        NotificationPageQueryVO res = notificationService.listAnnouncements(page, pageSize, type , readStatus, priority, keyword);
        return Result.success(res);
    }

    @PutMapping("/read")
    public Result<String> markRead(@RequestBody AnnouncementReadDTO readDTO) {
        // 1. 获取解析后的数组（此时能正确拿到你的两个ID）
        String[] ids = readDTO.getIds();
        log.info("成功接收数组参数，ids: {}", (Object) ids); // 日志会输出：[2027683319268999170, 2027683499213029377]

        // 2. 空值校验（避免空数组/空指针）
        if (ids == null || ids.length == 0) {
            return Result.error(114,"公告ID参数不能为空");
        }

        // 3. 业务逻辑处理
        if ("all".equals(ids[0])) {
            // 标记所有公告已读
            notificationService.markAllRead();
            log.info("标记所有公告为已读");
        } else {
            // 标记指定ID的公告已读（适配你的数组参数）
            notificationService.markRead(ids);
            log.info("标记指定ID公告为已读，共{}条", ids.length);
        }

        return Result.success("标记已读成功");
    }

    @DeleteMapping("/delete")
    public Result<Boolean> delete(@RequestBody AnnouncementReadDTO readDTO) {
        log.info("开始删除公告");

        String[] ids = readDTO.getIds();
        log.info("成功接收数组参数，ids: {}", (Object) ids);

        // 2. 空值校验（避免空数组/空指针）
        if (ids == null || ids.length == 0) {
            return Result.error(114,"公告ID参数不能为空");
        }

        // 3. 业务逻辑处理
        if ("all".equals(ids[0])) {
            // 标记所有公告已读
            notificationService.deleteAll();
            log.info("标记所有公告为已读");
        } else {
            // 标记指定ID的公告已读（适配你的数组参数）
            notificationService.delete(ids);
            log.info("标记指定ID公告为已读，共{}条", ids.length);
        }

        return Result.success();
    }
}
