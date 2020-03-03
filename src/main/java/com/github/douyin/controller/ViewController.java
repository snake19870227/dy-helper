package com.github.douyin.controller;

import cn.hutool.core.io.IoUtil;
import cn.hutool.extra.emoji.EmojiUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gargoylesoftware.htmlunit.UnexpectedPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.github.douyin.client.CacheAjaxController;
import com.github.douyin.client.DouYinApi;
import com.github.douyin.client.DouYinWorker;
import com.github.douyin.entity.DyUser;
import com.github.douyin.entity.DyVideo;
import com.github.douyin.message.LogMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Bu HuaYang
 */
@Controller
public class ViewController {

    private static final Logger logger = LoggerFactory.getLogger(ViewController.class);

    private static Map<String, WebRequest> lastPageRequest = new HashMap<>();

    @Value("${douyin.download.local-path}")
    private String downloadLocalPath;

    private final ObjectMapper objectMapper;

    private final DouYinWorker douYinWorker;

    public ViewController(ObjectMapper objectMapper, DouYinWorker douYinWorker) {
        this.objectMapper = objectMapper;
        this.douYinWorker = douYinWorker;
    }

    @GetMapping(path = "/view/user/{uid}")
    @ResponseBody
    public Object view(@PathVariable(name = "uid") String uid,
                       @RequestParam(name = "cursor", defaultValue = "0") long cursor) {
        final WebClient webClient = new WebClient();
        WebRequest h5UserVideoPageRequest = lastPageRequest.get(uid);
        if (h5UserVideoPageRequest == null) {
            List<WebRequest> ajaxPageRequests = new ArrayList<>();
            webClient.setAjaxController(new CacheAjaxController(ajaxPageRequests));
            try {
                String shareUrl = DouYinApi.createShareUrlByUid(uid);
                webClient.getPage(shareUrl);
            } catch (IOException e) {
                logger.error("模拟浏览器访问分析链接失败", e);
            }
            if (ajaxPageRequests.isEmpty()) {
                logger.warn("未得到获取用户视频列表ajax请求地址");
            }
            h5UserVideoPageRequest = ajaxPageRequests.get(0);
            lastPageRequest.clear();
            lastPageRequest.put(uid, h5UserVideoPageRequest);
        }
        if (h5UserVideoPageRequest != null) {
            String defUrl = DouYinApi.getBaseH5VideoListUrl(h5UserVideoPageRequest.getUrl().toString());
            String listUrl = defUrl + "&max_cursor=" + cursor;
            logger.info("拉取视频列表:{}", listUrl);
            try {
                h5UserVideoPageRequest.setUrl(new URL(listUrl));
                UnexpectedPage jsonPage = webClient.getPage(h5UserVideoPageRequest);
                String jsonStr = IoUtil.read(jsonPage.getInputStream(), StandardCharsets.UTF_8);
                JsonNode rootNode = objectMapper.readTree(jsonStr);
                JsonNode awemeList = rootNode.get("aweme_list");
                if (awemeList != null) {
                    File rootPath = new File(downloadLocalPath);
                    Map<String, DyUser> userMap = new HashMap<>();
                    Map<String, String> existsMap = new HashMap<>();
                    for (JsonNode awemeNode : awemeList) {
                        JsonNode authorNode = awemeNode.get("author");
                        JsonNode videoNode = awemeNode.get("video");
                        if (authorNode != null && videoNode != null) {
                            String userId = authorNode.get("uid").textValue();
                            DyUser user = userMap.get(userId);
                            if (user == null) {
                                String shortId = authorNode.get("short_id").textValue();
                                String nickname = authorNode.get("nickname").textValue();
                                nickname = EmojiUtil.containsEmoji(nickname) ? EmojiUtil.removeAllEmojis(nickname) : nickname;
                                user = new DyUser(userId, shortId, nickname);
                                userMap.put(userId, user);
                            }
                            File profilePath = douYinWorker.createProfilePath(user, rootPath);

                            String videoId = videoNode.get("vid").textValue();
                            DyVideo video = new DyVideo(videoId);
                            video.setUser(user);

                            File videoFile = new File(profilePath, video.getVideoId() + ".mp4");
                            if (videoFile.exists()) {
                                existsMap.put(videoId, videoFile.toString());
                            }
                        }
                    }
                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("videosInfo", jsonStr);
                    resultMap.put("exists", existsMap);
                    return resultMap;
                }
            } catch (IOException e) {
                logger.error("拉取视频列表失败", e);
            }
        }
        return null;
    }
}
