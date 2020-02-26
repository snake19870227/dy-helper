package com.github.douyin.controller;

import com.github.douyin.message.SubMessage;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

/**
 * @author Bu HuaYang
 */
@Controller
public class WebSocketController {

    @SubscribeMapping("/topic/log")
    public SubMessage subLogMessage() {
        return new SubMessage("订阅成功");
    }
}
