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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author wangguangwu
 */
@SpringBootTest
class GuavaCacheServiceTest {

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add(CacheTypeConstants.LOCAL_CACHE_TYPE, () -> CacheTypeConstants.GUAVA);
    }

    @Autowired
    private LocalCacheService<Object, Object> localCacheService;

    @Test
    @DisplayName("测试 Guava 缓存的 put 和 get 方法")
    void testCachePutAndGet() {
        localCacheService.put("key1", "value1");
        Object value = localCacheService.getIfPresent("key1");
        System.out.println("已插入 key1 -> value1");
        System.out.println("获取到的值: " + value);
        assertEquals("value1", value);
    }

    @Test
    @DisplayName("测试 Guava 缓存的过期策略")
    void testCacheExpiration() throws InterruptedException {
        localCacheService.put("key2", "value2");
        System.out.println("已插入 key2 -> value2");

        // 立即获取应该存在
        Object value = localCacheService.getIfPresent("key2");
        System.out.println("过期前获取到的值: " + value);
        assertEquals("value2", value);

        // 等待超过缓存的过期时间
        TimeUnit.SECONDS.sleep(11);

        // 过期后应返回 null
        value = localCacheService.getIfPresent("key2");
        System.out.println("过期后获取到的值（应为 null）: " + value);
        assertNull(value);
    }

    @Test
    @DisplayName("测试 Guava 缓存的最大容量限制")
    void testCacheSizeLimit() {
        // 添加超过容量限制的元素
        for (int i = 0; i < 1100; i++) {
            localCacheService.put("key" + i, "value" + i);
        }
        System.out.println("已向缓存中插入 1100 个元素");

        // 由于 Guava 配置的最大容量是 1000，最早的应被淘汰
        Object value = localCacheService.getIfPresent("key0");
        System.out.println("检查最早的条目是否被淘汰: " + value);
        assertNull(value);

        // 最新的应该还存在
        value = localCacheService.getIfPresent("key1099");
        System.out.println("检查最新的条目是否存在: " + value);
        assertEquals("value1099", value);
    }
}