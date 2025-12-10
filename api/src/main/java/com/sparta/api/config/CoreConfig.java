package com.sparta.api.config;

import com.sparta.core.service.BettingService;
import com.sparta.core.service.GameService;
import com.sparta.core.service.HandEvaluator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CoreConfig {

    @Bean
    public GameService gameService() {
        return new GameService(new HandEvaluator(), new BettingService());
    }

    @Bean
    public HandEvaluator handEvaluator() {
        return new HandEvaluator();
    }
}
