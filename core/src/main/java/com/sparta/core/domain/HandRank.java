package com.sparta.core.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HandRank {
    HIGH_CARD(1, "하이 카드"),
    ONE_PAIR(2, "원 페어"),
    TWO_PAIR(3, "투 페어"),
    THREE_OF_A_KIND(4, "쓰리 카드"),
    STRAIGHT(5, "스트레이트"),
    FLUSH(6,"플러시"),
    FULL_HOUSE(7, "풀 하우스"),
    FOUR_OF_A_KIND(8, "포 카드"),
    STRAIGHT_FLUSH(9, "스트레이트 플러시"),
    ROYAL_FLUSH(10, "로얄 스트레이트 플러시");

    private final int value; // 높을수록 강함
    private final String description;
}
