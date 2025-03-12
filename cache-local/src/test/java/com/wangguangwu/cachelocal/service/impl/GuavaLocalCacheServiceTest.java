package com.wangguangwu.cachelocal.service.impl;

import com.wangguangwu.cachelocal.constants.CacheTypeConstants;
import com.wangguangwu.cachelocal.properties.LocalCacheProperties;
import com.wangguangwu.cachelocal.service.LocalCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
class GuavaLocalCacheServiceTest {

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add(CacheTypeConstants.LOCAL_CACHE_TYPE, () -> CacheTypeConstants.GUAVA);
    }

    @Autowired
    private LocalCacheService<Object, Object> localCacheService;

    @Autowired
    private LocalCacheProperties localCacheProperties;

    @BeforeEach
    void setUp() {
        localCacheService.invalidateAll();
    }

    @AfterEach
    void tearDown() {
        localCacheService.invalidateAll();
    }

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
        // 过期前获取，期望存在值
        Object valueBeforeExpiration = localCacheService.getIfPresent("key2");
        assertEquals("value2", valueBeforeExpiration);
        // 等待过期时间后
        TimeUnit.SECONDS.sleep(localCacheProperties.getExpireAfterWrite());
        // 过期后获取，期望为空
        Object valueAfterExpiration = localCacheService.getIfPresent("key2");
        assertNull(valueAfterExpiration);
    }

    @Test
    @DisplayName("测试 Guava 缓存的最大容量限制")
    void testCacheSizeLimit() {
        int maxSize = (int) localCacheProperties.getMaximumSize();
        // 超出最大容量
        int totalEntries = maxSize + 10;
        for (int i = 0; i < totalEntries; i++) {
            localCacheService.put("key" + i, "value" + i);
        }
        // 最早插入的 key 应该被驱逐
        Object value = localCacheService.getIfPresent("key0");
        assertNull(value);
        // 最新插入的 key 应该存在
        value = localCacheService.getIfPresent("key" + (totalEntries - 1));
        assertEquals("value" + (totalEntries - 1), value);
    }

    @Test
    @DisplayName("测试 Guava 缓存处理 null 值")
    void testCacheHandlingNullValues() {
        // 期望存储 null 时抛出 NullPointerException
        assertThrows(NullPointerException.class, () -> localCacheService.put("key3", null));
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
        int maxSize = (int) localCacheProperties.getMaximumSize();
        // 填满缓存至最大容量
        for (int i = 0; i < maxSize; i++) {
            localCacheService.put("key" + i, "value" + i);
        }
        // 插入一个新元素，触发驱逐
        localCacheService.put("key" + maxSize, "value" + maxSize);
        // 检查最早插入的 key 应该被驱逐
        Object evictedValue = localCacheService.getIfPresent("key0");
        assertNull(evictedValue);
        // 检查最后插入的 key 应该存在，确保不是因过期而被清空
        Object latestValue = localCacheService.getIfPresent("key" + maxSize);
        assertEquals("value" + maxSize, latestValue);
        // 检查中间的 key 仍然存在
        int midIndex = maxSize / 2;
        Object midValue = localCacheService.getIfPresent("key" + midIndex);
        assertEquals("value" + midIndex, midValue);
    }

    @Test
    @DisplayName("测试过期后访问值是否正确刷新")
    void testValueRefreshAfterExpiration() throws InterruptedException {
        localCacheService.put("key5", "value5");
        // 等待超出配置的过期时间
        TimeUnit.SECONDS.sleep(localCacheProperties.getExpireAfterWrite() + 1);
        localCacheService.put("key5", "newValue5");
        Object value = localCacheService.getIfPresent("key5");
        assertEquals("newValue5", value);
    }

    @Test
    @DisplayName("测试并发访问")
    void testConcurrentAccess() throws InterruptedException {
        int numberOfTasks = 10;
        CountDownLatch latch = new CountDownLatch(numberOfTasks);
        ExecutorService executor = Executors.newFixedThreadPool(5);
        for (int i = 0; i < numberOfTasks; i++) {
            final int index = i;
            // 使用 CountDownLatch 确保每个任务完成后计数器减一
            executor.submit(() -> {
                localCacheService.put("key" + index, "value" + index);
                latch.countDown();
            });
        }
        executor.shutdown();
        // 等待所有任务完成（确保执行时间不超过缓存过期时间）
        latch.await(2, TimeUnit.SECONDS);
        for (int i = 0; i < numberOfTasks; i++) {
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


    @Test
    @DisplayName("测试 get 方法带 mappingFunction")
    void testCacheGetWithMappingFunction() {
        String key = "key7";
        // 当 key 不存在时，通过 mappingFunction 计算并返回值，同时写入缓存
        Object value1 = localCacheService.get(key, k -> "computedValue");
        assertEquals("computedValue", value1);

        // 当 key 已存在时，再次调用 get 方法应直接返回缓存中的值，不调用新的 mappingFunction
        Object value2 = localCacheService.get(key, k -> "newComputedValue");
        assertEquals("computedValue", value2);
    }
}