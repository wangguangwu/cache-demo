package com.wangguangwu.cachelocal.service.impl;

import com.wangguangwu.cachelocal.service.LocalCacheService;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * 基于 HashMap 实现的本地缓存
 *
 * @author wangguangwu
 */
public class MapLocalCacheService<K, V> implements LocalCacheService<K, V> {

    private final Map<K, CacheItem<V>> cache;
    private final int MAX_SIZE = 1000;  // 设置最大缓存容量
    private final long EXPIRATION_TIME = 10 * 60 * 1000; // 设置过期时间：10分钟

    public MapLocalCacheService() {
        cache = new HashMap<>();
    }

    @Override
    public void put(K key, V value) {
        if (cache.size() >= MAX_SIZE) {
            evictExpiredEntries(); // 移除过期条目
        }

        cache.put(key, new CacheItem<>(value, System.currentTimeMillis()));
    }

    @Override
    public V getIfPresent(K key) {
        CacheItem<V> item = cache.get(key);
        if (item == null) {
            return null; // 如果没有找到，返回 null
        }

        // 检查是否过期
        if (System.currentTimeMillis() - item.timestamp > EXPIRATION_TIME) {
            cache.remove(key); // 移除过期项
            return null;
        }

        return item.value;
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

    // 移除过期缓存
    private void evictExpiredEntries() {
        Iterator<Map.Entry<K, CacheItem<V>>> iterator = cache.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<K, CacheItem<V>> entry = iterator.next();
            if (System.currentTimeMillis() - entry.getValue().timestamp > EXPIRATION_TIME) {
                iterator.remove();
            }
        }
    }

    // 内部类：缓存项，用于存储缓存值和时间戳
    private static class CacheItem<V> {
        V value;
        long timestamp;

        CacheItem(V value, long timestamp) {
            this.value = value;
            this.timestamp = timestamp;
        }
    }
}
