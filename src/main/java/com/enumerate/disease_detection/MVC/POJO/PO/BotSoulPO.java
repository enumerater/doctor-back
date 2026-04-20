package com.enumerate.disease_detection.MVC.POJO.PO;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("bot_soul")
public class BotSoulPO {
    private String id;
    private String content;
    private LocalDateTime updatedAt;
}
