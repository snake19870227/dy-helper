package com.github.douyin.client;

import cn.hutool.core.util.StrUtil;
import com.gargoylesoftware.htmlunit.AjaxController;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Bu HuaYang
 */
public class CacheAjaxController extends AjaxController {

    private static final Logger logger = LoggerFactory.getLogger(CacheAjaxController.class);

    private List<WebRequest> ajaxRequestList;

    public CacheAjaxController(List<WebRequest> ajaxRequestList) {
        this.ajaxRequestList = ajaxRequestList;
    }

    @Override
    public boolean processSynchron(HtmlPage page, WebRequest request, boolean async) {
        if (StrUtil.equals("www.iesdouyin.com", request.getUrl().getHost())) {
            logger.debug("得到ajax请求:{}", request.getUrl());
            ajaxRequestList.add(request);
        }
        return super.processSynchron(page, request, async);
    }
}
