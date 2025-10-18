package com.quokka.jobmate_connect.service;

import com.quokka.jobmate_connect.exception.AppException;
import com.quokka.jobmate_connect.exception.ErrorCode;
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

    static final long EXPIRE_MINUTES = 3;
    static final long RESEND_LIMIT_WINDOW = 10;
    static final int MAX_RESEND_COUNT = 3;

    public String generateOtp(String userId) {
        SecureRandom random = new SecureRandom();
        String otp = String.format("%06d", random.nextInt(1000000));

        redisTemplate.opsForValue().set(buildKey(userId), otp, EXPIRE_MINUTES, TimeUnit.MINUTES);
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

    public String resendOtp(String userId) {

        String limitKey = buildResendKey(userId);
        Long count = redisTemplate.opsForValue().increment(limitKey);

        if(count == 1) {
            redisTemplate.expire(limitKey, RESEND_LIMIT_WINDOW, TimeUnit.MINUTES);
        }

        if(count != null && count > MAX_RESEND_COUNT) {
            throw new AppException(ErrorCode.RESEND_OTP_LIMIT);
        }

        String key = buildKey(userId);
        String existingOtp = redisTemplate.opsForValue().get(key);

        if (existingOtp == null) {
            existingOtp = generateOtp(userId);
        }

        return existingOtp;
    }

    private String buildKey(String userId) {
        return "otp:" + userId;
    }

    private String buildResendKey(String userId) {
        return "otp:resend:" + userId;
    }
}
