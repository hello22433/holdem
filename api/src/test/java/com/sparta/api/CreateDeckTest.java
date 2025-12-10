package com.sparta.api;

import com.sparta.core.domain.Card;
import com.sparta.core.domain.Deck;
import org.junit.jupiter.api.Test;


public class CreateDeckTest {

    @Test
    public void createDeck() {
        Deck deck = new Deck();

        Card card1 = deck.draw();
        Card card2 = deck.draw();

        System.out.println("뽑은 카드 1: " + card1.getSuit() + " " + card1.getRank());
        System.out.println("뽑은 카드 2: " + card2.getSuit() + " " + card2.getRank());
        System.out.println("남은 카드 개수: " + deck.getRemainingCount()); // 50
    }
}



