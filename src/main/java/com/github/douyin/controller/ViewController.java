package com.github.douyin.controller;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.emoji.EmojiUtil;
import cn.hutool.http.HttpUtil;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.gargoylesoftware.htmlunit.UnexpectedPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.github.douyin.client.CacheAjaxController;
import com.github.douyin.client.DouYinApi;
import com.github.douyin.client.DouYinWorker;
import com.github.douyin.client.DyApi;
import com.github.douyin.dao.DyUserRepository;
import com.github.douyin.entity.DyLocalVideo;
import com.github.douyin.entity.DyUser;
import com.github.douyin.entity.DyVideo;
import com.github.douyin.message.LogMessage;
import org.elasticsearch.index.query.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Bu HuaYang
 */
@Controller
public class ViewController {

    private static final Logger logger = LoggerFactory.getLogger(ViewController.class);

    private static Map<String, WebRequest> lastPageRequest = new HashMap<>();

    private WebClient webClient;

    @Value("${douyin.download.local-path}")
    private String downloadLocalPath;

    @Value("${douyin.download.local-http-path}")
    private String downloadLocalHttpPath;

    private final ObjectMapper objectMapper;

    private final DouYinWorker douYinWorker;

    private final DyUserRepository dyUserRepository;

    public ViewController(DouYinWorker douYinWorker, DyUserRepository dyUserRepository) {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        this.douYinWorker = douYinWorker;
        this.dyUserRepository = dyUserRepository;
        this.webClient = new WebClient();
    }

    @GetMapping(path = "/view/user")
    @ResponseBody
    public Object view(@RequestParam(name = "searchText") String searchText,
                       @RequestParam(name = "cursor", defaultValue = "1") long cursor) {

        String uid = null;
        String shareUrl = null;

        if (StrUtil.startWithIgnoreCase(searchText, "http")) {
            shareUrl = searchText;
            uid = douYinWorker.getUserIdByShareUrl(shareUrl);
        } else {
            uid = searchText;
            shareUrl = DouYinApi.createShareUrlByUid(uid);
        }

        WebRequest h5UserVideoPageRequest = lastPageRequest.get(uid);
        if (h5UserVideoPageRequest == null) {
            List<WebRequest> ajaxPageRequests = new ArrayList<>();
            webClient.setAjaxController(new CacheAjaxController(ajaxPageRequests));
            try {
                HtmlPage page = webClient.getPage(shareUrl);
                DomNode node = page.querySelector("#pagelet-user-info > div.video-tab > div > div.music-tab.tab.get-list.active");
                if (node != null) {
                    HtmlDivision division = page.querySelector("#pagelet-user-info > div.video-tab > div > div.user-tab.tab.get-list");
                    if (division != null) {
                        division.click();
                    }
                }
            } catch (IOException e) {
                logger.error("模拟浏览器访问分析链接失败", e);
            }
            if (ajaxPageRequests.isEmpty()) {
                logger.warn("未得到获取用户视频列表ajax请求地址");
            }
            h5UserVideoPageRequest = ajaxPageRequests.get(ajaxPageRequests.size() - 1);
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
                    Map<String, DyLocalVideo> existsMap = new HashMap<>();
                    for (JsonNode awemeNode : awemeList) {
                        JsonNode authorNode = awemeNode.get("author");
                        JsonNode videoNode = awemeNode.get("video");
                        if (authorNode != null && videoNode != null) {
                            String userId = authorNode.get("uid").textValue();

                            DyUser user = userMap.get(userId);
                            if (user == null) {
                                user = DyApi.buildUserByAwemeNode(authorNode);
                                DyApi.createProfileFile(user, rootPath, true);
                                dyUserRepository.save(user);
                                userMap.put(userId, user);
                            }

                            File profilePath = user.getProfilePath();

                            String videoId = videoNode.get("vid").textValue();
                            DyVideo video = new DyVideo(videoId);
                            video.setUid(user.getUid());

                            File videoFile = new File(profilePath, video.getVideoId() + ".mp4");
                            if (videoFile.exists()) {
                                DyLocalVideo localVideo = new DyLocalVideo();
                                localVideo.setVideo(video);
                                localVideo.setVideoFile(videoFile);
                                localVideo.setLocalUrl(downloadLocalHttpPath + "/" + user.getProfilePath().getName() + "/" + videoFile.getName());
                                existsMap.put(videoId, localVideo);
                            }
                        }
                    }

                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("videosInfo", jsonStr);
                    resultMap.put("exists", existsMap);
                    return resultMap;
                }
            } catch (Exception e) {
                logger.error("拉取视频列表失败", e);
            }
        }
        return null;
    }

    @GetMapping(path = "/users")
    @ResponseBody
    public Map<String, Object> users(@RequestParam(name = "searchStr") String searchStr,
                                     @RequestParam(name = "page", defaultValue = "0") int page) {
        Page<DyUser> pageInfo;
        PageRequest pageRequest = PageRequest.of(page, 20, Sort.by("uid.keyword"));
        if (StrUtil.isNotBlank(searchStr)) {
            MultiMatchQueryBuilder queryBuilder = new MultiMatchQueryBuilder(searchStr, "nickname", "signature");
            NativeSearchQuery query = new NativeSearchQuery(queryBuilder);
            query.setPageable(pageRequest);
            pageInfo = dyUserRepository.search(query);
        } else {
            pageInfo = dyUserRepository.findAll(pageRequest);
        }

        logger.info(pageInfo.getPageable().toString());

        Map<String, Object> resultMap = new HashMap<>();
        if (pageInfo.hasContent()) {
            resultMap.put("userList", pageInfo.getContent());
        }

        Pageable pageable = pageInfo.getPageable();
        resultMap.put("currentPage", pageable.getPageNumber());

        resultMap.put("hasNext", pageInfo.hasNext());
        if (pageInfo.hasNext()) {
            resultMap.put("nextPage", pageable.next().getPageNumber());
        }

        resultMap.put("hasPre", pageInfo.hasPrevious());
        if (pageInfo.hasPrevious()) {
            resultMap.put("prePage", pageable.previousOrFirst().getPageNumber());
        }

        return resultMap;
    }
}
