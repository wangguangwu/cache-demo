package com.wangguangwu.cachelocal.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.wangguangwu.cachelocal.properties.LocalCacheProperties;
import com.wangguangwu.cachelocal.service.LocalCacheService;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 基于 Caffeine 实现的本地缓存服务。
 * 该服务提供了常见的缓存操作，如放入缓存、获取缓存、判断缓存是否存在、缓存清除等。
 * 支持缓存的过期策略和最大容量限制。
 *
 * @author wangguangwu
 * @param <K> 缓存的键类型
 * @param <V> 缓存的值类型
 */
public class CaffeineLocalCacheService<K, V> implements LocalCacheService<K, V> {

    private final Cache<K, V> cache;

    /**
     * 构造方法，配置缓存策略。
     * 使用 Caffeine 的构建器来设置缓存过期时间、访问后过期时间、最大缓存大小等。
     * 配置了键弱引用、值软引用以及启用了缓存统计，确保在内存压力下缓存能被合理回收。
     *
     * @param localCacheProperties 缓存配置属性，包含过期时间、访问过期时间和最大缓存容量
     */
    public CaffeineLocalCacheService(LocalCacheProperties localCacheProperties) {
        // 检查最大缓存大小是否大于0，确保配置有效
        if (localCacheProperties.getMaximumSize() <= 0) {
            throw new IllegalArgumentException("Maximum cache size must be greater than 0");
        }

        cache = Caffeine.newBuilder()
                // expireAfterWrite: 指定缓存项在写入后多久过期。这里使用 localCacheProperties.getExpireAfterWrite() 指定秒数。
                .expireAfterWrite(localCacheProperties.getExpireAfterWrite(), TimeUnit.SECONDS)
                // expireAfterAccess: 指定缓存项在最后一次访问后多久过期。这里使用 localCacheProperties.getExpireAfterAccess() 指定秒数。
                .expireAfterAccess(localCacheProperties.getExpireAfterAccess(), TimeUnit.SECONDS)
                // maximumSize: 指定缓存的最大容量。当缓存项数量超过该值时，Caffeine 会根据一定的策略进行回收。
                .maximumSize(localCacheProperties.getMaximumSize())
                // weakKeys: 使用弱引用存储键，这样在内存不足时，键可以被垃圾回收，从而释放缓存空间。
                .weakKeys()
                // softValues: 使用软引用存储值，这样在内存不足时，值可以被垃圾回收，从而释放缓存空间。
                .softValues()
                // recordStats: 启用缓存统计功能，用于监控缓存的命中率和其他性能指标。
                .recordStats()
                .build();
    }

    /**
     * 将指定的键值对放入缓存中。
     *
     * @param key   要缓存的键
     * @param value 要缓存的值
     */
    @Override
    public void put(K key, V value) {
        cache.put(key, value);
    }

    /**
     * 获取指定键的缓存值，如果不存在则返回 null。
     *
     * @param key 要获取的键
     * @return 对应的缓存值，可能为 null
     */
    @Override
    public V getIfPresent(K key) {
        return cache.getIfPresent(key);
    }

    /**
     * 如果 key 存在，则返回对应的值；如果 key 不存在，则使用提供的 mappingFunction 计算并存入缓存后返回。
     *
     * @param key             键
     * @param mappingFunction 计算值的方法
     * @return 缓存中的值
     */
    @Override
    public V get(K key, Function<? super K, ? extends V> mappingFunction) {
        return cache.get(key, mappingFunction);
    }

    /**
     * 判断缓存中是否存在指定的 key。
     *
     * @param key 要检查的键
     * @return 如果 key 存在，则返回 true，否则返回 false
     */
    @Override
    public boolean containsKey(K key) {
        return cache.asMap().containsKey(key);
    }

    /**
     * 从缓存中移除指定的键。
     *
     * @param key 要移除的键
     */
    @Override
    public void invalidate(K key) {
        cache.invalidate(key);
    }

    /**
     * 清空所有缓存。
     */
    @Override
    public void invalidateAll() {
        cache.invalidateAll();
    }

    /**
     * 返回缓存中的所有键。
     *
     * @return 缓存中的键集合
     */
    @Override
    public Set<K> keySet() {
        return cache.asMap().keySet();
    }

    /**
     * 获取当前缓存的大小。
     *
     * @return 缓存中的元素数量
     */
    @Override
    public int size() {
        return (int) cache.estimatedSize();
    }
}
