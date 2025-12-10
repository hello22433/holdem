package com.sparta.core.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

public class Deck {
    private final List<Card> cards = new ArrayList<>();

    // 생성자 : 덱을 만들 때 52장의 카드를 자동 생성
    public Deck() {
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                cards.add(new Card(rank, suit));
            }
        }
        shuffle();
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public Card draw() {
        if (cards.isEmpty()) {
            throw new NoSuchElementException("덱에 카드가 없습니다.");
        }
        return cards.remove(0);
    }

    public int getRemainingCount() {
        return cards.size();
    }



}
