package com.github.douyin.common;

import com.github.douyin.entity.DyUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.stereotype.Component;

/**
 * @author Bu HuaYang
 */
@Component
public class ProjectInit implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(ProjectInit.class);

    private final ElasticsearchRestTemplate template;

    public ProjectInit(ElasticsearchRestTemplate template) {
        this.template = template;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        boolean isExists = template.indexExists(DyUser.DY_USER_INDEX);
        if (!isExists) {
            logger.info("未找到elastic索引 {} ，开始创建...", DyUser.DY_USER_INDEX);
            template.createIndex(DyUser.class);
            template.putMapping(DyUser.class);
            logger.info("elastic索引 {} 创建成功", DyUser.DY_USER_INDEX);
        }
    }
}
