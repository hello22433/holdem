package com.sparta.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.api.dto.GameActionRequest;
import com.sparta.api.util.BinarySerializer;
import com.sparta.core.domain.ActionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ProtocolEfficiencyTest {

    @Test
    @DisplayName("JSON vs Binary 데이터 크기 비교")
    void compareSize() throws JsonProcessingException {
        // Given: 테스트 데이터 생성
        GameActionRequest request = new GameActionRequest();
        request.setPlayerId("User_Economic_King"); // 18글자
        request.setAction(ActionType.BET);
        request.setAmount(10000L);

        // 1. JSON 직렬화 (기존 방식)
        ObjectMapper objectMapper = new ObjectMapper();
        byte[] jsonBytes = objectMapper.writeValueAsBytes(request);

        // 2. Binary 직렬화 (최적화 방식)
        byte[] binaryBytes = BinarySerializer.serialize(request);

        // 3. 결과 출력
        System.out.println("=========================================");
        System.out.println("[데이터 전송량 비교]");
        System.out.println("JSON   크기: " + jsonBytes.length + " bytes");
        System.out.println("Binary 크기: " + binaryBytes.length + " bytes");

        double reduction = ((double) (jsonBytes.length - binaryBytes.length) / jsonBytes.length) * 100;
        System.out.printf("📉 용량 감소율: %.2f%%\n", reduction);
        System.out.println("=========================================");

        // 검증 (역직렬화가 잘 되는지도 확인)
        GameActionRequest restored = BinarySerializer.deserialize(binaryBytes);
        System.out.println("데이터 복원 확인: " + restored.getPlayerId() + ", " + restored.getAction());
    }
}