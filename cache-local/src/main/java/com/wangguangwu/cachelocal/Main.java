package com.wangguangwu.cachelocal;

import com.wangguangwu.cachelocal.properties.LocalCacheProperties;
import com.wangguangwu.cachelocal.service.impl.MapLocalCacheService;
import org.junit.jupiter.api.BeforeAll;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author wangguangwu
 */
public class Main {

    public static void main(String[] args) throws InterruptedException {
        // 初始化缓存配置
        LocalCacheProperties localCacheProperties = new LocalCacheProperties();
        localCacheProperties.setMaximumSize(2);
        localCacheProperties.setExpireAfterWrite(3);

        // 初始化缓存服务，作为所有测试共享的实例
        MapLocalCacheService<String, String> cacheService = new MapLocalCacheService<>(localCacheProperties);


        int threadCount = 5;
        int iterations = 1;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int threadNum = i;
            executor.submit(() -> {
                System.out.println(Thread.currentThread().getName() + "开始执行");
                for (int j = 0; j < iterations; j++) {
                    String key = "concurrentKey_" + (threadNum * iterations + j);
                    // TODO：并发环境下，如果同时获得了写锁和读锁，是否就死锁了、
                    cacheService.put(key, "value" + j);
                    System.out.println(Thread.currentThread().getName() + "放入值");

                    String value = cacheService.getIfPresent(key);
                    System.out.println(Thread.currentThread().getName() + "获取值");
                }
                System.out.println(Thread.currentThread().getId() + "-countdown");
                latch.countDown();
                System.out.println(Thread.currentThread().getName() + "执行完成");
            });
        }
        latch.await();
        executor.shutdown();
    }
}
