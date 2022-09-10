package com.nals.rw360.service.v1;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    public Object getHash(final String key, final Object hashKey) {
        return redisTemplate.opsForHash().get(key, hashKey);
    }

    public List<?> multiGetHash(final String key, final Collection<Object> hashKeys) {
        return redisTemplate.opsForHash().multiGet(key, hashKeys);
    }

    public void putHash(final String key, final Object hashKey, final Object value) {
        redisTemplate.opsForSet().add(key, hashKey, value);
    }

    public void expire(final String key, final Duration timeout) {
        redisTemplate.expire(key, timeout);
    }

    public Long deleteHash(final String key, final Object... hashKeys) {
        return redisTemplate.opsForHash().delete(key, hashKeys);
    }
}
