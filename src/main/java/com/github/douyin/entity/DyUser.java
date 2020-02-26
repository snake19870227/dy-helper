package com.github.douyin.entity;

/**
 * @author Bu HuaYang
 */
public class DyUser {

    private String secUid;

    private String uid;

    private String shortId;

    private String nickname;

    private String shareUrl;

    public DyUser() {
    }

    public DyUser(String secUid, String uid, String shareUrl) {
        this.secUid = secUid;
        this.uid = uid;
        this.shareUrl = shareUrl;
    }

    public DyUser(String secUid, String uid, String shortId, String nickname) {
        this.secUid = secUid;
        this.uid = uid;
        this.shortId = shortId;
        this.nickname = nickname;
    }

    public DyUser(String secUid, String uid, String shortId, String nickname, String shareUrl) {
        this.secUid = secUid;
        this.uid = uid;
        this.shortId = shortId;
        this.nickname = nickname;
        this.shareUrl = shareUrl;
    }

    @Override
    public String toString() {
        return "DyUser{" +
                "secUid='" + secUid + '\'' +
                ", uid='" + uid + '\'' +
                ", shortId='" + shortId + '\'' +
                ", nickname='" + nickname + '\'' +
                ", shareUrl='" + shareUrl + '\'' +
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

    public String getShareUrl() {
        return shareUrl;
    }

    public void setShareUrl(String shareUrl) {
        this.shareUrl = shareUrl;
    }
}
