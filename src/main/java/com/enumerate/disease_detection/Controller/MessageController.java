package com.enumerate.disease_detection.Controller;

import com.enumerate.disease_detection.Common.Result;
import com.enumerate.disease_detection.POJO.PO.ChatMessagePO;
import com.enumerate.disease_detection.Service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @GetMapping
    @CrossOrigin
    public Result<List<ChatMessagePO>> getMessage(@RequestParam String sessionId) {
        List<ChatMessagePO> res = messageService.getMessage(sessionId);
        return Result.success(res);
    }
}
