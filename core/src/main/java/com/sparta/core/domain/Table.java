package com.sparta.core.domain;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Table {
    private final String id;
    private final List<Player> players = new ArrayList<>();
    private final List<Card> communityCards = new ArrayList<>();
    private final Deck deck;
    private final Pot pot;
    private GameRound currentRound;

    public static final int MAX_PLAYERS = 6; // 테이블 최대 인원

    public Table(String id) {
        this.id = id;
        this.deck = new Deck();
        this.pot = new Pot();
        this.currentRound = GameRound.PRE_FLOP;
    }

    public void addPlayer(Player player) {
        // 1. 정원 체크
        if (players.size() >= MAX_PLAYERS) {
            throw new IllegalStateException("테이블이 꽉 찼습니다.");
        }

        // 2. 중복 입장 체크
        boolean isExist = players.stream()
                        .anyMatch(p -> p.getId().equals(player.getId()));
        if (isExist) {
            throw new IllegalArgumentException("이미 참여 중인 플레이어입니다.");
        }

        // 3. 최소 바이인(참가비) 체크 (예: 1000칩 이상 있어야 함)
        if (player.getChips() < 1000) {
            throw new IllegalArgumentException("칩이 부족하여 입장할 수 없습니다.");
        }

        players.add(player);
    }

    public void dealCommunityCard(int count) {
        for (int i = 0; i < count; i++) {
            communityCards.add(deck.draw());
        }
    }

    public void advanceRound() {
        switch (currentRound) {
            case PRE_FLOP -> currentRound = GameRound.FLOP;
            case FLOP -> currentRound = GameRound.TURN;
            case TURN ->  currentRound = GameRound.RIVER;
            case RIVER -> currentRound = GameRound.SHOWDOWN;
        }
    }

    public void prepareNewGame() {
        deck.shuffle(); // Deck 내부에서 카드 초기화 + shuffle
        communityCards.clear(); // 바닥 카드 초기화
        pot.reset();
        currentRound = GameRound.PRE_FLOP; // 라운드 초기화

        // 플레이어 초기화
        for (Player player : players) {
            player.resetRound(); // 이전 판 기록 초기화
            player.setFolded(false); // 폴드 상태 초기화
            player.getHoleCards().clear(); // 손패 초기화
        }
    }
}
