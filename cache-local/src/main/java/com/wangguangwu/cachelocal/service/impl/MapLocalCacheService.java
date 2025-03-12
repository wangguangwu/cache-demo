package com.wangguangwu.cachelocal.service.impl;

import com.wangguangwu.cachelocal.properties.LocalCacheProperties;
import com.wangguangwu.cachelocal.service.LocalCacheService;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

/**
 * 基于 HashMap 实现的本地缓存服务
 *
 * @param <K> 缓存的键类型
 * @param <V> 缓存的值类型
 * @author wangguangwu
 */
public class MapLocalCacheService<K, V> implements LocalCacheService<K, V> {

    // 缓存数据存储，键值对形式
    private final Map<K, CacheItem<V>> cache;

    // 最大缓存容量
    private final int maxSize;

    // 缓存过期时间（毫秒）
    private final int expirationTime;

    // ReadWriteLock for thread safety
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final ThreadLocal<Boolean> isReadLocked = ThreadLocal.withInitial(() -> false);

    /**
     * 构造方法，初始化缓存配置。
     *
     * @param localCacheProperties 缓存配置属性，包含最大容量和过期时间
     */
    public MapLocalCacheService(LocalCacheProperties localCacheProperties) {
        // 初始化缓存容器，使用 LinkedHashMap 保持插入顺序
        this.cache = new LinkedHashMap<>(localCacheProperties.getMaximumSize(), 0.75f, true);
        // 从配置中获取最大缓存容量
        this.maxSize = localCacheProperties.getMaximumSize();
        // 获取过期时间并转换为毫秒
        this.expirationTime = localCacheProperties.getExpireAfterWrite() * 1000;
    }

    @Override
    public void put(K key, V value) {
        lock.writeLock().lock();
        try {
            // 如果缓存已满
            if (cache.size() >= maxSize) {
                // 移除过期条目以腾出空间
                evictExpiredEntries();
            }

            // 存入缓存并记录时间戳
            cache.put(key, new CacheItem<>(value, System.currentTimeMillis()));
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public V getIfPresent(K key) {
        lock.readLock().lock();
        try {
            // 从缓存中获取缓存项
            CacheItem<V> item = cache.get(key);
            if (item == null) {
                // 如果没有找到，返回 null
                return null;
            }

            // 检查是否过期
            if (System.currentTimeMillis() - item.timestamp > expirationTime) {
                // 如果缓存过期
                // 移除过期项
                cache.remove(key);
                // 返回 null
                return null;
            }

            // 返回缓存中的值
            return item.value;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public V get(K key, Function<? super K, ? extends V> mappingFunction) {
        lock.readLock().lock();
        try {
            // 标记当前线程已持有读锁
            isReadLocked.set(true);
            // 从缓存中获取缓存项
            CacheItem<V> item = cache.get(key);

            // 如果缓存中没有该键，则使用 mappingFunction 生成新的值并放入缓存
            if (item == null) {
                // 计算值
                V value = mappingFunction.apply(key);
                // 将计算的值存入缓存
                // 释放读锁
                lock.readLock().unlock();
                // 清除读锁标记
                isReadLocked.set(false);
                put(key, value);
                // 返回计算的值
                return value;
            }

            // 如果缓存项存在，则检查是否过期
            if (System.currentTimeMillis() - item.timestamp > expirationTime) {
                // 如果缓存过期，移除该项并返回 null
                cache.remove(key);
                return null;
            }

            // 返回缓存中的值
            return item.value;
        } finally {
            // 检查当前线程是否持有读锁，如果持有则释放它
            if (Boolean.TRUE.equals(isReadLocked.get())) {
                // 只有持有锁时才释放
                lock.readLock().unlock();
            }
            // 清理线程标记
            isReadLocked.remove();
        }
    }

    @Override
    public boolean containsKey(K key) {
        // 判断缓存中是否包含指定的键
        return cache.containsKey(key);
    }

    @Override
    public void invalidate(K key) {
        lock.writeLock().lock();
        try {
            // 移除指定的缓存项
            cache.remove(key);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void invalidateAll() {
        lock.writeLock().lock();
        try {
            // 清空所有缓存项
            cache.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Set<K> keySet() {
        // 返回缓存中所有的键
        return cache.keySet();
    }

    @Override
    public int size() {
        // 返回缓存的大小
        return cache.size();
    }

    // 移除过期缓存
    private void evictExpiredEntries() {
        // 移除过期缓存项
        cache.entrySet().removeIf(entry -> System.currentTimeMillis() - entry.getValue().timestamp > expirationTime);
        // 如果缓存超出最大容量，移除最早的缓存项
        if (cache.size() >= maxSize) {
            Iterator<Map.Entry<K, CacheItem<V>>> iterator = cache.entrySet().iterator();
            if (iterator.hasNext()) {
                iterator.next();
                iterator.remove();
            }
        }
    }

    // 内部类：缓存项，用于存储缓存值和时间戳
    private static class CacheItem<V> {
        // 缓存的值
        V value;
        // 缓存项的时间戳
        long timestamp;

        CacheItem(V value, long timestamp) {
            this.value = value;
            this.timestamp = timestamp;
        }
    }
}
