package com.github.douyin.client;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.emoji.EmojiUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.douyin.common.BaseStreamProgress;
import com.github.douyin.config.DouYinApiProperties;
import com.github.douyin.entity.DyLocalVideo;
import com.github.douyin.entity.DyUser;
import com.github.douyin.entity.DyVideo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Bu HuaYang
 */
@Component
public class DouYinWorker {

    private static final Logger logger = LoggerFactory.getLogger(DouYinWorker.class);

    private final DouYinApiProperties douYinApiProperties;

    public DouYinWorker(DouYinApiProperties douYinApiProperties) {
        this.douYinApiProperties = douYinApiProperties;
    }

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static final Pattern pattern = Pattern.compile("href=\"(.*?)\">");

//    public DyUser getUserByShareUrl(String shareUrlStr) {
//        try {
//            URL shareUrl = new URL(shareUrlStr);
//
//            String path = shareUrl.getPath();
//            String uid = StrUtil.sub(path, StrUtil.lastIndexOfIgnoreCase(path, "/") + 1, path.length());
//
//            String queryStr = shareUrl.getQuery();
//            String secUid = null;
//            for (String paramStr : StrUtil.split(queryStr, "&")) {
//                if (StrUtil.startWithIgnoreCase(paramStr, "sec_uid")) {
//                    secUid = StrUtil.split(paramStr, "=")[1];
//                }
//            }
//            if (StrUtil.isNotBlank(uid) && StrUtil.isNotBlank(secUid)) {
//                HttpRequest request = createAppApiRequest(DouYinApi.createUserProfileUrl(uid, secUid));
//                return createDyUserByProfileJson(request.execute().body());
//            }
//
//            logger.error("未能获取到用户信息:{}", shareUrlStr);
//        } catch (Exception e) {
//            logger.error("根据分享链接获取用户信息失败:{}", shareUrlStr, e);
//        }
//        return null;
//    }

    public String getUserIdByShareUrl(String shareUrlStr) {
        try {
            URL shareUrl = new URL(shareUrlStr);

            String path = shareUrl.getPath();
            return StrUtil.sub(path, StrUtil.lastIndexOfIgnoreCase(path, "/") + 1, path.length());
        } catch (Exception e) {
            logger.error("根据分享链接获取用户信息失败:{}", shareUrlStr, e);
        }
        return null;
    }

//    public DyUser getUser(String uid) {
//        try {
//            HttpRequest request = createAppApiRequest(DouYinApi.createUserProfileUrl(uid, null));
//            DyUser tmpUser = createDyUserByProfileJson(request.execute().body());
//            if (tmpUser != null) {
//                return getUserByShareUrl(tmpUser.getShareUrl());
//            }
//
//            logger.error("未能获取到用户信息:{}", uid);
//        } catch (Exception e) {
//            logger.error("获取用户信息[{}]失败", uid, e);
//        }
//        return null;
//    }

//    public DyUser getUser(String uid, String secUid) {
//        try {
//            HttpRequest request = createAppApiRequest(DouYinApi.createUserProfileUrl(uid, secUid));
//            return createDyUserByProfileJson(request.execute().body());
//        } catch (Exception e) {
//            logger.error("获取用户信息[{}]失败", uid, e);
//        }
//        return null;
//    }

//    public DyUser createDyUserByProfileJson(String json) throws Exception {
//        JsonNode userProfileNode = OBJECT_MAPPER.readTree(json);
//        JsonNode userNode = userProfileNode.get("user");
//        if (userNode != null) {
//            DyUser user = createDyUserByInfoNode(userNode);
//
//            String shareUrlStr = "https://" + userNode.get("share_info").get("share_url").textValue();
//
//            user.setShareUrl(shareUrlStr);
//
//            return user;
//        } else {
//            logger.error("未能从用户信息接口返回中获取用户信息:{}", json);
//        }
//        return null;
//    }

    public DyUser createDyUserByVideoAuthorNode(JsonNode authorNode) throws Exception {
        return createDyUserByInfoNode(authorNode);
    }

    public DyUser createDyUserByInfoNode(JsonNode infoNode) {
        String uid = infoNode.get("uid").textValue();
        String secUid = infoNode.get("sec_uid").textValue();
        String shortId = infoNode.get("short_id").textValue();

        String userNickName = infoNode.get("nickname").textValue();
        // 去除Emoji文本
        userNickName = EmojiUtil.containsEmoji(userNickName) ? EmojiUtil.removeAllEmojis(userNickName) : userNickName;

        return new DyUser(secUid, uid, shortId, userNickName);
    }

    public DyVideo createDyVideoByAwemeItem(JsonNode awemeItem) {
        boolean isDel = false;
        JsonNode statusNode = awemeItem.get("status");
        if (statusNode != null) {
            JsonNode isDeleteNode = statusNode.get("is_delete");
            if (isDeleteNode != null) {
                isDel = isDeleteNode.booleanValue();
            }
        }
        if (!isDel) {
            try {
                String awemeId = awemeItem.get("aweme_id").textValue();
                JsonNode videoNode = awemeItem.get("video");
                JsonNode downloadNode = videoNode.get("download_addr");
                String videoId = downloadNode.get("uri").textValue();
                JsonNode authorNode = awemeItem.get("author");

                if (authorNode != null && StrUtil.isNotBlank(awemeId) && StrUtil.isNotBlank(videoId)) {

                    DyUser user = createDyUserByVideoAuthorNode(authorNode);

                    DyVideo video = new DyVideo(awemeId, videoId);

                    video.setUser(user);

                    ArrayNode downloadUrlNode = (ArrayNode) downloadNode.get("url_list");
                    if (downloadUrlNode != null) {
                        List<String> downloadUrlList = new ArrayList<>(downloadUrlNode.size());
                        downloadUrlNode.forEach(jsonNode -> downloadUrlList.add(jsonNode.textValue()));
                        video.setDownloadUrls(downloadUrlList);
                    }

                    JsonNode shareUrlNode = awemeItem.get("share_url");
                    if (shareUrlNode != null) {
                        video.setShareUrl(shareUrlNode.textValue());
                    }

                    return video;
                }

                logger.info("视频信息不完整:{}", awemeItem.toString());
            } catch (Exception e) {
                logger.error("解析视频信息失败:{}", awemeItem.toString(), e);
            }
        } else {
            logger.info("视频已删除:{}", awemeItem.toString());
        }
        return null;
    }

    public DyLocalVideo download(DyVideo video, File rootPath) {
        DyLocalVideo localVideo = new DyLocalVideo();

        DyUser user = video.getUser();
        File profilePath = createProfilePath(user, rootPath);
        File videoFile = new File(profilePath, video.getVideoId() + ".mp4");

        localVideo.setVideo(video);
        localVideo.setRootPath(rootPath);
        localVideo.setProfilePath(profilePath);
        localVideo.setVideoFile(videoFile);

        if (videoFile.exists()) {
            logger.info("本地已存在视频: {}", videoFile.toURI());
            localVideo.setNew(false);
            return localVideo;
        }

        List<String> downloadUrlList = video.getDownloadUrls();
        if (downloadUrlList == null || downloadUrlList.isEmpty()) {
            downloadUrlList = Collections.singletonList(DouYinApi.createVideoUrl(video.getVideoId()));
        }

        for (String downloadUrl : downloadUrlList) {
            try {
                downloadUrl = StrUtil.replace(downloadUrl, "&watermark=1", "");
                logger.debug("尝试从[{}]下载", downloadUrl);
                HttpRequest request = HttpUtil.createGet(downloadUrl);
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
                    return localVideo;
                }
            } catch (Exception e) {
                logger.error("下载失败:{}", downloadUrl, e);
            }
        }
        return null;
    }

    public File createProfilePath(DyUser user, File rootPath) {
        String profileName = user.getShortId() + "-" + user.getNickname() + "-" + user.getUid();
        File profilePath = new File(rootPath, profileName);
        if (!profilePath.exists()) {
            profilePath.mkdir();
        }
        return profilePath;
    }

    public HttpRequest createAppApiRequest(String url) {
        HttpRequest request = HttpUtil.createGet(url);
        douYinApiProperties.getHeaders().forEach(request::header);
        request.header("x-khronos", String.valueOf(Instant.now().plusMillis(TimeUnit.HOURS.toMillis(8)).getEpochSecond()));
        request.cookie(douYinApiProperties.getCookieStr());
        return request;
    }

//    public static HttpRequest createAppApiRequest(String url) {
//        HttpRequest request = HttpUtil.createGet(url);
//        request.header("x-tt-token", "0064c1252a3714555a91731240b70527326ae1fb09740955a7fd3efb6d80ab15c87da313ca84529253dfb1078630fb7a9ea");
//        request.header("x-tt-trace-id", "00-7b3257e409b32d560f0a344377720468-7b3257e409b32d56-01");
//        request.header("x-khronos", String.valueOf(Instant.now().plusMillis(TimeUnit.HOURS.toMillis(8)).getEpochSecond()));
//        request.header("x-gorgon", "840220b300004b419e38b64e08505efb84f414bc44c827ec48d7");
//        request.header("x-ss-dp", "1128");
//        request.header("user-agent", "Aweme 9.8.1 rv:98107 (iPhone; iOS 13.3.1; zh_CN) Cronet");
//        request.cookie(
//                "msh=cxKAPdVbiNFELFZDmS-xEufwetU;" +
//                        "d_ticket=70c6f06b9ce9bf02a438d0fb7263e24034aa3;" +
//                        "sid_guard=64c1252a3714555a91731240b7052732%7C1582552506%7C5184000%7CFri%2C+24-Apr-2020+13%3A55%3A06+GMT;" +
//                        "uid_tt=dc1954f0f526c1d499b035e1f1062cce;" +
//                        "sid_tt=64c1252a3714555a91731240b7052732;" +
//                        "sessionid=64c1252a3714555a91731240b7052732;" +
//                        "odin_tt=8f09fbea196f14d6b0016a336593b5e6ddeb2811197e37ce96213d0a2267c431f43c3cf2834fda957849962d53de1988;" +
//                        "install_id=103948220879;" +
//                        "ttreq=1$17dc954255112a85407457c72a0805496af1ee8d"
//        );
//        return request;
//    }
}
