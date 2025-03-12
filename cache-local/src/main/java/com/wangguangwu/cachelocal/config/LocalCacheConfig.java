package com.wangguangwu.cachelocal.config;

import com.wangguangwu.cachelocal.constants.CacheTypeConstants;
import com.wangguangwu.cachelocal.properties.LocalCacheProperties;
import com.wangguangwu.cachelocal.service.LocalCacheService;
import com.wangguangwu.cachelocal.service.impl.CaffeineLocalCacheService;
import com.wangguangwu.cachelocal.service.impl.GuavaLocalCacheService;
import com.wangguangwu.cachelocal.service.impl.MapLocalCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 本类用于配置本地缓存的类型。
 *
 * @author wangguangwu
 */
@Configuration
@RequiredArgsConstructor
public class LocalCacheConfig {

    private final LocalCacheProperties localCacheProperties;

    @Bean
    @ConditionalOnProperty(name = CacheTypeConstants.LOCAL_CACHE_TYPE, havingValue = CacheTypeConstants.GUAVA)
    public LocalCacheService<Object, Object> guavaCacheService() {
        return new GuavaLocalCacheService<>(localCacheProperties);
    }

    @Bean
    @ConditionalOnProperty(name = CacheTypeConstants.LOCAL_CACHE_TYPE, havingValue = CacheTypeConstants.CAFFEINE)
    public LocalCacheService<Object, Object> caffeineCacheService() {
        return new CaffeineLocalCacheService<>(localCacheProperties);
    }

    @Bean
    @ConditionalOnProperty(name = CacheTypeConstants.LOCAL_CACHE_TYPE, havingValue = CacheTypeConstants.MAP)
    public LocalCacheService<Object, Object> mapCacheService() {
        return new MapLocalCacheService<>(localCacheProperties);
    }
}
