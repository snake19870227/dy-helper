package com.github.douyin.config;

import com.gargoylesoftware.htmlunit.WebClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * @author Bu HuaYang
 */
@Configuration
@EnableWebSocketMessageBroker
//@EnableConfigurationProperties(DouYinApiProperties.class)
public class ProjectConfig {

    @Configuration
    public static class WebSocketStompConfig implements WebSocketMessageBrokerConfigurer {

        @Override
        public void registerStompEndpoints(StompEndpointRegistry registry) {
            registry.addEndpoint("/websocket").setAllowedOrigins("*").withSockJS();
        }

        @Override
        public void configureMessageBroker(MessageBrokerRegistry registry) {
            registry.enableSimpleBroker("/user", "/topic");
            registry.setApplicationDestinationPrefixes("/ws");
            registry.setUserDestinationPrefix("/user");
        }

    }
}
