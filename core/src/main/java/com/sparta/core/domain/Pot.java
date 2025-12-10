package com.sparta.core.domain;

import lombok.Getter;

@Getter
public class Pot {
    private long totalAmount; // 테이블에 쌓인 총 판단

    public void add(long amount) {
        this.totalAmount += amount;
    }

    public void reset() {
        this.totalAmount = 0;
    }

    // 롤백용 메서드
    public void recoveryAmount(long originalAmount) {
        this.totalAmount = originalAmount;
    }
}
