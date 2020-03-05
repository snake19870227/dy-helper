package com.github.douyin.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.File;

/**
 * @author Bu HuaYang
 */
@Document(indexName = DyUser.DY_USER_INDEX, type = "_doc", createIndex = false)
public class DyUser {

    public static final String DY_USER_INDEX = "dy_user";

    @Id
    @Field(type = FieldType.Keyword)
    private String uid;

    @Field(type = FieldType.Keyword)
    private String secUid;

    @Field(type = FieldType.Keyword)
    private String shortId;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String nickname;

    @Field(type = FieldType.Keyword)
    private String uniqueId;

    @Field(type = FieldType.Keyword)
    private String shareUrl;

    @Field(type = FieldType.Keyword)
    private String headImageUrl;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String signature;

    @Field(type = FieldType.Keyword)
    private File profilePath;

    public DyUser() {
    }

    public DyUser(String uid) {
        this.uid = uid;
    }

    public DyUser(String uid, String shortId, String nickname) {
        this.uid = uid;
        this.shortId = shortId;
        this.nickname = nickname;
    }

    public DyUser(String secUid, String uid, String shortId, String nickname) {
        this.secUid = secUid;
        this.uid = uid;
        this.shortId = shortId;
        this.nickname = nickname;
    }

    public DyUser(String secUid, String uid, String shortId, String nickname, String uniqueId) {
        this.secUid = secUid;
        this.uid = uid;
        this.shortId = shortId;
        this.nickname = nickname;
        this.uniqueId = uniqueId;
    }

    public DyUser(String secUid, String uid, String shortId, String nickname, String uniqueId, String headImageUrl, String signature) {
        this.secUid = secUid;
        this.uid = uid;
        this.shortId = shortId;
        this.nickname = nickname;
        this.uniqueId = uniqueId;
        this.headImageUrl = headImageUrl;
        this.signature = signature;
    }

    @Override
    public String toString() {
        return "DyUser{" +
                "secUid='" + secUid + '\'' +
                ", uid='" + uid + '\'' +
                ", shortId='" + shortId + '\'' +
                ", nickname='" + nickname + '\'' +
                ", uniqueId='" + uniqueId + '\'' +
                ", shareUrl='" + shareUrl + '\'' +
                ", headImageUrl='" + headImageUrl + '\'' +
                ", signature='" + signature + '\'' +
                ", profilePath=" + profilePath +
                '}';
    }

    public String getSecUid() {
        return secUid;
    }

    public void setSecUid(String secUid) {
        this.secUid = secUid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getShortId() {
        return shortId;
    }

    public void setShortId(String shortId) {
        this.shortId = shortId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getShareUrl() {
        return shareUrl;
    }

    public void setShareUrl(String shareUrl) {
        this.shareUrl = shareUrl;
    }

    public String getHeadImageUrl() {
        return headImageUrl;
    }

    public void setHeadImageUrl(String headImageUrl) {
        this.headImageUrl = headImageUrl;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public File getProfilePath() {
        return profilePath;
    }

    public void setProfilePath(File profilePath) {
        this.profilePath = profilePath;
    }
}
