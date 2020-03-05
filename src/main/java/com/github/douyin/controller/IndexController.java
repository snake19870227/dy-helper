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
        return "view";
    }

    @GetMapping(path = "/view")
    public String view() {
        return "view";
    }

    @GetMapping(path = "/batch")
    public String batch() {
        return "index";
    }

    @GetMapping(path = "/local")
    public String local() {
        return "local";
    }
}
