package com.sparta.core.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class Card implements Comparable<Card> {

    private final Rank rank;
    private final Suit suit;

    @Override
    public int compareTo(Card o) {
        return this.rank.getValue() - o.rank.getValue();
    }
}
