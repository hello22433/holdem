package com.sparta.api;

import com.sparta.core.domain.*;
import com.sparta.core.service.HandEvaluator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HandEvaluatorTest {

    private final HandEvaluator handEvaluator = new HandEvaluator();

    @Test
    @DisplayName("1. 로얄 스트레이트 플러시 (Royal Flush)")
    void testRoyalFlush() {
        // Given: 스페이드 10, J, Q, K, A
        List<Card> cards = List.of(
                new Card(Rank.ACE, Suit.SPADE),
                new Card(Rank.KING, Suit.SPADE),
                new Card(Rank.QUEEN, Suit.SPADE),
                new Card(Rank.JACK, Suit.SPADE),
                new Card(Rank.TEN, Suit.SPADE),
                new Card(Rank.TWO, Suit.HEART), // 더미
                new Card(Rank.THREE, Suit.DIAMOND) // 더미
        );

        // When
        HandScore result = handEvaluator.evaluate(cards); // HandScore 반환

        // Then
        assertEquals(HandRank.ROYAL_FLUSH, result.getRank());
    }

    @Test
    @DisplayName("2. 스트레이트 플러시 (Straight Flush)")
    void testStraightFlush() {
        // Given: 하트 5, 6, 7, 8, 9
        List<Card> cards = List.of(
                new Card(Rank.NINE, Suit.HEART),
                new Card(Rank.EIGHT, Suit.HEART),
                new Card(Rank.SEVEN, Suit.HEART),
                new Card(Rank.SIX, Suit.HEART),
                new Card(Rank.FIVE, Suit.HEART),
                new Card(Rank.ACE, Suit.CLUB),
                new Card(Rank.KING, Suit.CLUB)
        );

        HandScore result = handEvaluator.evaluate(cards);
        assertEquals(HandRank.STRAIGHT_FLUSH, result.getRank());
    }

    @Test
    @DisplayName("3. 포 카드 (Four of a Kind)")
    void testFourOfAKind() {
        // Given: 9가 4장
        List<Card> cards = List.of(
                new Card(Rank.NINE, Suit.SPADE),
                new Card(Rank.NINE, Suit.HEART),
                new Card(Rank.NINE, Suit.DIAMOND),
                new Card(Rank.NINE, Suit.CLUB),
                new Card(Rank.ACE, Suit.SPADE),
                new Card(Rank.TWO, Suit.HEART),
                new Card(Rank.THREE, Suit.DIAMOND)
        );

        HandScore result = handEvaluator.evaluate(cards);
        assertEquals(HandRank.FOUR_OF_A_KIND, result.getRank());
    }

    @Test
    @DisplayName("4. 풀 하우스 (Full House)")
    void testFullHouse() {
        // Given: K 3장 + 7 2장
        List<Card> cards = List.of(
                new Card(Rank.KING, Suit.SPADE),
                new Card(Rank.KING, Suit.HEART),
                new Card(Rank.KING, Suit.DIAMOND),
                new Card(Rank.SEVEN, Suit.CLUB),
                new Card(Rank.SEVEN, Suit.SPADE),
                new Card(Rank.TWO, Suit.HEART),
                new Card(Rank.THREE, Suit.DIAMOND)
        );

        HandScore result = handEvaluator.evaluate(cards);
        assertEquals(HandRank.FULL_HOUSE, result.getRank());
    }

    @Test
    @DisplayName("5. 플러시 (Flush)")
    void testFlush() {
        // Given: 클로버 5장 (숫자 연속 안 됨)
        List<Card> cards = List.of(
                new Card(Rank.ACE, Suit.CLUB),
                new Card(Rank.JACK, Suit.CLUB),
                new Card(Rank.NINE, Suit.CLUB),
                new Card(Rank.FOUR, Suit.CLUB),
                new Card(Rank.TWO, Suit.CLUB),
                new Card(Rank.KING, Suit.HEART),
                new Card(Rank.QUEEN, Suit.DIAMOND)
        );

        HandScore result = handEvaluator.evaluate(cards);
        assertEquals(HandRank.FLUSH, result.getRank());
    }

    @Test
    @DisplayName("6. 스트레이트 (Straight)")
    void testStraight() {
        // Given: 5, 6, 7, 8, 9 (무늬 섞임)
        List<Card> cards = List.of(
                new Card(Rank.FIVE, Suit.SPADE),
                new Card(Rank.SIX, Suit.HEART),
                new Card(Rank.SEVEN, Suit.DIAMOND),
                new Card(Rank.EIGHT, Suit.CLUB),
                new Card(Rank.NINE, Suit.SPADE),
                new Card(Rank.ACE, Suit.HEART),
                new Card(Rank.TWO, Suit.DIAMOND)
        );

        HandScore result = handEvaluator.evaluate(cards);
        assertEquals(HandRank.STRAIGHT, result.getRank());
    }

    @Test
    @DisplayName("7. 쓰리 카드 (Three of a Kind)")
    void testThreeOfAKind() {
        // Given: Q 3장
        List<Card> cards = List.of(
                new Card(Rank.QUEEN, Suit.SPADE),
                new Card(Rank.QUEEN, Suit.HEART),
                new Card(Rank.QUEEN, Suit.DIAMOND),
                new Card(Rank.TWO, Suit.CLUB),
                new Card(Rank.FOUR, Suit.SPADE),
                new Card(Rank.SIX, Suit.HEART),
                new Card(Rank.EIGHT, Suit.DIAMOND)
        );

        HandScore result = handEvaluator.evaluate(cards);
        assertEquals(HandRank.THREE_OF_A_KIND, result.getRank());
    }

    @Test
    @DisplayName("8. 투 페어 (Two Pair)")
    void testTwoPair() {
        // Given: J 페어 + 10 페어
        List<Card> cards = List.of(
                new Card(Rank.JACK, Suit.SPADE),
                new Card(Rank.JACK, Suit.HEART),
                new Card(Rank.TEN, Suit.DIAMOND),
                new Card(Rank.TEN, Suit.CLUB),
                new Card(Rank.ACE, Suit.SPADE),
                new Card(Rank.TWO, Suit.HEART),
                new Card(Rank.THREE, Suit.DIAMOND)
        );

        HandScore result = handEvaluator.evaluate(cards);
        assertEquals(HandRank.TWO_PAIR, result.getRank());
    }

    @Test
    @DisplayName("9. 원 페어 (One Pair)")
    void testOnePair() {
        // Given: 2 페어
        List<Card> cards = List.of(
                new Card(Rank.TWO, Suit.SPADE),
                new Card(Rank.TWO, Suit.HEART),
                new Card(Rank.FOUR, Suit.DIAMOND),
                new Card(Rank.FIVE, Suit.CLUB),
                new Card(Rank.NINE, Suit.SPADE),
                new Card(Rank.JACK, Suit.HEART),
                new Card(Rank.KING, Suit.DIAMOND)
        );

        HandScore result = handEvaluator.evaluate(cards);
        assertEquals(HandRank.ONE_PAIR, result.getRank());
    }

    @Test
    @DisplayName("10. 하이 카드 (High Card)")
    void testHighCard() {
        // Given: 족보 없음 (A 하이)
        List<Card> cards = List.of(
                new Card(Rank.ACE, Suit.SPADE),
                new Card(Rank.QUEEN, Suit.HEART),
                new Card(Rank.NINE, Suit.DIAMOND),
                new Card(Rank.SEVEN, Suit.CLUB),
                new Card(Rank.FIVE, Suit.SPADE),
                new Card(Rank.THREE, Suit.HEART),
                new Card(Rank.TWO, Suit.DIAMOND)
        );

        HandScore result = handEvaluator.evaluate(cards);
        assertEquals(HandRank.HIGH_CARD, result.getRank());
    }
}