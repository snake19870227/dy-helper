package com.github.douyin.service;

import cn.hutool.core.io.file.FileWriter;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.gargoylesoftware.htmlunit.UnexpectedPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.github.douyin.client.CacheAjaxController;
import com.github.douyin.client.DouYinApi;
import com.github.douyin.client.DouYinWorker;
import com.github.douyin.common.VideoListUrlCreater;
import com.github.douyin.entity.DyLocalVideo;
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

/**
 * @author Bu HuaYang
 */
@Service
public class ApiService {

    private static final Logger logger = LoggerFactory.getLogger(ApiService.class);

    private final SimpMessagingTemplate messagingTemplate;

    private final DouYinWorker douYinWorker;

    public ApiService(SimpMessagingTemplate messagingTemplate, DouYinWorker douYinWorker) {
        this.messagingTemplate = messagingTemplate;
        this.douYinWorker = douYinWorker;
    }

    @Async
    public void downloadCollection(String uid, File rootPath) {
        download(uid, rootPath, "cursor", DouYinApi::createUserCollectionUrl);
    }

    @Async
    public void downloadSelf(String uid, File rootPath) {
        download(uid, rootPath, "max_cursor", DouYinApi::createApiUserVideoListUrl);
    }

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

    private void download(String uid, File rootPath, String cursorNodeName, VideoListUrlCreater urlCreater) {
        long cursor = 0L;
        boolean hasMore;
        Map<String, FileWriter> logFileWriterMap = new HashMap<>();
        do {
            try {
                String listUrl = urlCreater.create(uid, cursor);
                logger.info("拉取视频列表:{}", listUrl);
                HttpRequest request = douYinWorker.createAppApiRequest(listUrl);
                HttpResponse response = request.execute();
                JsonNode rootNode = DouYinWorker.OBJECT_MAPPER.readTree(response.body());
                JsonNode cursorNode = rootNode.get(cursorNodeName);
                JsonNode hasMoreNode = rootNode.get("has_more");
                JsonNode awemeListNode = rootNode.get("aweme_list");
                logger.info("cursorNode:{};hasMoreNode:{};awemeListSize:{}", cursorNode.longValue(), hasMoreNode.intValue(), awemeListNode.size());
                disposeAwemeList(awemeListNode, rootPath, logFileWriterMap);
                hasMore = (hasMoreNode.intValue() == 1);
                cursor = cursorNode.longValue();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                break;
            }
        } while (hasMore);
        logger.info("下载完成");
        messagingTemplate.convertAndSend("/topic/log", new LogMessage<>("下载完成"));
    }

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
}
