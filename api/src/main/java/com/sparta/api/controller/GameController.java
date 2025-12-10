package com.sparta.api.controller;

import com.sparta.core.domain.ActionType;
import com.sparta.core.domain.Table;
import com.sparta.core.service.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
@Tag(name = "Holdem Game API", description = "게임 진행 및 상태 조회 (REST API)")
public class GameController {

    private final GameService gameService;

    // 1. 방 생성
    @PostMapping("/table/{tableId}")
    @Operation(summary = "게임 방 생성", description = "새로운 게임 테이블을 생성합니다.")
    public ResponseEntity<String> createTable(@PathVariable String tableId) {
        gameService.createTable(tableId);
        return ResponseEntity.ok("Table " + tableId + " created successfully.");
    }

    // 2. 플레이어 입장 (수정됨: Service로 위임)
    @PostMapping("/table/{tableId}/join")
    @Operation(summary = "플레이어 입장", description = "테이블에 새로운 플레이어를 추가합니다. (동시성 제어 적용)")
    public ResponseEntity<String> joinTable(
            @PathVariable String tableId,
            @RequestParam String playerId,
            @RequestParam String playerName,
            @RequestParam long chips
    ) {
        // 컨트롤러는 요청만 전달하고, 실제 락(Lock)과 입장 처리는 Service가 담당합니다.
        gameService.joinPlayer(tableId, playerId, playerName, chips);

        return ResponseEntity.ok(playerName + " joined table " + tableId);
    }

    // 3. 게임 시작
    @PostMapping("/table/{tableId}/start")
    @Operation(summary = "게임 시작 (카드 딜링)", description = "플레이어가 다 모이면 게임을 시작하고 카드를 돌립니다.")
    public ResponseEntity<String> startGame(@PathVariable String tableId) {
        gameService.startGame(tableId);
        return ResponseEntity.ok("Game started! Cards dealt.");
    }

    // 4. 플레이어 액션 (Bet, Fold, Check, All-in 등 통합)
    @PostMapping("/table/{tableId}/action")
    @Operation(summary = "플레이어 액션", description = "베팅, 폴드, 체크, 올인 등의 액션을 수행합니다.")
    public ResponseEntity<String> submitAction(
            @PathVariable String tableId,
            @RequestParam String playerId,
            @RequestParam ActionType action, // Enum으로 받음 (BET, FOLD, CHECK, ALL_IN ...)
            @RequestParam(defaultValue = "0") long amount // FOLD나 CHECK일 때는 0이어도 됨
    ) {
        // GameService -> BettingService 순으로 호출되며 처리됨
        gameService.submitAction(tableId, playerId, action, amount);

        return ResponseEntity.ok("Action [" + action + "] processed successfully.");
    }

    // 5. 다음 스트리트 강제 진행 (테스트용)
    @PostMapping("/table/{tableId}/next-street")
    @Operation(summary = "다음 라운드 진행", description = "강제로 다음 라운드(플랍/턴/리버)로 넘깁니다.")
    public ResponseEntity<String> nextStreet(@PathVariable String tableId) {
        gameService.nextStreet(tableId);
        return ResponseEntity.ok("Proceeded to next street.");
    }

    // 6. 현재 상태 조회
    @GetMapping("/table/{tableId}/status")
    @Operation(summary = "테이블 상태 조회", description = "현재 테이블의 모든 정보(카드, 칩, 플레이어)를 조회합니다.")
    public ResponseEntity<Table> getStatus(@PathVariable String tableId) {
        Table table = gameService.getTable(tableId); // 읽기 전용은 락이 필요 없을 수도 있으나, 상황에 따라 다름
        return ResponseEntity.ok(table);
    }
}