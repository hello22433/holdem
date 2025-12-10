package com.sparta.core.domain;

import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@ToString
public class Player {
    private final String id;
    private final String name;
    private long chips; // 현재 보유 칩 (Asset)
    private long currentBet; // 이번 라운드에 낸 칩 (Sunk Cose)
    private boolean isFolded; // 포기 여부
    private List<Card> holeCards = new ArrayList<>(); // 손패

    public Player(String id, String name, long chips) {
        this.id = id;
        this.name = name;
        this.chips = chips;
        this.currentBet = 0;
        this.isFolded = false;
    }

    // [핵심 로직] 칩 차감 (돈이 빠져나가는 유일한 통로)
    public void betChips(long amount) {
        if (amount > this.chips) {
            throw new IllegalArgumentException("보유 칩보다 많은 금액을 배팅할 수 없습니다.");
        }
        if (amount < 0) {
            throw new IllegalArgumentException("마이너스 금액은 베팅할 수 없습니다.");
        }
        this.chips -= amount; // 자산 감소
        this.currentBet += amount; // 베팅액 증가
    }

    public void fold() {
        this.isFolded = true;
    }

    public void resetRound() {
        this.currentBet = 0; // 라운드 끝나면 초기화
    }

    public void receiveCard(Card c1, Card c2) {
        this.holeCards.clear();
        this.holeCards.add(c1);
        this.holeCards.add(c2);
    }

    public List<Card> getHoleCards() {
        return holeCards;
    }

    public void setFolded(boolean folded) {
        this.isFolded = folded;
    }

    // 칩 획득 (승리 시)
    public void winChips(long amount) {
        this.chips += amount;
    }

    // 롤백용 메서드 (일반적인 게임 로직에선 쓰지 않고, 오직 트랜잭션 복구용으로만 사용)
    public void recoveryChips(long originalAmount) {
        this.chips = originalAmount;
        // 필요하다면 currentBet도 복구해야 함
    }
}
