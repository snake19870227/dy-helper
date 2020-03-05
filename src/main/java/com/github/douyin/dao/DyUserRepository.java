package com.github.douyin.dao;

import com.github.douyin.entity.DyUser;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Bu HuaYang
 */
@Repository
public interface DyUserRepository extends ElasticsearchRepository<DyUser, String> {
}
