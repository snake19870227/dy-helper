package com.github.douyin;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.gargoylesoftware.htmlunit.UnexpectedPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.github.douyin.client.CacheAjaxController;
import com.github.douyin.client.DyApi;
import com.github.douyin.dao.DyUserRepository;
import com.github.douyin.entity.DyUser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Bu HuaYang
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ReloadLocal {

    private static final Logger logger = LoggerFactory.getLogger(ReloadLocal.class);

    @Value("${douyin.download.local-path}")
    private String downloadLocalPath;

    @Autowired
    private DyUserRepository repository;

    @Test
    public void reload() {
        File rootFile = new File(downloadLocalPath);
        File[] userFiles = rootFile.listFiles();
        if (rootFile.exists() && userFiles != null) {

            logger.info("共{}个", userFiles.length);

            int num = 0;

            List<WebRequest> ajaxRequestList = new ArrayList<>();
            CacheAjaxController cacheAjaxController = new CacheAjaxController(ajaxRequestList);
            WebClient client = new WebClient();
            client.setAjaxController(cacheAjaxController);

            for (File userFile : userFiles) {

                num++;

                logger.info("========================< 第{}个目录 >========================", num);

                String fileName = userFile.getName();
                String[] infos = StrUtil.split(userFile.getName(), "-");

                if (infos == null || infos.length != 2) {
                    logger.warn("本地目录名无效:{}", fileName);
                    continue;
                }

                String uid = infos[1];

                Optional<DyUser> dyUser = repository.findById(uid);

                if (dyUser.isPresent()) {
                    logger.info("elastic中已存在用户记录:{}", fileName);
                    continue;
                }

                String shareUrl = DyApi.buildUserShareUrl(uid);
                logger.info("准备请求:{}", shareUrl);
                try {
                    client.getPage(shareUrl);
                    Thread.sleep(RandomUtil.randomLong(500L, 3000L));
                } catch (Exception e) {
                    logger.error(fileName, e);
                }
                if (!ajaxRequestList.isEmpty()) {
                    WebRequest ajaxRequest = ajaxRequestList.get(0);
                    try {
                        logger.info("准备请求:{}", ajaxRequest.getUrl());
                        UnexpectedPage jsonPage = client.getPage(ajaxRequest);
                        JsonNode rootNode = DyApi.objectMapper.readTree(jsonPage.getInputStream());
                        JsonNode awemeList = rootNode.get("aweme_list");
                        if (awemeList != null && !awemeList.isEmpty()) {
                            JsonNode awemeNode = awemeList.get(0);
                            JsonNode authorNode = awemeNode.get("author");
                            if (authorNode != null) {
                                DyUser user = DyApi.buildUserByAwemeNode(authorNode);
                                DyApi.createProfileFile(user, rootFile, true);
                                repository.save(user);
                            } else {
                                logger.warn("没找到author节点:{}", awemeNode);
                            }
                        }
                    } catch (IOException e) {
                        logger.error(ajaxRequest.getUrl().toString(), e);
                    }
                    ajaxRequestList.clear();
                }
            }
        }
    }
}
