package com.github.douyin.controller;

import cn.hutool.core.util.StrUtil;
import com.github.douyin.client.DouYinWorker;
import com.github.douyin.entity.DyUser;
import com.github.douyin.service.ApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.File;

/**
 * @author Bu HuaYang
 */
@RestController
public class DownloadController {

    @Value("${douyin.download.local-path}")
    private String downloadLocalPath;

    private final ApiService apiService;

    private final DouYinWorker douYinWorker;

    public DownloadController(ApiService apiService, DouYinWorker douYinWorker) {
        this.apiService = apiService;
        this.douYinWorker = douYinWorker;
    }

    @GetMapping(path = "/download/user/{uid}")
    public String downloadUser(@PathVariable(name = "uid") String uid) {
        apiService.downloadCollection(uid, new File(downloadLocalPath));
        return "Downloaded";
    }

    @PostMapping(path = "/download")
    public String download(@RequestParam(name = "shareUrl") String shareUrl,
                           @RequestParam(name = "downloadType") String downloadType) {
        DyUser user = douYinWorker.getUserByShareUrl(shareUrl);
        if (user != null) {
            if (StrUtil.equals("2", downloadType)) {
                apiService.downloadCollection(user.getUid(), new File(downloadLocalPath));
            }
            if (StrUtil.equals("1", downloadType)) {
                apiService.downloadSelf(user.getUid(), new File(downloadLocalPath));
            }
        }
        return "Downloaded";
    }
}
