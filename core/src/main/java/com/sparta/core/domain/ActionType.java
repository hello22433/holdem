package com.sparta.core.domain;

public enum ActionType {
    CHECK, // 칩을 내지 않고 순서 넘기기
    CALL, // 앞사람이 낸 만큼 똑같이 내기
    BET, // 내가 처음으로 판돈 키우기
    RAISE, // 앞사람보다 더 많이 내기
    FOLD, // 포기하기 (이번 판 손절매)
    ALL_IN // 전 재산 걸기
}
