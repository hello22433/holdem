package com.sparta.api.dto;

import com.sparta.core.domain.ActionType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GameActionRequest {
    private String playerId;
    private ActionType action; // "BET", "FOLD" 등
    private long amount; // 베팅 금액
}
