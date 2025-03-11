package com.wangguangwu.cachelocal.service.impl;

import com.wangguangwu.cachelocal.service.LocalCacheService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author wangguangwu
 */
@SpringBootTest
@TestPropertySource(properties = "local.cache.type=map")
class MapCacheServiceTest {

    @Autowired
    private LocalCacheService<Object, Object> localCacheService;

    @Test
    public void testCachePutAndGet() {
        localCacheService.put("key3", "value3");
        Object value = localCacheService.getIfPresent("key3");
        assertEquals("value3", value);
    }
}