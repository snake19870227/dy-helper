package com.github.douyin.entity;

import java.io.File;

/**
 * @author Bu HuaYang
 */
public class DyLocalVideo {

    private boolean isNew;

    private DyVideo video;

    private File rootPath;

    private File profilePath;

    private File videoFile;

    private String localUrl;

    @Override
    public String toString() {
        return "DyLocalVideo{" +
                "isNew=" + isNew +
                ", video=" + video +
                ", rootPath=" + rootPath +
                ", profilePath=" + profilePath +
                ", videoFile=" + videoFile +
                ", localUrl='" + localUrl + '\'' +
                '}';
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    public DyVideo getVideo() {
        return video;
    }

    public void setVideo(DyVideo video) {
        this.video = video;
    }

    public File getRootPath() {
        return rootPath;
    }

    public void setRootPath(File rootPath) {
        this.rootPath = rootPath;
    }

    public File getProfilePath() {
        return profilePath;
    }

    public void setProfilePath(File profilePath) {
        this.profilePath = profilePath;
    }

    public File getVideoFile() {
        return videoFile;
    }

    public void setVideoFile(File videoFile) {
        this.videoFile = videoFile;
    }

    public String getLocalUrl() {
        return localUrl;
    }

    public void setLocalUrl(String localUrl) {
        this.localUrl = localUrl;
    }
}
