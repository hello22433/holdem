package com.sparta.core.service;

import com.sparta.core.domain.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HandEvaluator {

    public HandScore evaluate(List<Card> originalCards) {
        // 1. 방어 로직: 카드는 반드시 7장이어야 한다고 가정 (핸드2 + 커뮤니티5)
        if (originalCards == null || originalCards.size() < 5) {
            throw new IllegalArgumentException("카드가 부족합니다.");
        }

        // 2. 정렬 (높은 숫자가 먼저 오도록 내림차순 정렬 Ace -> King -> ... -> 2)
        // -> 정렬을 해두면 스트레이트나 하이카드를 찾기 훨씬 쉬워집니다. (비용 최적화)
        // [수정] 원본 리스트를 건드리지 않기 위해 '새로운 리스트'로 복사합니다.
        // 이렇게 하면 List.of()로 들어온 불변 리스트도 문제없이 정렬할 수 있습니다.
        List<Card> cards = new ArrayList<>(originalCards);

        // 2. 정렬 (이제 복사본인 cards를 정렬하므로 에러가 안 납니다)
        cards.sort(Collections.reverseOrder());

        // 족보 판별 로직
        // [플러시 체크]
        HandScore flushScore = checkFlush(cards);
        if (flushScore != null) return flushScore;

        // [스트레이트 체크]
        HandScore straightScore = checkStraight(cards);
        if (straightScore != null) return straightScore;

        // [페어 계열 체크] (포카드, 풀하우스, 트리플, 투페어, 원페어, 하이카드)
        return checkPairs(cards);
    }

    // --- 내부 로직 ---
    private HandScore checkFlush(List<Card> cards) {
        // 1. 무늬별로 그룹핑
        Map<Suit, List<Card>> suitMap = cards.stream()
                .collect(Collectors.groupingBy(Card::getSuit));

        for (List<Card> suitedCards : suitMap.values()) {
            // 2. 같은 무늬가 5장 이상인가?
            if (suitedCards.size() >= 5) {
                // 정렬 (내림차순)
                suitedCards.sort(Collections.reverseOrder());

                // ---------------------------------------------------------
                // [핵심 수정] 플러시 카드들로 '스트레이트' 여부를 먼저 체크한다!
                // ---------------------------------------------------------
                HandScore straightFlushResult = checkStraight(suitedCards);

                if (straightFlushResult != null) {
                    // 스트레이트가 됨 -> 즉, '스트레이트 플러시'임

                    // 가장 높은 숫자가 ACE(14)인가? -> 로얄 스트레이트 플러시
                    int topRank = straightFlushResult.getTiebreaker().get(0);
                    HandRank finalRank = (topRank == 14) ? HandRank.ROYAL_FLUSH : HandRank.STRAIGHT_FLUSH;

                    return new HandScore(finalRank, straightFlushResult.getTiebreaker(), straightFlushResult.getBestFive());
                }

                // ---------------------------------------------------------
                // 스트레이트가 안 되면 -> 그냥 '플러시'
                // ---------------------------------------------------------
                List<Card> best5 = suitedCards.subList(0, 5);

                // 타이브레이커: 카드 5장의 숫자 그대로 (높은 숫자 순)
                List<Integer> tiebreaker = best5.stream().map(c -> c.getRank().getValue()).toList();

                return new HandScore(HandRank.FLUSH, tiebreaker, best5);
            }
        }

        return null;
    }

    private HandScore checkStraight(List<Card> cards) {
        // 중복 숫자 제거 및 정렬
        List<Integer> ranks = cards.stream()
                .map(c -> c.getRank().getValue())
                .distinct()
                .sorted(Collections.reverseOrder())
                .toList();

        // 연속된 5개 숫자 찾기
        for (int i = 0; i <= ranks.size() - 5; i++) {
            int current = ranks.get(i);
            if (ranks.get(i+1) == current - 1 &&
                ranks.get(i+2) == current - 2 &&
                ranks.get(i+3) == current - 3 &&
                ranks.get(i+4) == current - 4) {
                // 스트레이트 완성 ! (타이브레이커는 가장 높은 숫자 하나만 있어도 됨)
                return new HandScore(HandRank.STRAIGHT, List.of(current), new ArrayList<>()); // bestFive 생략
            }
        }
        return null;
    }

    private HandScore checkPairs(List<Card> cards) {
        // 숫자별로 그룹핑 (Map<Rank, Count>)
        Map<Integer, Long> rankCount = cards.stream()
                .collect(Collectors.groupingBy(c -> c.getRank().getValue(), Collectors.counting()));

        List<Integer> fours = new ArrayList<>();
        List<Integer> threes = new ArrayList<>();
        List<Integer> pairs = new ArrayList<>();
        List<Integer> singles = new ArrayList<>();

        // 그룹별 분류
        for (Map.Entry<Integer, Long> entry : rankCount.entrySet()) {
            int rank = entry.getKey();
            long count = entry.getValue();
            if (count == 4) fours.add(rank);
            else if (count == 3) threes.add(rank);
            else if (count == 2) pairs.add(rank);
            else singles.add(rank);
        }

        // 높은 숫자가 먼저 오도록 정렬
        fours.sort(Collections.reverseOrder());
        threes.sort(Collections.reverseOrder());
        pairs.sort(Collections.reverseOrder());
        singles.sort(Collections.reverseOrder());

        // 1. 포카드
        if (!fours.isEmpty()) {
            int fourRank = fours.get(0);
            // 키커 찾기 (포카드를 제외한 가장 높은 카드 1장)
            int kicker = findKickers(cards, List.of(fourRank), 1).get(0);
            return new HandScore(HandRank.FOUR_OF_A_KIND, List.of(fourRank, kicker), new ArrayList<>());
        }

        // 2. 풀하우스 (3장 + 2장)
        if (!threes.isEmpty() && (!pairs.isEmpty() || threes.size() > 1)) {
            int threeRank = threes.get(0);
            int pairRank = (threes.size() > 1) ? threes.get(1) : pairs.get(0); // 트리플이 2개면 낮은 트리플이 페어 역할
            return new HandScore(HandRank.FULL_HOUSE, List.of(threeRank, pairRank), new ArrayList<>());
        }

        // 3. 플러시, 스트레이트는 위에서 이미 체크함

        // 4. 트리플 (3장)
        if (!threes.isEmpty()) {
            int threeRank = threes.get(0);
            // 키커 2장 필요
            List<Integer> kickers = findKickers(cards, List.of(threeRank), 2);
            List<Integer> tiebreaker = new ArrayList<>();
            tiebreaker.add(threeRank);
            tiebreaker.addAll(kickers);
            return new HandScore(HandRank.THREE_OF_A_KIND, tiebreaker, new ArrayList<>());
        }

        // 5. 투 페어
        if (pairs.size() >= 2) {
            int highPair = pairs.get(0);
            int lowPair = pairs.get(1);

            // 키커 1장 필요 (두 페어를 제외한 나머지 중 가장 높은 것)
            List<Integer> kickers = findKickers(cards, List.of(highPair, lowPair), 1);
            List<Integer> tiebreaker = new ArrayList<>();
            tiebreaker.add(highPair);
            tiebreaker.add(lowPair);
            tiebreaker.addAll(kickers);
            return new HandScore(HandRank.TWO_PAIR, tiebreaker, new ArrayList<>());
        }

        // 6. 원 페어
        if (!pairs.isEmpty()) {
            int pairRank = pairs.get(0);
            // 키커 3장 필요
            List<Integer> kickers = findKickers(cards, List.of(pairRank), 3);
            List<Integer> tiebreaker = new ArrayList<>();
            tiebreaker.add(pairRank);
            tiebreaker.addAll(kickers);
            return new HandScore(HandRank.ONE_PAIR, tiebreaker, new ArrayList<>());
        }

        // 7. 하이 카드 (탑 5)
        List<Integer> top5 = cards.stream()
                .map(c -> c.getRank().getValue())
                .limit(5)
                .toList();
        return new HandScore(HandRank.HIGH_CARD, top5, cards.subList(0,5));

    }

    // 키커 찾기 헬퍼 메서드 (제외할 랭크를 빼고 상위 N개 리턴)
    private List<Integer> findKickers(List<Card> allCards, List<Integer> excludeRanks, int count) {
        return allCards.stream()
                .map(c -> c.getRank().getValue())
                .filter(r -> !excludeRanks.contains(r)) // 메인 족보 카드 제외
                .limit(count)
                .toList();
    }
}























