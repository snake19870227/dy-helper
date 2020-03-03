package com.github.douyin.controller;

import com.github.douyin.client.DouYinWorker;
import com.github.douyin.entity.DyUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Bu HuaYang
 */
@RestController
public class UserProfileController {

    private static final Logger logger = LoggerFactory.getLogger(UserProfileController.class);

    private final DouYinWorker douYinWorker;

    public UserProfileController(DouYinWorker douYinWorker) {
        this.douYinWorker = douYinWorker;
    }

//    @GetMapping(path = "/user/{uid}/{secUid}")
//    public DyUser profile(@PathVariable(name = "uid") String uid,
//                          @PathVariable(name = "secUid") String secUid) {
//        return douYinWorker.getUser(uid, secUid);
//    }

//    @GetMapping(path = "/user/{uid}")
//    public DyUser profile(@PathVariable(name = "uid") String uid) {
//        return douYinWorker.getUser(uid);
//    }
}
