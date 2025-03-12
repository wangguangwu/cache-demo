package com.wangguangwu.cachedistributed.service;

import java.util.Set;
import java.util.function.Function;

/**
 * 分布式缓存服务接口
 *
 * @param <K> 缓存的键类型
 * @param <V> 缓存的值类型
 * @author wangguangwu
 */
public interface DistributedCacheService<K, V> {

    /**
     * 向缓存中添加一个键值对
     *
     * @param key   键
     * @param value 值
     */
    void put(K key, V value);

    /**
     * 获取缓存中的值
     *
     * @param key 键
     * @return 缓存中的值，如果不存在则返回 null
     */
    V getIfPresent(K key);

    /**
     * 获取缓存中的值，如果不存在则计算值并存入缓存后返回
     *
     * @param key             键
     * @param mappingFunction 计算值的函数
     * @return 缓存中的值
     */
    V get(K key, Function<? super K, ? extends V> mappingFunction);

    /**
     * 判断缓存中是否存在某个 key
     *
     * @param key 键
     * @return 如果存在则返回 true，否则返回 false
     */
    boolean containsKey(K key);

    /**
     * 移除缓存中的某个键值对
     *
     * @param key 键
     */
    void invalidate(K key);

    /**
     * 移除所有缓存项
     */
    void invalidateAll();

    /**
     * 返回缓存中的所有键集合
     *
     * @return 键集合
     */
    Set<K> keySet();

    /**
     * 获取当前缓存的大小
     *
     * @return 当前缓存的大小
     */
    int size();

    /**
     * 设置缓存的过期时间
     *
     * @param key        键
     * @param expiration 过期时间（单位：毫秒）
     */
    void setExpiration(K key, long expiration);

    /**
     * 获取缓存的过期时间
     *
     * @param key 键
     * @return 缓存的过期时间（单位：毫秒）
     */
    long getExpiration(K key);
}
