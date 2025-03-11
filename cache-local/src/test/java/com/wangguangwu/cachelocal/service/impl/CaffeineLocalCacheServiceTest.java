package com.wangguangwu.cachelocal.service.impl;

import com.wangguangwu.cachelocal.constants.CacheTypeConstants;
import com.wangguangwu.cachelocal.service.LocalCacheService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CaffeineCacheServiceTest {

    @Autowired
    private LocalCacheService<Object, Object> localCacheService;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add(CacheTypeConstants.LOCAL_CACHE_TYPE, () -> CacheTypeConstants.CAFFEINE);
    }

    @Test
    @DisplayName("测试 Caffeine 缓存的 put 和 get 方法")
    void testCachePutAndGet() {
        localCacheService.put("key2", "value2");
        System.out.println("Putting key2 with value2 into cache.");
        Object value = localCacheService.getIfPresent("key2");
        assertEquals("value2", value);
    }

    @Test
    @DisplayName("测试 Caffeine 缓存的 get 方法与映射函数")
    void testCacheGetWithMappingFunction() {
        Object value = localCacheService.get("key3", key -> "mappedValue");
        assertEquals("mappedValue", value);
    }

    @Test
    @DisplayName("测试 Caffeine 缓存的 containsKey 方法")
    void testCacheContainsKey() {
        localCacheService.put("key4", "value4");
        assertTrue(localCacheService.containsKey("key4"));
    }

    @Test
    @DisplayName("测试 Caffeine 缓存的 invalidate 方法")
    void testCacheInvalidate() {
        localCacheService.put("key5", "value5");
        localCacheService.invalidate("key5");
        assertNull(localCacheService.getIfPresent("key5"));
    }

    @Test
    @DisplayName("测试 Caffeine 缓存的 keySet 方法")
    void testCacheKeySet() {
        localCacheService.put("key6", "value6");
        Set<Object> keys = localCacheService.keySet();
        assertTrue(keys.contains("key6"));
    }

    @Test
    @DisplayName("测试 Caffeine 缓存的 size 方法")
    void testCacheSize() {
        localCacheService.put("key7", "value7");
        localCacheService.put("key8", "value8");
        assertEquals(2, localCacheService.size());
    }

    @Test
    @DisplayName("测试 Caffeine 缓存的 invalidateAll 方法")
    void testCacheInvalidateAll() {
        localCacheService.put("key9", "value9");
        localCacheService.put("key10", "value10");
        assertEquals(2, localCacheService.size());

        localCacheService.invalidateAll();
        assertEquals(0, localCacheService.size());
    }
}