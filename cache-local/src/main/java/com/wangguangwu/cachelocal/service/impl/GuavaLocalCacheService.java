package com.wangguangwu.cachelocal.service.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.wangguangwu.cachelocal.exception.CacheException;
import com.wangguangwu.cachelocal.properties.LocalCacheProperties;
import com.wangguangwu.cachelocal.service.LocalCacheService;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 基于 Guava 实现的本地缓存服务。
 * 本服务提供了常见的缓存操作，如放入缓存、获取缓存、判断缓存是否存在、缓存清除等。
 * 支持缓存的过期策略和最大容量限制。
 *
 * @param <K> 缓存的键类型
 * @param <V> 缓存的值类型
 * @author wangguangwu
 */
public class GuavaLocalCacheService<K, V> implements LocalCacheService<K, V> {

    private final Cache<K, V> cache;

    /**
     * GuavaLocalCacheService 构造方法，配置缓存策略。
     * 通过 Guava 的构建器设置缓存的过期时间、访问后过期时间和最大缓存大小等策略。
     *
     * @param localCacheProperties 缓存配置属性，包含过期时间、访问过期时间和最大缓存容量
     */
    public GuavaLocalCacheService(LocalCacheProperties localCacheProperties) {
        if (localCacheProperties.getMaximumSize() <= 0) {
            throw new IllegalArgumentException("Maximum cache size must be greater than 0");
        }

        cache = CacheBuilder.newBuilder()
                .expireAfterWrite(localCacheProperties.getExpireAfterWrite(), TimeUnit.SECONDS)
                .expireAfterAccess(localCacheProperties.getExpireAfterAccess(), TimeUnit.SECONDS)
                .maximumSize(localCacheProperties.getMaximumSize())
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
     * @throws CacheException 如果缓存操作失败，则抛出自定义缓存异常
     */
    @Override
    public V get(K key, Function<? super K, ? extends V> mappingFunction) {
        try {
            return cache.get(key, () -> mappingFunction.apply(key));
        } catch (Exception e) {
            throw new CacheException("Error loading value for key: " + key, e);
        }
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
        return (int) cache.size();
    }
}
