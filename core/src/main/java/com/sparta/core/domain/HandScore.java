package com.sparta.core.domain;

import lombok.Getter;

import java.util.List;
import java.util.Objects;

@Getter
public class HandScore implements Comparable<HandScore> {
    private final HandRank rank;
    // tiebreaker: 동률일 때 비교할 정수 리스트 (큰 값부터)
    private final List<Integer> tiebreaker;
    // bestFive: 실제 선택된 5장 (디버깅 출력용)
    private final List<Card> bestFive;

    public HandScore(HandRank rank, List<Integer> tiebreaker, List<Card> bestFive) {
        this.rank = rank;
        this.tiebreaker = tiebreaker;
        this.bestFive = bestFive;
    }

    @Override
    public int compareTo(HandScore o) {
        if (this.rank.getValue() != o.rank.getValue()) {
            return Integer.compare(this.rank.getValue(), o.rank.getValue());
        }

        // same rank -> compare tiebreakers lexicographically
        int len = Math.min(this.tiebreaker.size(), o.tiebreaker.size());
        for (int i = 0; i < len; i++) {
            int cmp = Integer.compare(this.tiebreaker.get(i), o.tiebreaker.get(i));
            if (cmp != 0) return cmp;
        }
        return Integer.compare(this.tiebreaker.size(), o.tiebreaker.size());
    }

    @Override
    public String toString() {
        return rank.getDescription() + " - tiebreaker =" + tiebreaker;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HandScore)) return false;
        HandScore handScore = (HandScore) o;
        return rank == handScore.rank && Objects.equals(tiebreaker, handScore.tiebreaker);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rank, tiebreaker);
    }
}
