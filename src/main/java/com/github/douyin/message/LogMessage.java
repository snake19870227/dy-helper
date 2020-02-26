package com.github.douyin.message;

import cn.hutool.core.util.StrUtil;
import com.github.douyin.entity.DyLocalVideo;
import com.github.douyin.entity.DyUser;
import com.github.douyin.entity.DyVideo;

/**
 * @author Bu HuaYang
 */
public class LogMessage<T> {

    private enum LogMessageType {
        /**
         * 用户信息消息
         */
        User,
        /**
         * 远程视频信息消息
         */
        Video,
        /**
         * 本地视频信息消息
         */
        LocalVideo
    }

    public LogMessage(T data) {
        if (data instanceof DyUser) {
            this.type = LogMessageType.User.name();
        }
        if (data instanceof DyVideo) {
            this.type = LogMessageType.Video.name();
        }
        if (data instanceof DyLocalVideo) {
            this.type = LogMessageType.LocalVideo.name();
        }
        if (StrUtil.isBlank(this.type)) {
            this.type = "unknown";
        }
        this.data = data;
    }

    private String type;

    private T data;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
