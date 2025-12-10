package com.sparta.core.service;

import com.sparta.core.domain.ActionType;
import com.sparta.core.domain.Player;
import com.sparta.core.domain.Pot;

public class BettingService {

    /**
     * 락(Lock)은 이 메서드를 호출하는 쪽(GameService)에서 제어한다고 가정합니다.
     * 여기서는 순수 비즈니스 로직(검증, 계산)만 수행합니다.
     */
    public void processBetting(Player player, Pot pot, ActionType action, long amount) {


        // 1. 공통 검증 (죽은 사람, 칩 부족 등)
        validateAvailable(player, action, amount);

        // [Transaction Start] 상태 스냅샷 저장 (Rollback을 위한 백업)
        long beforePlayerChips = player.getChips();
        long beforePotTotal = pot.getTotalAmount();

        try {
            // 2. 액션별 처리
            switch (action) {
                case BET, RAISE, CALL -> handleChipBetting(player, pot, amount);
                case ALL_IN -> handleAllIn(player, pot);
                case FOLD -> player.fold();
                case CHECK -> { /* 체크는 칩 이동 없음 */ }
            }

            // 3. [핵심] 무결성 검증 (Zero-Sum Check)
            // -> 돈이 오가는 액션인 경우에만 검사
            if (isChipMoveAction(action)) {
                long playerDecreased = beforePlayerChips - player.getChips();
                long potIncreased = pot.getTotalAmount() - beforePotTotal;

                if (playerDecreased != potIncreased) {
                    throw new IllegalStateException("CRITICAL: 자산 불일치 발생! (Player 감소분 != Pot 증가분)");
                }
            }
        } catch (Exception e) {
            // 4. 롤백
            // 4. [Rollback] 예외 발생 시 태초의 상태(스냅샷)로 강제 복구
            System.err.println("[Rollback Triggered] " + e.getMessage());

            player.recoveryChips(beforePlayerChips);
            pot.recoveryAmount(beforePotTotal);

            // 상위로 예외 전파 (유저에게 알림)
            throw new RuntimeException("베팅 처리 중 오류가 발생하여 취소되었습니다.", e);
        }
    }

    private void validateAvailable(Player player, ActionType action, long amount) {
        if (player.isFolded()) {
            throw new IllegalStateException("이미 폴드한 플레이어입니다.");
        }
        if ((action == ActionType.BET || action == ActionType.RAISE) && player.getChips() < amount) {
            throw new IllegalStateException("칩이 부족합니다.");
        }
        // 추가적인 포커 룰 검증 (최소 베팅금액 등)이 여기에 들어갈 수 있음
    }

    private void handleChipBetting(Player player, Pot pot, long amount) {
        player.betChips(amount);
        pot.add(amount);
    }

    private boolean isChipMoveAction(ActionType action) {
        return action == ActionType.BET || action == ActionType.RAISE ||
                action == ActionType.CALL || action == ActionType.ALL_IN;
    }

    private void handleAllIn(Player player, Pot pot) {
        long allInAmount = player.getChips();
        player.betChips(allInAmount);
        pot.add(allInAmount);
    }
}
