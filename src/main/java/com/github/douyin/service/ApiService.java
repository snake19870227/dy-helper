package com.github.douyin.service;

import cn.hutool.core.io.file.FileWriter;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.fasterxml.jackson.databind.JsonNode;
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
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
                JsonNode collectionNode = DouYinWorker.OBJECT_MAPPER.readTree(response.body());
                JsonNode cursorNode = collectionNode.get(cursorNodeName);
                JsonNode hasMoreNode = collectionNode.get("has_more");
                JsonNode awemeListNode = collectionNode.get("aweme_list");
                logger.info("cursorNode:{};hasMoreNode:{};awemeListSize:{}", cursorNode.longValue(), hasMoreNode.intValue(), awemeListNode.size());
                for (JsonNode awemeNode : awemeListNode) {
                    DyVideo video = douYinWorker.createDyVideoByAwemeItem(awemeNode);
                    if (video != null) {
                        DyLocalVideo localVideo = douYinWorker.download(video, rootPath);
                        if (localVideo != null) {
                            FileWriter logFileWriter = logFileWriterMap.get(localVideo.getProfilePath().toString());
                            if (logFileWriter == null) {
                                String logFileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".log";
                                logFileWriter = new FileWriter(new File(localVideo.getProfilePath(), logFileName), StandardCharsets.UTF_8);
                                logFileWriterMap.put(localVideo.getProfilePath().toString(), logFileWriter);
                            }
                            logFileWriter.appendLines(Collections.singletonList(DouYinWorker.OBJECT_MAPPER.writeValueAsString(localVideo)));
                            messagingTemplate.convertAndSend("/topic/log", new LogMessage<>(localVideo));
                        }
                    }
                }
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
}
