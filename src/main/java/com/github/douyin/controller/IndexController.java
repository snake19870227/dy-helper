package com.github.douyin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author Bu HuaYang
 */
@Controller
public class IndexController {

    @GetMapping(path = "/")
    public String index() {
        return "index";
    }
}
