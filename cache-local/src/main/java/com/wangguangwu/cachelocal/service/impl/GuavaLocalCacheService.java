package com.wangguangwu.cachelocal.service.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.wangguangwu.cachelocal.service.LocalCacheService;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 基于 Guava 实现的本地缓存
 *
 * @author wangguangwu
 */
public class GuavaLocalCacheService<K, V> implements LocalCacheService<K, V> {

    private final Cache<K, V> cache;

    public GuavaLocalCacheService() {
        cache = CacheBuilder.newBuilder()
                // 设置缓存过期时间
                .expireAfterWrite(10, TimeUnit.SECONDS)
                // 设置最大缓存大小
                .maximumSize(100)
                .build();
    }

    @Override
    public void put(K key, V value) {
        cache.put(key, value);
    }

    @Override
    public V getIfPresent(K key) {
        return cache.getIfPresent(key);
    }

    @Override
    public V get(K key, Function<? super K, ? extends V> mappingFunction) {
        return null;
    }

    @Override
    public boolean containsKey(K key) {
        return false;
    }

    @Override
    public void invalidate(K key) {

    }

    @Override
    public void invalidateAll() {

    }

    @Override
    public Set<K> keySet() {
        return Set.of();
    }

    @Override
    public int size() {
        return 0;
    }
}
