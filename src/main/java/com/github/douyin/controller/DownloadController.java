package com.github.douyin.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.emoji.EmojiUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import com.github.douyin.client.DouYinApi;
import com.github.douyin.client.DouYinWorker;
import com.github.douyin.config.DouYinApiProperties;
import com.github.douyin.entity.DyLocalVideo;
import com.github.douyin.entity.DyUser;
import com.github.douyin.entity.DyVideo;
import com.github.douyin.service.ApiService;
import com.github.douyin.util.HttpUrl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Bu HuaYang
 */
@RestController
public class DownloadController {

    private static final Logger logger = LoggerFactory.getLogger(DownloadController.class);

    public static final Pattern pattern = Pattern.compile("href=\"(.*?)\">");

    @Value("${douyin.download.local-path}")
    private String downloadLocalPath;

    private final DouYinApiProperties douYinApiProperties;

    private final ApiService apiService;

    private final DouYinWorker douYinWorker;

    public DownloadController(DouYinApiProperties douYinApiProperties, ApiService apiService, DouYinWorker douYinWorker) {
        this.douYinApiProperties = douYinApiProperties;
        this.apiService = apiService;
        this.douYinWorker = douYinWorker;
    }

    @GetMapping(path = "/download/user/{uid}")
    public String downloadUser(@PathVariable(name = "uid") String uid) {
        String shareUrl = DouYinApi.createShareUrlByUid(uid);
        apiService.downloadSelfBrowser(shareUrl, new File(downloadLocalPath));
        return "Downloaded";
    }

    @PostMapping(path = "/download")
    public String download(@RequestParam(name = "shareUrl") String shareUrl,
                           @RequestParam(name = "downloadType") String downloadType) {
//        DyUser user = douYinWorker.getUserByShareUrl(shareUrl);
        String uid = douYinWorker.getUserIdByShareUrl(shareUrl);
        if (StrUtil.isNotBlank(uid)) {
            if (StrUtil.equals("1", downloadType)) {
//                apiService.downloadSelf(user.getUid(), new File(downloadLocalPath));
                apiService.downloadSelfBrowser(shareUrl, new File(downloadLocalPath));
            }
//            if (StrUtil.equals("2", downloadType)) {
//                apiService.downloadCollection(uid, new File(downloadLocalPath));
//            }
        }
        return "Downloaded";
    }

    @GetMapping(path = "/download/video")
    public DyLocalVideo download(@RequestParam(name = "url") String url,
                                 @RequestParam(name = "videoId") String videoId,
                                 @RequestParam(name = "uid") String uid,
                                 @RequestParam(name = "shortId") String shortId,
                                 @RequestParam(name = "secUid") String secUid,
                                 @RequestParam(name = "uniqueId") String uniqueId,
                                 @RequestParam(name = "nickname") String nickname) {
        try {
            File rootPath = new File(downloadLocalPath);

            nickname = EmojiUtil.containsEmoji(nickname) ? EmojiUtil.removeAllEmojis(nickname) : nickname;
            DyUser user = new DyUser(secUid, uid, shortId, nickname, uniqueId);
            File profilePath = douYinWorker.createProfilePath(user, rootPath);

            DyVideo video = new DyVideo(videoId, Stream.of(url).collect(Collectors.toList()));
            video.setUser(user);
            File videoFile = new File(profilePath, video.getVideoId() + ".mp4");

            DyLocalVideo localVideo = new DyLocalVideo();
            localVideo.setVideo(video);
            localVideo.setRootPath(rootPath);
            localVideo.setProfilePath(profilePath);
            localVideo.setVideoFile(videoFile);

            if (videoFile.exists()) {
                logger.info("本地已存在视频: {}", videoFile.toURI());
                localVideo.setNew(false);
                return localVideo;
            }

            url = StrUtil.replace(url, "&watermark=1", "");
            HttpRequest request = HttpUtil.createGet(url);
            request.header("User-Agent", "Aweme");
            String resultStr = request.execute().body();
            Matcher matcher = pattern.matcher(resultStr);
            if (matcher.find()) {
                String realFileUrl = matcher.group(1);
                logger.debug("得到真实下载地址:{}", realFileUrl);
                HttpUtil.downloadFile(realFileUrl, videoFile);
                logger.info("下载完成:{}({})", videoFile.toURI(), FileUtil.readableFileSize(videoFile));
                if (StrUtil.startWithIgnoreCase(realFileUrl, "http://")) {
                    realFileUrl = StrUtil.replaceIgnoreCase(realFileUrl, "http://", "https://");
                }
                video.setRealFileUrl(realFileUrl);
                localVideo.setNew(true);
                String logFileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".log.json";
                FileWriter logFileWriter = new FileWriter(new File(localVideo.getProfilePath(), logFileName), StandardCharsets.UTF_8);
                logFileWriter.appendLines(Collections.singletonList(DouYinWorker.OBJECT_MAPPER.writeValueAsString(localVideo)));
                return localVideo;
            }
        } catch (Exception e) {
            logger.error("下载失败:{}", url, e);
        }
        return null;
    }

    @GetMapping(path = "/play")
    public String play(@RequestParam(name = "url") String url) {
        url = StrUtil.replace(url, "&watermark=1", "");
        HttpRequest request = HttpUtil.createGet(url);
        request.header("User-Agent", "Aweme");
        String resultStr = request.execute().body();
        Matcher matcher = pattern.matcher(resultStr);
        if (matcher.find()) {
            String realFileUrl = matcher.group(1);
            if (StrUtil.startWithIgnoreCase(realFileUrl, "http://")) {
                realFileUrl = StrUtil.replaceIgnoreCase(realFileUrl, "http://", "https://");
            }
            return realFileUrl;
        }
        return null;
    }
}
