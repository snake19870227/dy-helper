package com.github.douyin.client;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;

import java.net.URL;

/**
 * @author Bu HuaYang
 */
public class DouYinApi {

    public static String createApiUserVideoListUrl(String uid, long cursor) {
        String url = "https://api3-normal-c-lf.amemv.com/aweme/v1/aweme/post/?version_code=9.8.1" +
                "&js_sdk_version=1.47.2.2" +
                "&app_name=aweme" +
                "&vid=B4E25DA6-0092-4486-BC10-609C29DBCBEF" +
                "&app_version=9.8.1" +
                "&device_id=48097485040" +
                "&channel=App%20Store" +
                "&mcc_mnc=46002" +
                "&aid=1128" +
                "&screen_width=640" +
                "&openudid=6270d5e73d08338a9782cccfd3aabe0901e78c2e" +
                "&cdid=06D07B4C-CE0E-4B04-BC38-DAF66F1F3C6A" +
                "&os_api=18" +
                "&ac=WIFI" +
                "&os_version=13.3.1" +
                "&device_platform=iphone" +
                "&build_number=98107" +
//                "&iid=103948220879" +
                "&device_type=iPhone8,4" +
                "&idfa=0DDAC365-7A5D-4E82-826E-B20356C44351";
        if (cursor == 0L) {
            url += "&min_cursor=" + cursor;
        }
        url += "&user_id=" + uid + "&count=21&max_cursor=" + cursor + "&source=0";
        return url;
    }

    public static String createUserCollectionUrl(String uid, long cursor) {
        return "https://api3-normal-c-lf.amemv.com/aweme/v1/aweme/listcollection/" +
                "?version_code=9.8.1" +
                "&js_sdk_version=1.47.2.2" +
                "&app_name=aweme" +
                "&vid=B4E25DA6-0092-4486-BC10-609C29DBCBEF" +
                "&app_version=9.8.1" +
                "&device_id=48097485040" +
                "&channel=App%20Store" +
                "&mcc_mnc=46002" +
                "&aid=1128" +
                "&screen_width=640" +
                "&openudid=6270d5e73d08338a9782cccfd3aabe0901e78c2e" +
                "&cdid=06D07B4C-CE0E-4B04-BC38-DAF66F1F3C6A" +
                "&os_api=18" +
                "&ac=WIFI" +
                "&os_version=13.3.1" +
                "&device_platform=iphone" +
                "&build_number=98107" +
                "&iid=" + uid +
                "&device_type=iPhone8,4" +
                "&idfa=0DDAC365-7A5D-4E82-826E-B20356C44351" +
                "&count=12" +
                "&cursor=" + cursor;
    }

    public static String createUserProfileUrl(String uid, String secUid) {
        String url = "https://api3-normal-c-lf.amemv.com/aweme/v1/user/profile/other/" +
                "?version_code=9.8.1" +
                "&js_sdk_version=1.47.2.2" +
                "&app_name=aweme" +
                "&vid=B4E25DA6-0092-4486-BC10-609C29DBCBEF" +
                "&app_version=9.8.1" +
                "&device_id=48097485040" +
                "&channel=App%20Store" +
                "&mcc_mnc=46002" +
                "&aid=1128" +
                "&screen_width=640" +
                "&openudid=6270d5e73d08338a9782cccfd3aabe0901e78c2e" +
                "&cdid=06D07B4C-CE0E-4B04-BC38-DAF66F1F3C6A" +
                "&os_api=18" +
                "&ac=WIFI" +
                "&os_version=13.3.1" +
                "&device_platform=iphone" +
                "&build_number=98107" +
                "&iid=103948220879" +
                "&device_type=iPhone8,4" +
                "&idfa=0DDAC365-7A5D-4E82-826E-B20356C44351" +
                "&user_id=" + uid +
                "&address_book_access=1";
        if (StrUtil.isNotBlank(secUid)) {
            url += "&sec_user_id=" + secUid;
        }
        return url;
    }

    public static String createVideoUrl(String videoId) {
        return "https://api3-normal-c-lf.amemv.com/aweme/v1/play/" +
                "?video_id=" + videoId +
                "&line=0" +
//                "&ratio=540p" +
//                "&watermark=1" +
                "&media_type=4" +
                "&vr_type=0" +
                "&improve_bitrate=0" +
                "&logo_name=aweme" +
                "&quality_type=11" +
                "&source=PackSourceEnum_PUBLISH" +
                "&version_code=9.8.1" +
                "&js_sdk_version=1.47.2.2" +
                "&app_name=aweme" +
                "&vid=B4E25DA6-0092-4486-BC10-609C29DBCBEF" +
                "&app_version=9.8.1" +
//                "&device_id=48097485040" +
                "&channel=App%20Store" +
                "&mcc_mnc=46002" +
                "&aid=1128" +
//                "&screen_width=640" +
                "&openudid=6270d5e73d08338a9782cccfd3aabe0901e78c2e" +
                "&cdid=06D07B4C-CE0E-4B04-BC38-DAF66F1F3C6A" +
                "&os_api=18" +
                "&ac=WIFI" +
                "&os_version=13.3.1" +
                "&device_platform=iphone" +
                "&build_number=98107&iid=103948220879" +
                "&device_type=iPhone8,4" +
                "&idfa=0DDAC365-7A5D-4E82-826E-B20356C44351";
    }

    public static String getBaseVideoListUrl(String urlStr) {
        URL url = URLUtil.url(urlStr);

        StringBuilder defaultUrl = new StringBuilder();
        defaultUrl.append(url.getProtocol())
                .append("://")
                .append(url.getHost())
                .append(url.getPath())
                .append("?");

        String queryStr = url.getQuery();
        String[] params = StrUtil.split(queryStr, "&");
        for (int i = 0; i < params.length; i++) {
            String paramStr = params[i];
            if (StrUtil.containsIgnoreCase(paramStr, "max_cursor")) {
                continue;
            }
            if (i > 0) {
                defaultUrl.append("&");
            }
            defaultUrl.append(paramStr);
        }
        return defaultUrl.toString();
    }
}
