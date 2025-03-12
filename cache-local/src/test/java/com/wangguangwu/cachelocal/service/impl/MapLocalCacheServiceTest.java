package com.wangguangwu.cachelocal.service.impl;

import com.wangguangwu.cachelocal.properties.LocalCacheProperties;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MapLocalCacheService 的单元测试类
 */
class MapLocalCacheServiceTest {

    private static MapLocalCacheService<String, String> cacheService;

    @BeforeAll
    static void beforeAll() {
        // 初始化缓存配置
        LocalCacheProperties localCacheProperties = new LocalCacheProperties();
        localCacheProperties.setMaximumSize(2);
        localCacheProperties.setExpireAfterWrite(3);

        // 初始化缓存服务，作为所有测试共享的实例
        cacheService = new MapLocalCacheService<>(localCacheProperties);
    }

    @BeforeEach
    void beforeEach() {
        // 清空缓存
        cacheService.invalidateAll();
    }

    @Test
    @DisplayName("测试缓存的存取，包括过期项的移除")
    void testPutAndGetIfPresent() {
        cacheService.put("key1", "value1");
        cacheService.put("key2", "value2");

        // 验证缓存中的值
        assertEquals("value1", cacheService.getIfPresent("key1"));
        assertEquals("value2", cacheService.getIfPresent("key2"));

        // 超过最大缓存容量，测试是否会移除过期项
        cacheService.put("key3", "value3");
        assertNull(cacheService.getIfPresent("key1"));
    }

    @Test
    @DisplayName("测试缓存过期行为")
    void testCacheExpiration() throws InterruptedException {
        cacheService.put("key1", "value1");

        // 等待超过过期时间
        TimeUnit.SECONDS.sleep(4);

        // 验证过期后缓存项是否被移除
        assertNull(cacheService.getIfPresent("key1"));
    }

    @Test
    @DisplayName("测试使用映射函数的 get 方法")
    void testGetWithMappingFunction() {
        String result = cacheService.get("key1", key -> "computedValue");
        assertEquals("computedValue", result);

        // 再次调用，应该直接从缓存中获取值，而不是重新计算
        result = cacheService.get("key1", key -> "newValue");
        // 缓存中的值应该是第一个计算的值
        assertEquals("computedValue", result);
    }

    @Test
    @DisplayName("测试检查缓存中是否包含指定键")
    void testContainsKey() {
        cacheService.put("key1", "value1");
        assertTrue(cacheService.containsKey("key1"));
        assertFalse(cacheService.containsKey("key2"));
    }

    @Test
    @DisplayName("测试移除单个缓存项")
    void testInvalidate() {
        cacheService.put("key1", "value1");

        // 验证缓存中存在 key1
        assertEquals("value1", cacheService.getIfPresent("key1"));

        // 移除 key1 后，缓存中不应再有该键
        cacheService.invalidate("key1");
        assertNull(cacheService.getIfPresent("key1"));
    }

    @Test
    @DisplayName("测试移除所有缓存项")
    void testInvalidateAll() {
        cacheService.put("key1", "value1");
        cacheService.put("key2", "value2");

        // 验证缓存中有两个元素
        assertEquals(2, cacheService.size());

        // 清空缓存
        cacheService.invalidateAll();
        assertEquals(0, cacheService.size());
    }

    @Test
    @DisplayName("测试缓存的并发 put 和 get 操作")
    void testConcurrentPutAndGet() throws InterruptedException {
        int threadCount = 5;
        int iterations = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int threadNum = i;
            executor.submit(() -> {
                System.out.println(Thread.currentThread().getName() + "开始执行");
                try {
                    for (int j = 0; j < iterations; j++) {
                        String key = "concurrentKey_" + (threadNum * iterations + j);
                        cacheService.put(key, "value" + j);

                        String value = cacheService.getIfPresent(key);
                        System.out.println("value: " + value);
                        // 断言确保值不为 null
                        assertNotNull(value, "Value should not be null for key: " + key);
                    }
                } finally {
                    latch.countDown();
                    System.out.println(Thread.currentThread().getName() + " 执行完成");
                }
            });
        }
        latch.await();
        executor.shutdown();
    }

    @Test
    @DisplayName("测试缓存的并发无效化操作")
    void testConcurrentInvalidate() throws InterruptedException {
        int threadCount = 10;
        int iterations = 100;

        // Pre-populate cache with keys
        for (int i = 0; i < threadCount * iterations; i++) {
            cacheService.put("key" + i, "value" + i);
        }

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int threadNum = i;
            executor.submit(() -> {
                // Each thread invalidates a unique subset of keys
                for (int j = threadNum * iterations; j < (threadNum + 1) * iterations; j++) {
                    cacheService.invalidate("key" + j);
                }
                latch.countDown();
            });
        }
        latch.await();
        executor.shutdown();

        // Verify that the keys have been invalidated
        for (int i = 0; i < threadCount * iterations; i++) {
            assertNull(cacheService.getIfPresent("key" + i), "Key should be invalidated: key" + i);
        }
    }

    // --- 新增的测试方法 ---

    @Test
    @DisplayName("测试同一 key 的值覆盖")
    void testPutOverwrite() {
        cacheService.put("key1", "value1");
        assertEquals("value1", cacheService.getIfPresent("key1"));
        cacheService.put("key1", "newValue1");
        assertEquals("newValue1", cacheService.getIfPresent("key1"));
    }

    @Test
    @DisplayName("测试并发调用 get(mappingFunction) 仅计算一次")
    void testConcurrentGetWithMappingFunction() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        final String key = "concurrentMappingKey";
        final AtomicInteger counter = new AtomicInteger(0);

        Runnable task = () -> {
            try {
                // mappingFunction 增加计数器，模拟计算过程
                String value = cacheService.get(key, k -> {
                    counter.incrementAndGet();
                    return "computedValue";
                });
                assertEquals("computedValue", value);
            } finally {
                latch.countDown();
            }
        };

        for (int i = 0; i < threadCount; i++) {
            executor.submit(task);
        }
        latch.await();
        executor.shutdown();
        // 映射函数应该只被调用一次
        assertEquals(1, counter.get(), "Mapping function should be computed only once");
    }

    @Test
    @DisplayName("测试无效化不存在的 key")
    void testInvalidateNonExisting() {
        // 无效化一个不存在的 key，不应抛出异常，并保持状态不变
        cacheService.invalidate("nonExistingKey");
        assertNull(cacheService.getIfPresent("nonExistingKey"));
    }

    @Test
    @DisplayName("测试多个 key 的过期行为")
    void testExpirationMultiple() throws InterruptedException {
        // 插入多个 key
        for (int i = 0; i < 5; i++) {
            cacheService.put("key" + i, "value" + i);
        }
        // 等待超过过期时间
        TimeUnit.SECONDS.sleep(4);
        for (int i = 0; i < 5; i++) {
            assertNull(cacheService.getIfPresent("key" + i), "Key should be expired: key" + i);
        }
    }
}