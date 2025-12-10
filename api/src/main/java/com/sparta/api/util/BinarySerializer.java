package com.sparta.api.util;

import com.sparta.api.dto.GameActionRequest;
import com.sparta.core.domain.ActionType;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class BinarySerializer {

    /**
     * [직렬화] 객체 -> byte[] (압축)
     * 구조: [Action(1byte)] + [Amount(8byte)] + [PlayerId길이(4byte)] + [PlayerId(Nbyte)]
     */
    public static byte[] serialize(GameActionRequest request) {
        // 1. 데이터 준비
        byte actionByte = (byte) request.getAction().ordinal(); // Enum을 숫자로 (BET=0, CALL=1...)
        long amount = request.getAmount();
        byte[] idBytes = request.getPlayerId().getBytes(StandardCharsets.UTF_8);
        int idLength = idBytes.length;

        // 2. 버퍼 할당 (1 + 8 + 4 + N)
        ByteBuffer buffer = ByteBuffer.allocate(1 + 8 + 4 + idLength);

        // 3. 데이터 기록 (순서가 중요함!)
        buffer.put(actionByte);
        buffer.putLong(amount);
        buffer.putInt(idLength); // 가변 길이 문자열 처리를 위해 길이 먼저 기록
        buffer.put(idBytes);

        return buffer.array();
    }

    /**
     * [역직렬화] byte[] -> 객체 (복원)
     * 받는 쪽(서버/클라이언트)에서 사용
     */
    public static GameActionRequest deserialize(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        // 1. 순서대로 읽기
        int actionOrdinal = buffer.get(); // 1byte 읽기
        ActionType action = ActionType.values()[actionOrdinal];

        long amount = buffer.getLong(); // 8byte 읽기

        int idLength = buffer.getInt(); // 4byte 읽기
        byte[] idBytes = new byte[idLength];
        buffer.get(idBytes); // 길이만큼 문자열 읽기
        String playerId = new String(idBytes, StandardCharsets.UTF_8);

        // 2. 객체 생성
        GameActionRequest request = new GameActionRequest();
        request.setAction(action);
        request.setAmount(amount);
        request.setPlayerId(playerId);

        return request;
    }
}
