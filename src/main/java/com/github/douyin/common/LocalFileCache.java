package com.github.douyin.common;

import com.github.douyin.entity.DyUser;

import java.util.List;

/**
 * @author Bu HuaYang
 */
public interface LocalFileCache {

    void init();

    List<DyUser> search(String searchText);
}
