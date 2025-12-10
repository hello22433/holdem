package com.sparta.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 클라이언트가 연결할 엔드포인트: ws://localhost:8080/ws-holdem
        registry.addEndpoint("/ws-holdem")
                .setAllowedOriginPatterns("*")
                .withSockJS();                  // SockJS 활성화 (필수!);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 메시지 받을 경로 (구독)
        registry.enableSimpleBroker("/topic");
        // 메시지 보낼 경로 (발행)
        registry.setApplicationDestinationPrefixes("/app");
    }
}
