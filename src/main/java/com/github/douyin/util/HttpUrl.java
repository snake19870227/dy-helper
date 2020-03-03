package com.github.douyin.util;

import cn.hutool.core.util.StrUtil;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Bu HuaYang
 */
public class HttpUrl {

    private URL url;

    private Map<String, String> queryMap;

    public HttpUrl(String u) throws MalformedURLException {
        this.url = new URL(u);
        this.queryMap = new LinkedHashMap<>();
        String queryStr = this.url.getQuery();
        if (StrUtil.isNotBlank(queryStr)) {
            String[] querys = StrUtil.split(queryStr, "&");
            Stream.of(querys).forEach(s -> {
                String[] kv = StrUtil.split(s, "=");
                queryMap.put(kv[0], kv[1]);
            });
        }
    }

    @Override
    public String toString() {
        return url.toString();
    }

    public URI toUri() throws URISyntaxException {
        return url.toURI();
    }

    public String getPath() {
        return url.getPath();
    }

    public int getPort() {
        return url.getPort();
    }

    public String getProtocol() {
        return url.getProtocol();
    }

    public String getHost() {
        return url.getHost();
    }

    public String getParam(String paramName) {
        return queryMap.get(paramName);
    }

}
