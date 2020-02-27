package com.github.douyin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * @author Bu HuaYang
 */
@ConfigurationProperties(prefix = "douyin.download.api")
public class DouYinApiProperties {

    private Map<String, String> headers;

    private Map<String, String> cookies;

    private String cookieStr;

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Map<String, String> getCookies() {
        return cookies;
    }

    public void setCookies(Map<String, String> cookies) {
        this.cookies = cookies;
        StringBuilder stringBuilder = new StringBuilder();
        cookies.forEach((s, s2) -> stringBuilder.append(s).append("=\"").append(s2).append('"').append(';'));
        this.cookieStr = stringBuilder.toString();
    }

    public String getCookieStr() {
        return cookieStr;
    }

    public void setCookieStr(String cookieStr) {
        this.cookieStr = cookieStr;
    }
}
