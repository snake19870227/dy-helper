package com.github.douyin.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.gargoylesoftware.htmlunit.UnexpectedPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlVideo;
import com.github.douyin.client.CacheAjaxController;
import com.github.douyin.client.DouYinApi;
import com.github.douyin.client.DouYinWorker;
import com.github.douyin.common.VideoListUrlCreater;
import com.github.douyin.entity.DyLocalVideo;
import com.github.douyin.entity.DyUser;
import com.github.douyin.entity.DyVideo;
import com.github.douyin.message.LogMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Bu HuaYang
 */
@Service
public class ApiService {

    private static final Logger logger = LoggerFactory.getLogger(ApiService.class);

    public static final Pattern pattern = Pattern.compile("href=\"(.*?)\">");

    private final SimpMessagingTemplate messagingTemplate;

    private final DouYinWorker douYinWorker;

    public ApiService(SimpMessagingTemplate messagingTemplate, DouYinWorker douYinWorker) {
        this.messagingTemplate = messagingTemplate;
        this.douYinWorker = douYinWorker;
    }

//    @Async
//    public void downloadCollection(String uid, File rootPath) {
//        download(uid, rootPath, "cursor", DouYinApi::createUserCollectionUrl);
//    }

//    @Async
//    public void downloadSelf(String uid, File rootPath) {
//        download(uid, rootPath, "max_cursor", DouYinApi::createApiUserVideoListUrl);
//    }

    @Async
    public void downloadSelfBrowser(String shareUrl, File rootPath) {
        long cursor = 0L;
        boolean hasMore;
        Map<String, FileWriter> logFileWriterMap = new HashMap<>();
        List<WebRequest> ajaxPageRequests = new ArrayList<>();
        final WebClient webClient = new WebClient();
        webClient.setAjaxController(new CacheAjaxController(ajaxPageRequests));
        try {
            webClient.getPage(shareUrl);
        } catch (IOException e) {
            logger.error("模拟浏览器访问分析链接失败", e);
            messagingTemplate.convertAndSend("/topic/log", new LogMessage<>("模拟浏览器访问分析链接失败:" + e.getMessage()));
        }
        if (ajaxPageRequests.isEmpty()) {
            logger.warn("未得到获取用户视频列表ajax请求地址");
            messagingTemplate.convertAndSend("/topic/log", new LogMessage<>("未得到获取用户视频列表ajax请求地址"));
        }
        WebRequest h5UserVideoPageRequest = ajaxPageRequests.get(0);
        if (h5UserVideoPageRequest != null) {
            String defUrl = DouYinApi.getBaseH5VideoListUrl(h5UserVideoPageRequest.getUrl().toString());
            do {
                try {
                    String listUrl = defUrl + "&max_cursor=" + cursor;
                    logger.info("拉取视频列表:{}", listUrl);
                    h5UserVideoPageRequest.setUrl(new URL(listUrl));
                    UnexpectedPage jsonPage = webClient.getPage(h5UserVideoPageRequest);
                    JsonNode rootNode = DouYinWorker.OBJECT_MAPPER.readTree(jsonPage.getInputStream());
                    JsonNode cursorNode = rootNode.get("max_cursor");
                    JsonNode hasMoreNode = rootNode.get("has_more");
                    JsonNode awemeListNode = rootNode.get("aweme_list");
                    logger.info("cursorNode:{};hasMoreNode:{};awemeListSize:{}", cursorNode.longValue(), hasMoreNode.booleanValue(), awemeListNode.size());
                    disposeAwemeList(awemeListNode, rootPath, logFileWriterMap);
                    hasMore = hasMoreNode.booleanValue();
                    cursor = cursorNode.longValue();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                    break;
                }
            } while (hasMore);
            logger.info("下载完成");
            messagingTemplate.convertAndSend("/topic/log", new LogMessage<>("下载完成"));
        }
    }

//    private void download(String uid, File rootPath, String cursorNodeName, VideoListUrlCreater urlCreater) {
//        long cursor = 0L;
//        boolean hasMore;
//        Map<String, FileWriter> logFileWriterMap = new HashMap<>();
//        do {
//            try {
//                String listUrl = urlCreater.create(uid, cursor);
//                logger.info("拉取视频列表:{}", listUrl);
//                HttpRequest request = douYinWorker.createAppApiRequest(listUrl);
//                HttpResponse response = request.execute();
//                JsonNode rootNode = DouYinWorker.OBJECT_MAPPER.readTree(response.body());
//                JsonNode cursorNode = rootNode.get(cursorNodeName);
//                JsonNode hasMoreNode = rootNode.get("has_more");
//                JsonNode awemeListNode = rootNode.get("aweme_list");
//                logger.info("cursorNode:{};hasMoreNode:{};awemeListSize:{}", cursorNode.longValue(), hasMoreNode.intValue(), awemeListNode.size());
//                disposeAwemeList(awemeListNode, rootPath, logFileWriterMap);
//                hasMore = (hasMoreNode.intValue() == 1);
//                cursor = cursorNode.longValue();
//            } catch (Exception e) {
//                logger.error(e.getMessage(), e);
//                break;
//            }
//        } while (hasMore);
//        logger.info("下载完成");
//        messagingTemplate.convertAndSend("/topic/log", new LogMessage<>("下载完成"));
//    }

    private void disposeAwemeList(JsonNode awemeListNode, File rootPath, Map<String, FileWriter> logFileWriterMap) throws JsonProcessingException {
        for (JsonNode awemeNode : awemeListNode) {
            DyVideo video = douYinWorker.createDyVideoByAwemeItem(awemeNode);
            if (video != null) {
                DyLocalVideo localVideo = douYinWorker.download(video, rootPath);
                if (localVideo != null) {
                    FileWriter logFileWriter = logFileWriterMap.get(localVideo.getProfilePath().toString());
                    if (logFileWriter == null) {
                        String logFileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".log.json";
                        logFileWriter = new FileWriter(new File(localVideo.getProfilePath(), logFileName), StandardCharsets.UTF_8);
                        logFileWriterMap.put(localVideo.getProfilePath().toString(), logFileWriter);
                    }
                    logFileWriter.appendLines(Collections.singletonList(DouYinWorker.OBJECT_MAPPER.writeValueAsString(localVideo)));
                    messagingTemplate.convertAndSend("/topic/log", new LogMessage<>(localVideo));
                }
            }
        }
    }

//    private void disposeAwemeList(JsonNode awemeListNode, File rootPath, Map<String, FileWriter> logFileWriterMap) {
//        final WebClient webClient = new WebClient();
//        for (JsonNode awemeItem : awemeListNode) {
//            boolean isDel = false;
//            JsonNode statusNode = awemeItem.get("status");
//            if (statusNode != null) {
//                JsonNode isDeleteNode = statusNode.get("is_delete");
//                if (isDeleteNode != null) {
//                    isDel = isDeleteNode.booleanValue();
//                }
//            }
//            if (!isDel) {
//                try {
//                    String awemeId = awemeItem.get("aweme_id").textValue();
//                    String shareUrl = awemeItem.get("share_url").textValue();
//                    JsonNode videoNode = awemeItem.get("video");
//                    JsonNode downloadNode = videoNode.get("download_addr");
//                    String videoId = downloadNode.get("uri").textValue();
//                    JsonNode authorNode = awemeItem.get("author");
//
//                    if (authorNode != null && StrUtil.isNotBlank(awemeId) && StrUtil.isNotBlank(videoId) && StrUtil.isNotBlank(shareUrl)) {
//
//                        DyUser user = douYinWorker.createDyUserByVideoAuthorNode(authorNode);
//
//                        DyVideo video = new DyVideo(awemeId, videoId, shareUrl);
//
//                        video.setUser(user);
//
//                        DyLocalVideo localVideo = new DyLocalVideo();
//
//                        File profilePath = douYinWorker.createProfilePath(user, rootPath);
//                        File videoFile = new File(profilePath, video.getVideoId() + ".mp4");
//
//                        localVideo.setVideo(video);
//                        localVideo.setRootPath(rootPath);
//                        localVideo.setProfilePath(profilePath);
//                        localVideo.setVideoFile(videoFile);
//
//                        if (videoFile.exists()) {
//                            logger.info("本地已存在视频: {}", videoFile.toURI());
//                            localVideo.setNew(false);
//                        } else {
//
//                            HtmlPage videoSharePage = webClient.getPage(shareUrl);
//
//                            HtmlDivision playDiv = videoSharePage.querySelector("#pageletReflowVideo > div > div.video-box.fl > div > div");
//
//                            if (playDiv != null) {
//
//                                HtmlPage changedPage = playDiv.click();
//
//                                Thread.sleep(500L);
//
//                                HtmlVideo videoHtmlNode = changedPage.querySelector("#pageletReflowVideo > div > div.video-box.fl > div > video");
//                                if (videoHtmlNode == null) {
//                                    Console.log("视频不存在");
//                                    return;
//                                }
//                                String v = videoHtmlNode.getAttribute("src");
//                                Console.log("视频播放地址:{}", StrUtil.trim(v));
//                                String vinfo = HttpUtil.get(v);
//                                Console.log("视频文件地址:{}", StrUtil.trim(vinfo));
//                                Matcher matcher = pattern.matcher(vinfo);
//
//                                if (matcher.find()) {
//                                    String realFileUrl = matcher.group(1);
//                                    logger.debug("得到真实下载地址:{}", realFileUrl);
//                                    HttpUtil.downloadFile(realFileUrl, videoFile);
//                                    logger.info("下载完成:{}({})", videoFile.toURI(), FileUtil.readableFileSize(videoFile));
//                                    if (StrUtil.startWithIgnoreCase(realFileUrl, "http://")) {
//                                        realFileUrl = StrUtil.replaceIgnoreCase(realFileUrl, "http://", "https://");
//                                    }
//                                    video.setRealFileUrl(realFileUrl);
//                                    localVideo.setNew(true);
//                                }
//                            } else {
//                                logger.warn("未找到播放按钮");
//                            }
//                        }
//
//                        FileWriter logFileWriter = logFileWriterMap.get(localVideo.getProfilePath().toString());
//                        if (logFileWriter == null) {
//                            String logFileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".log.json";
//                            logFileWriter = new FileWriter(new File(localVideo.getProfilePath(), logFileName), StandardCharsets.UTF_8);
//                            logFileWriterMap.put(localVideo.getProfilePath().toString(), logFileWriter);
//                        }
//                        logFileWriter.appendLines(Collections.singletonList(DouYinWorker.OBJECT_MAPPER.writeValueAsString(localVideo)));
//                        messagingTemplate.convertAndSend("/topic/log", new LogMessage<>(localVideo));
//                    }
//
//                    logger.info("视频信息不完整:{}", awemeItem.toString());
//                } catch (Exception e) {
//                    logger.error("解析视频信息失败:{}", awemeItem.toString(), e);
//                }
//            } else {
//                logger.info("视频已删除:{}", awemeItem.toString());
//            }
//        }
//    }
}
