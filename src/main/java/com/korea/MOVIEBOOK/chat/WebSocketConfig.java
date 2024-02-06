package com.korea.MOVIEBOOK.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {
    //생성자 주입
    private final ChatHandler chatHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatHandler,"ws/chat").setAllowedOrigins("*");
    }  // "/ws/chat" 엔드포인트로 WebSocketHandler 객체를 등록
       // setAllowedOrigins("*")를 통해 모든 오리진에서의 접근을 허용
}