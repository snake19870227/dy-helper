package com.github.douyin.common;

/**
 * @author Bu HuaYang
 */
@FunctionalInterface
public interface VideoListUrlCreater {

    String create(String uid, long cursor);
}
