package com.sparta.api.controller;

import com.sparta.api.dto.GameActionRequest;
import com.sparta.api.util.BinarySerializer;
import com.sparta.core.domain.Table;
import com.sparta.core.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Base64;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class GameSocketController {

    private final GameService gameService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * [1] 기존 JSON 방식 핸들러
     * - 용도: 일반적인 개발 및 디버깅, 웹 클라이언트 호환성
     * - 경로: /app/table/{tableId}/action
     */
    @MessageMapping("/table/{tableId}/action")
    public void handleJsonAction(@DestinationVariable String tableId, GameActionRequest request) {
        // 공통 로직 호출
        processAndBroadcast(tableId, request);
    }

    /**
     * [2] [New] 바이너리 최적화 핸들러 (Protobuf 컨셉)
     * - 용도: 네트워크 대역폭 절감이 필요한 고성능 환경 (Unity/C# 클라이언트 등)
     * - 경로: /app/table/{tableId}/action/binary
     * - 특징: JSON 대비 패킷 크기 약 50% 절감
     */
    @MessageMapping("/table/{tableId}/action/binary")
//    public void handleBinaryAction(@DestinationVariable String tableId, byte[] payload) {
    public void handleBinaryAction(@DestinationVariable String tableId, Map<String,String> payload) {
        byte[] bytes = Base64.getDecoder().decode(payload.get("data"));
        // 1. 바이트 배열을 객체로 역직렬화 (유틸리티 사용)
        GameActionRequest request = BinarySerializer.deserialize(bytes);

        System.out.println("⚡ Binary Action Received! Size: " + bytes.length + " bytes");

        // 2. 공통 로직 호출
        processAndBroadcast(tableId, request);
    }

    /**
     * [공통 로직] 서비스 실행 및 상태 전파 (Broadcasting)
     */
    private void processAndBroadcast(String tableId, GameActionRequest request) {
        // 1. 핵심 비즈니스 로직 실행
        // (Service 메서드명이 submitAction이라고 가정. processAction이나 bet 등 상황에 맞게 사용)
        gameService.submitAction(
                tableId,
                request.getPlayerId(),
                request.getAction(),
                request.getAmount()
        );

        // 2. 변경된 테이블 상태 조회
        Table currentTable = gameService.getTable(tableId);

        // 3. 같은 방의 모두에게 브로드캐스팅 (Pub/Sub)
        // 응답은 프론트엔드 편의성을 위해 JSON으로 유지합니다. (요청만 최적화해도 효과 큼)
        messagingTemplate.convertAndSend("/topic/table/" + tableId, currentTable);
    }
}