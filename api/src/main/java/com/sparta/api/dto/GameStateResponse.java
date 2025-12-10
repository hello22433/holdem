package com.sparta.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GameStateResponse {
    private String message; // "Player1님이 1000칩을 베팅했습니다."
    private long currentPot; // 현재 팟 금액

    // 테스트용 팩토리 메서드
    public static GameStateResponse test(String msg, long pot) {
        return new GameStateResponse(msg, pot);
    }
}
