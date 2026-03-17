package com.enumerate.disease_detection.Tools;

import com.enumerate.disease_detection.Annotations.ToolName;
import com.enumerate.disease_detection.Local.SessionHolder;
import com.enumerate.disease_detection.Local.UserContextHolder;
import com.enumerate.disease_detection.MVC.Service.UserReminderService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
@Slf4j
public class ReminderTool {

    @Autowired
    private UserReminderService userReminderService;

    @Tool("为用户创建定时提醒任务...")
    @ToolName("create_reminder")
    public String createReminder(
            @P("提醒的具体内容") String content,
            @P("Spring Cron表达式## Cron 表达式规范\n" +
                    "1. 格式：Spring标准6位（秒 分 时 日 月 周），取值：秒/分0-59、时0-23、日1-31、月1-12、周1-7（1=周日）；\n" +
                    "2. 支持*、?、/、-、,，禁用年维度；\n" +
                    "3. 农业场景默认：\n" +
                    "   - 每日采集：0 0 6 * * ?\n" +
                    "   - 每小时刷新：0 0 */1 * * ?\n" +
                    "   - 每周周报：0 0 7 * * 2\n" +
                    "   - 每月月报：0 0 8 1 * ?\n" +
                    "4. 生成/解析时校验合法性，错误需修正并说明。") String cron) {

        // 1. 先尝试从 UserContextHolder 获取 (兼容普通的HTTP调用)
        Long userId = UserContextHolder.getUserId();

        // 2. 如果为空，说明是在 WebSocket/异步 线程里，尝试从 WebSocketSession 获取
        if (userId == null) {
            WebSocketSession session = SessionHolder.getSession();
            if (session != null && session.getAttributes().containsKey("userId")) {
                // 假设你在 WebSocketAuthInterceptor 握手时存入了 "userId"
                // 例如: attributes.put("userId", userId);
                userId = Long.valueOf(session.getAttributes().get("userId").toString());
            }
        }

        // 3. 如果还是空，才真正的报错
        if (userId == null) {
            log.error("无法获取上下文中的 userId");
            return "error: 用户未登录或无法获取用户上下文";
        }

        try {
            log.info("创建定时提醒任务：userId={}, content={}, cron={}", userId, content, cron);
            userReminderService.addReminderTask(userId, content, cron);
            return "定时任务创建成功！Cron规则为：" + cron;
        } catch (Exception e) {
            return "任务创建失败：" + e.getMessage();
        }
    }

}