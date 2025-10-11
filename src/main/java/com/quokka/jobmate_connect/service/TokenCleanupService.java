package com.quokka.jobmate_connect.service;


import com.quokka.jobmate_connect.repository.InvalidatedTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupService {
    private final InvalidatedTokenRepository invalidatedTokenRepository;

    //Chạy mỗi ngày lúc 3h sáng
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupExpiredTokens() {
        long countBefore = invalidatedTokenRepository.count();
        invalidatedTokenRepository.deleteAllByExpiryTimeBefore(new Date());
        long countAfter = invalidatedTokenRepository.count();

        log.info("Dọn dẹp token: {} token đã xoá, còn lại {}",
                (countBefore - countAfter), countAfter);
    }
}
