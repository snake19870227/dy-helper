package com.github.douyin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * @author Bu HuaYang
 */
@ConfigurationProperties(prefix = "douyin.download.api")
public class DouYinApiProperties {

    private String ttToken;
    private String ttTraceId;
    private String gorgon;
    private String ssDp;
    private String userAgent;

    private Map<String, String> cookies;

    private String cookieStr;

    public String getTtToken() {
        return ttToken;
    }

    public void setTtToken(String ttToken) {
        this.ttToken = ttToken;
    }

    public String getTtTraceId() {
        return ttTraceId;
    }

    public void setTtTraceId(String ttTraceId) {
        this.ttTraceId = ttTraceId;
    }

    public String getGorgon() {
        return gorgon;
    }

    public void setGorgon(String gorgon) {
        this.gorgon = gorgon;
    }

    public String getSsDp() {
        return ssDp;
    }

    public void setSsDp(String ssDp) {
        this.ssDp = ssDp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
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
