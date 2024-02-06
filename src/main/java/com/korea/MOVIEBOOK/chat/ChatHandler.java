package com.korea.MOVIEBOOK.chat;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class ChatHandler extends TextWebSocketHandler {
    private final List<WebSocketSession> sessions = new ArrayList<>();
    // WebSocket 세션을 저장하기 위한 리스트

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // WebSocket 연결 후 호출
        sessions.add(session); // 새로운 세션을 세션 리스트에 추가
        updateParticipantCount(); // 참가자 수 갱신
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 클라이언트로부터 메시지 받았을 때 호출
        for (WebSocketSession connected: sessions) {
            connected.sendMessage(message);
        } // 연결된 세션에 메시지를 전송
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // WebSocket 연결이 종료된 후 호출
        sessions.remove(session); // 종료된 세션을 리스트에서 제거
        updateParticipantCount(); // 참가자 수 갱신
    }

    private void updateParticipantCount() {
        // 참가자 수를 갱신하고 세션에 참가자 수를 전송
        int count = sessions.size(); // 현재 세션에 참가자 수
        for (WebSocketSession session : sessions) {
            try {
                session.sendMessage(new TextMessage("{\"participantCount\": " + count + "}"));
                // 각 세션에 참가자 수 전송
            } catch (IOException e) {
                e.printStackTrace(); // 예외 처리
            }
        }
    }
}