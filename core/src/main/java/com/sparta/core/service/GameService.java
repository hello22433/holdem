package com.sparta.core.service;

import com.sparta.core.domain.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class GameService {

    // DB 대신 메모리에 게임 방 저장 (간소화)
    // 동시성 제어를 위해 ConcurrentHashMap 사용
    private final Map<String, Table> tableRepository = new ConcurrentHashMap<>();
    private final HandEvaluator handEvaluator;
    private final BettingService bettingService;

    // 순수 자바 생성자 주입
    public GameService(HandEvaluator handEvaluator, BettingService bettingService) {
        this.handEvaluator = handEvaluator;
        this.bettingService = bettingService;
    }

    // 1. 방 생성
    public Table createTable(String tableId) {
        // 이미 존재하는 방인지 체크
        if (tableRepository.containsKey(tableId)) {
            throw new IllegalArgumentException("이미 존재하는 테이블 ID입니다.");
        }
        Table table = new Table(tableId);
        tableRepository.put(tableId, table);
        return table;
    }

    // 게임 시작 (방 생성과 분리하거나, 방 생성 후 호출)
    public void startGame(String tableId) {
        Table table = getTableOrThrow(tableId);

        synchronized (table) {
            // 1) 최소 인원 체크 (혼자서는 게임 불가)
            if (table.getPlayers().size() < 2) {
                throw new IllegalStateException("게임 시작을 위해서는 최소 2명의 플레이어가 필요합니다.");
            }

            // 2) 테이블 청소 및 덱 준비
            table.prepareNewGame();

            // 3) [핵심] 딜링: 모든 플레이어에게 2장씩 지금
            for (Player player : table.getPlayers()) {
                Card card1 = table.getDeck().draw();
                Card card2 = table.getDeck().draw();

                player.receiveCard(card1, card2);
            }
        }
    }

    // 2. 베팅 처리 (동시성 제어 적용)
    public void submitAction(String tableId, String playerId, ActionType action, long amount) {
        // 1. 테이블 조회
        Table table = getTableOrThrow(tableId);

        // [중요] 해당 테이블 객체에'만' 락을 걸어 동시에 여러 명이 베팅하거나 상태를 바꾸지 못하게 함
        synchronized (table) {
            Player player = table.getPlayers().stream()
                    .filter(p -> p.getId().equals(playerId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("플레이어가 없습니다."));

            // 2. BettingService에 위임
            bettingService.processBetting(player, table.getPot(), action, amount);

            // 3. (선택) 베팅 후 게임 상태 업데이터 (턴 넘기기 등)
            // checkNextTurn(table)
        }
    }

    // 3. 다음 라운드로 진행 (핵심 로직)
    public void nextStreet(String tableId) {
        Table table = getTableOrThrow(tableId);

        synchronized (table) {
            table.advanceRound(); // 라운드 상태 변경

            // 라운드별 카드 오픈 규칙
            switch (table.getCurrentRound()) {
                case FLOP -> table.dealCommunityCard(3); // 3장 오픈
                case TURN, RIVER -> table.dealCommunityCard(1); // 1장씩 오픈
                case SHOWDOWN -> processShowdown(table);
            }
        }

    }

    // 4. 쇼다운 및 승자 처리
    private void processShowdown(Table table) {
        // 1) 활성 플레이어 추출
        List<Player> activePlayers = getActivePlayers(table);
        if (activePlayers.isEmpty()) {
            throw new IllegalStateException("승자를 판별할 플레이어가 없습니다."); // 발생하면 안 되는 상황
        }

        // 2) 점수 계산 (Map<Player, HandScore>)
        // HandScore는 Comparable을 구현하여 점수 비교가 가능해야 함
        Map<Player, HandScore> playerScores = calculateScores(activePlayers, table.getCommunityCards());

        // 3) 최고 점수 찾기
        HandScore maxScore = Collections.max(playerScores.values());

        // 4) 공동 우승자(Split) 찾기
        List<Player> winners = playerScores.entrySet().stream()
                .filter(entry -> entry.getValue().compareTo(maxScore) == 0) // 점수가 같으면 우승자
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // 5) 팟 분배 (정산)
        distributePot(table, winners);

        // 6) 라운드 종료 및 초기화
//        resetTableForNextGame(table);
        table.prepareNewGame();
    }

    // 입장도 동시성 제어가 필요함
    public void joinPlayer(String tableId, String playerId, String name, long chips) {
        Table table = getTableOrThrow(tableId);

        synchronized (table) { // 입장하는 순간 게임이 시작되거나 다른 사람이 들어오는 것을 방지
            Player newPlayer = new Player(playerId, name, chips);
            table.addPlayer(newPlayer);
        }
    }

    // --- Helper Methods (복잡도 분리) ---
    private List<Player> getActivePlayers(Table table) {
        return table.getPlayers().stream()
                .filter(p -> !p.isFolded())
                .collect(Collectors.toList());
    }

    private Map<Player, HandScore> calculateScores(List<Player> activePlayers, List<Card> communityCards) {
        Map<Player, HandScore> scores = new HashMap<>();
        for (Player player : activePlayers) {
            List<Card> totalCards = new ArrayList<>(player.getHoleCards());
            totalCards.addAll(communityCards);

            // Evaluator가 단순 Rank가 아니라 비교 가능한 Score 객체를 반환한다고 가정
            HandScore score = handEvaluator.evaluate(totalCards);
            scores.put(player, score);
        }
        return scores;

    }

    private void distributePot(Table table, List<Player> winners) {
        long totalPot = table.getPot().getTotalAmount();
        int winnersCount = winners.size();

        if (winnersCount == 0) return; // 방어 코드

        long prizePerWinner = totalPot / winnersCount;
        long remainder = totalPot % winnersCount;

        // 팟 분배 결과
        for (Player winner : winners) {
            winner.winChips(prizePerWinner);
        }

        // 자투리 칩 처리 (SB 포지션 등 규칙이 있지만, 여기선 첫 번째 승자에게 몰아주기)
        if (remainder > 0) {
            winners.get(0).winChips(remainder);
        }
    }

    public Table getTableOrThrow(String tableId) {
        Table table = tableRepository.get(tableId);
        if (table == null) {
            throw new IllegalArgumentException("존재하지 않는 테이블입니다: " + tableId);
        }
        return table;
    }


    public Table getTable(String tableId) {
        return tableRepository.get(tableId);
    }
}
