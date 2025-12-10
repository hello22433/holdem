package com.sparta.api;

import com.sparta.core.domain.ActionType;
import com.sparta.core.domain.Player;
import com.sparta.core.domain.Pot;
import com.sparta.core.service.BettingService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BettingServiceTest {

    @Test
    public void bettingServiceTest() {
        BettingService bettingService = new BettingService();
        Pot mainPot = new Pot();

        // 1. 플레이어 생성 (초기 자금 10,000칩)
        Player p1 = new Player("user1", "경제학도", 10000);
        Player p2 = new Player("user2", "타짜", 10000);

        // 2. 베팅 시나리오
        bettingService.processBetting(p1, mainPot, ActionType.BET, 1000);
        bettingService.processBetting(p2, mainPot, ActionType.CALL, 1000);
        bettingService.processBetting(p1, mainPot, ActionType.RAISE, 2000);

        // 3. 결과 검증
        assertEquals(7000, p1.getChips(), "P1 칩이 예상과 다릅니다.");
        assertEquals(9000, p2.getChips(), "P2 칩이 예상과 다릅니다.");
        assertEquals(4000, mainPot.getTotalAmount(), "POT 총액이 예상과 다릅니다.");

        // 4. 무결성 검증 (Zero-sum)
        long totalAssets = p1.getChips() + p2.getChips() + mainPot.getTotalAmount();
        assertEquals(20000, totalAssets, "전체 자산 합이 초기값과 같아야 합니다.");
    }
}
