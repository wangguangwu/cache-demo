package com.wangguangwu.cachelocal.service.impl;

import com.wangguangwu.cachelocal.constants.CacheTypeConstants;
import com.wangguangwu.cachelocal.service.LocalCacheService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
class GuavaLocalCacheServiceTest {

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        // TODO：使用动态注释可以吗
        registry.add(CacheTypeConstants.LOCAL_CACHE_TYPE, () -> CacheTypeConstants.GUAVA);
    }

    @Autowired
    private LocalCacheService<Object, Object> localCacheService;

    @Test
    @DisplayName("测试 Guava 缓存的 put 和 get 方法")
    void testCachePutAndGet() {
        localCacheService.put("key1", "value1");
        Object value = localCacheService.getIfPresent("key1");
        assertEquals("value1", value);
    }

    @Test
    @DisplayName("测试 Guava 缓存的过期策略")
    void testCacheExpiration() throws InterruptedException {
        localCacheService.put("key2", "value2");
        TimeUnit.SECONDS.sleep(11);
        Object value = localCacheService.getIfPresent("key2");
        assertNull(value);
    }

    @Test
    @DisplayName("测试 Guava 缓存的最大容量限制")
    void testCacheSizeLimit() {
        for (int i = 0; i < 1100; i++) {
            localCacheService.put("key" + i, "value" + i);
        }
        Object value = localCacheService.getIfPresent("key0");
        assertNull(value);
        value = localCacheService.getIfPresent("key1099");
        assertEquals("value1099", value);
    }

    @Test
    @DisplayName("测试 Guava 缓存处理 null 值")
    void testCacheHandlingNullValues() {
        localCacheService.put("key3", null);
        Object value = localCacheService.getIfPresent("key3");
        assertNull(value);
    }

    @Test
    @DisplayName("测试 Guava 缓存为空时的行为")
    void testCacheEmptyBehavior() {
        Object value = localCacheService.getIfPresent("key4");
        assertNull(value);
    }

    @Test
    @DisplayName("测试 Guava 缓存达到最大容量时的驱逐")
    void testCacheEviction() {
        for (int i = 0; i < 1000; i++) {
            localCacheService.put("key" + i, "value" + i);
        }
        localCacheService.put("key1001", "value1001");
        Object value = localCacheService.getIfPresent("key0");
        assertNull(value);
    }

    @Test
    @DisplayName("测试过期后访问值是否正确刷新")
    void testValueRefreshAfterExpiration() throws InterruptedException {
        localCacheService.put("key5", "value5");
        TimeUnit.SECONDS.sleep(11);
        localCacheService.put("key5", "newValue5");
        Object value = localCacheService.getIfPresent("key5");
        assertEquals("newValue5", value);
    }

    @Test
    @DisplayName("测试并发访问")
    void testConcurrentAccess() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 100; i++) {
            final int index = i;
            executor.submit(() -> localCacheService.put("key" + index, "value" + index));
        }
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
        for (int i = 0; i < 100; i++) {
            Object value = localCacheService.getIfPresent("key" + i);
            assertEquals("value" + i, value);
        }
    }

    @Test
    @DisplayName("测试无效化功能")
    void testCacheInvalidation() {
        localCacheService.put("key6", "value6");
        localCacheService.invalidate("key6");
        Object value = localCacheService.getIfPresent("key6");
        assertNull(value);
    }
}