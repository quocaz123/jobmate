package com.quokka.jobmate_connect.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OtpService {
    StringRedisTemplate redisTemplate;

    public String generateOtp(String userId) {
        SecureRandom random = new SecureRandom();
        String otp = String.format("%06d", random.nextInt(1000000));

        redisTemplate.opsForValue().set(buildKey(userId), otp, 3, TimeUnit.MINUTES);
        return otp;
    }


    public boolean validateOtp(String userId, String otp) {
        String key = buildKey(userId);
        String cachedOtp = redisTemplate.opsForValue().get(key);

        if(cachedOtp != null && cachedOtp .equals(otp)) {
            redisTemplate.delete(key);
            return true;
        }
        return false;
    }

    private String buildKey(String userId) {
        return "otp:" + userId;
    }
}
