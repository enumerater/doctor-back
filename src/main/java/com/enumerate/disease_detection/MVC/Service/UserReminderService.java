package com.enumerate.disease_detection.MVC.Service;

import com.enumerate.disease_detection.MVC.Mapper.NotificationMapper;
import com.enumerate.disease_detection.MVC.Mapper.UserReminderMapper;
import com.enumerate.disease_detection.MVC.POJO.PO.NotificationPO;
import com.enumerate.disease_detection.MVC.POJO.PO.UserReminderPO;
import com.enumerate.disease_detection.Utils.SendMessagesUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
@Slf4j
public class UserReminderService implements CommandLineRunner {

    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private SendMessagesUtils sendMessagesUtils;

    @Autowired
    private UserReminderMapper userReminderMapper; // 修正变量名小写开头（规范）

    // 内存中维护任务实例，用于取消/暂停任务
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    /**
     * 添加动态定时提醒任务（持久化版本）
     * @param userId 用户ID
     * @param content 提醒内容
     * @param cron Spring Cron表达式
     */
    public void addReminderTask(Long userId, String content, String cron) {
        // 1. 生成唯一任务标识
        String taskKey = "REMINDER_" + userId + "_" + UUID.randomUUID().toString().substring(0, 8);

        // 2. 定义任务执行逻辑
        Runnable task = () -> {
            log.info("触发AI定时提醒任务: userId={}, content={}, taskKey={}", userId, content, taskKey);
            // 写入数据库 Notification 表
            NotificationPO notification = NotificationPO.builder()
                    .userId(userId)
                    .type("reminder")
                    .title("AI 助手提醒")
                    .content(content)
                    .isRead(false)
                    .priority("high")
                    .createdAt(LocalDateTime.now())
                    .build();
            notificationMapper.insert(notification);

            // 可选：推送实时消息给前端
            // sendMessagesUtils.sendToUser(userId, "notification_update", "您有一个新的提醒");
        };

        // 3. 注册定时任务
        ScheduledFuture<?> future = taskScheduler.schedule(task, new CronTrigger(cron));
        scheduledTasks.put(taskKey, future);

        // 4. 持久化任务信息到数据库
        UserReminderPO reminder = UserReminderPO.builder()
                .userId(String.valueOf(userId))
                .content(content)
                .cron(cron)
                .status(1)
                .taskKey(taskKey)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        userReminderMapper.insert(reminder);

        log.info("新增定时提醒任务成功: taskKey={}, userId={}", taskKey, userId);
    }

    /**
     * 取消定时提醒任务
     * @param taskId 数据库中的任务ID
     */
    public void cancelReminderTask(Long taskId) {
        // 1. 查询任务信息
        UserReminderPO reminder = userReminderMapper.selectById(taskId);
        if (reminder == null || reminder.getStatus() == 0) {
            log.warn("任务不存在或已禁用: taskId={}", taskId);
            return;
        }

        // 2. 取消内存中的定时任务
        ScheduledFuture<?> future = scheduledTasks.get(reminder.getTaskKey());
        if (future != null) {
            future.cancel(true);
            scheduledTasks.remove(reminder.getTaskKey());
        }

        // 3. 更新数据库任务状态为禁用
        reminder.setStatus(0);
        reminder.setUpdatedAt(LocalDateTime.now());
        userReminderMapper.updateById(reminder);

        log.info("取消定时提醒任务成功: taskId={}, taskKey={}", taskId, reminder.getTaskKey());
    }

    /**
     * 服务器启动时恢复所有启用的定时任务
     */
    @Override
    public void run(String... args) throws Exception {
        log.info("开始恢复启用的定时提醒任务...");
        // 查询所有启用状态的任务
        List<UserReminderPO> enabledTasks = userReminderMapper.selectAllEnabledTasks();

        if (enabledTasks.isEmpty()) {
            log.info("无启用的定时提醒任务需要恢复");
            return;
        }

        // 逐个恢复任务
        for (UserReminderPO task : enabledTasks) {
            try {
                Runnable recoverTask = () -> {
                    log.info("恢复的定时提醒任务触发: userId={}, content={}", task.getUserId(), task.getContent());
                    NotificationPO notification = NotificationPO.builder()
                            .userId(Long.valueOf(task.getUserId()))
                            .type("reminder")
                            .title("AI 助手提醒")
                            .content(task.getContent())
                            .isRead(false)
                            .priority("high")
                            .createdAt(LocalDateTime.now())
                            .build();
                    notificationMapper.insert(notification);
                };

                // 重新注册任务
                ScheduledFuture<?> future = taskScheduler.schedule(recoverTask, new CronTrigger(task.getCron()));
                scheduledTasks.put(task.getTaskKey(), future);
                log.info("恢复定时提醒任务成功: taskKey={}, userId={}", task.getTaskKey(), task.getUserId());
            } catch (Exception e) {
                log.error("恢复定时提醒任务失败: taskId={}, error={}", task.getId(), e.getMessage(), e);
            }
        }

        log.info("定时提醒任务恢复完成，共恢复 {} 个任务", enabledTasks.size());
    }
}