package com.github.douyin.entity;

import java.util.List;

/**
 * @author Bu HuaYang
 */
public class DyVideo {

    private String awemeId;

    private String videoId;

    private String shareUrl;

    private List<String> downloadUrls;

    private String realFileUrl;

    private DyUser user;

    public DyVideo() {
    }

    public DyVideo(String awemeId, String videoId) {
        this.awemeId = awemeId;
        this.videoId = videoId;
    }

    @Override
    public String toString() {
        return "DyVideo{" +
                "awemeId='" + awemeId + '\'' +
                ", videoId='" + videoId + '\'' +
                ", shareUrl='" + shareUrl + '\'' +
                ", downloadUrls=" + downloadUrls +
                ", realFileUrl='" + realFileUrl + '\'' +
                '}';
    }

    public String getAwemeId() {
        return awemeId;
    }

    public void setAwemeId(String awemeId) {
        this.awemeId = awemeId;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getShareUrl() {
        return shareUrl;
    }

    public void setShareUrl(String shareUrl) {
        this.shareUrl = shareUrl;
    }

    public List<String> getDownloadUrls() {
        return downloadUrls;
    }

    public void setDownloadUrls(List<String> downloadUrls) {
        this.downloadUrls = downloadUrls;
    }

    public String getRealFileUrl() {
        return realFileUrl;
    }

    public void setRealFileUrl(String realFileUrl) {
        this.realFileUrl = realFileUrl;
    }

    public DyUser getUser() {
        return user;
    }

    public void setUser(DyUser user) {
        this.user = user;
    }
}