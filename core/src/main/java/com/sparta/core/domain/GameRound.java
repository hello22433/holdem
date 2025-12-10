package com.sparta.core.domain;

public enum GameRound {
    PRE_FLOP, // 카드 2장 받음 (공유카드 없음)
    FLOP, // 공유카드 3장 오픈
    TURN, // 공유카드 4장째 오픈
    RIVER, // 공유카드 5장째 오픈 (마지막)
    SHOWDOWN // 승자 판정
}
