package com.github.douyin.client;

import cn.hutool.core.io.file.FileWriter;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.emoji.EmojiUtil;
import cn.hutool.http.HttpUtil;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.douyin.entity.DyUser;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * @author Bu HuaYang
 */
public class DyApi {

    public static ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
    }

    public static String buildUserShareUrl(String uid) {
        return "https://www.iesdouyin.com/share/user/" + uid;
    }

    public static DyUser buildUserByAwemeNode(JsonNode authorNode) {
        JsonNode uniqueId = authorNode.get("unique_id");
        JsonNode nickname = authorNode.get("nickname");
        JsonNode shortId = authorNode.get("short_id");
        JsonNode signature = authorNode.get("signature");
        JsonNode avatarLarger = authorNode.get("avatar_larger");
        JsonNode uid = authorNode.get("uid");
        JsonNode secUid = authorNode.get("sec_uid");

        if (uid != null && StrUtil.isNotBlank(uid.textValue())) {
            DyUser user = new DyUser(uid.textValue());
            if (uniqueId != null && StrUtil.isNotBlank(uniqueId.textValue())) {
                user.setUniqueId(uniqueId.textValue());
            }
            if (nickname != null && StrUtil.isNotBlank(nickname.textValue())) {
                String n = nickname.textValue();
                n = EmojiUtil.containsEmoji(n) ? EmojiUtil.removeAllEmojis(n) : n;
                user.setNickname(n);
            }
            if (shortId != null && StrUtil.isNotBlank(shortId.textValue())) {
                user.setShortId(shortId.textValue());
            }
            if (signature != null && StrUtil.isNotBlank(signature.textValue())) {
                String s = signature.textValue();
                s = EmojiUtil.containsEmoji(s) ? EmojiUtil.removeAllEmojis(s) : s;
                user.setSignature(s);
            }
            if (secUid != null && StrUtil.isNotBlank(secUid.textValue())) {
                user.setSecUid(secUid.textValue());
            }
            if (avatarLarger != null && avatarLarger.get("url_list") != null && !avatarLarger.get("url_list").isEmpty()) {
                user.setHeadImageUrl(avatarLarger.get("url_list").get(0).textValue());
            }
            return user;
        } else {
            throw new RuntimeException("author节点信息不完整:" + authorNode.toString());
        }
    }

    public static void createProfileFile(DyUser user, File rootFile, boolean isPersistence) {
        String profileName = user.getShortId() + "-" + user.getUid();
        File profilePath = new File(rootFile, profileName);
        user.setProfilePath(profilePath);
        if (isPersistence) {

            if (!profilePath.exists()) {
                if (!profilePath.mkdir()) {
                    throw new RuntimeException("创建本地文件夹失败:" + user);
                }
            }

            File userFile = new File(user.getProfilePath(), "user.json");
            if (!userFile.exists()) {
                FileWriter userFileWriter = new FileWriter(userFile, StandardCharsets.UTF_8);
                try {
                    userFileWriter.write(objectMapper.writeValueAsString(user));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("写入user.json失败:" + user);
                }
            }

            File headImage = new File(user.getProfilePath(), "head.jpg");
            if (StrUtil.isNotBlank(user.getHeadImageUrl()) && !headImage.exists()) {
                try {
                    HttpUtil.downloadFile(user.getHeadImageUrl(), headImage);
                } catch (Exception e) {
                    throw new RuntimeException("下载head.jpg失败:" + user);
                }
            }
        }
    }
}
