package com.wangguangwu.cachelocal.service.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.wangguangwu.cachelocal.service.LocalCacheService;
import org.springframework.beans.factory.annotation.Value;

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

    /**
     * 缓存写入后过期时间，单位为秒
     */
    @Value("${cache.expireAfterWrite:10}")
    private long expireAfterWrite;

    /**
     * 缓存访问后过期时间，单位为秒
     */
    @Value("${cache.expireAfterAccess:10}")
    private long expireAfterAccess;

    /**
     * 缓存最大大小
     */
    @Value("${cache.maximumSize:100}")
    private long maximumSize;

    /**
     * GuavaLocalCacheService 构造方法，配置缓存策略。
     * 通过 Guava 的构建器设置缓存的过期时间、访问后过期时间和最大缓存大小等策略。
     * <p>
     * - expireAfterWrite: 设置数据写入缓存后多长时间过期，单位为秒。
     * - expireAfterAccess: 设置数据最后一次访问后多长时间过期，单位为秒。
     * - maximumSize: 限制缓存的最大容量，超过此大小时，最少使用的缓存项将被清除。
     */
    public GuavaLocalCacheService() {

        // 确保注入的参数正确，否则会使用默认值
        System.out.println("expireAfterWrite: " + expireAfterWrite + " seconds");
        System.out.println("expireAfterAccess: " + expireAfterAccess + " seconds");
//        cache = CacheBuilder.newBuilder()
//                .expireAfterWrite(expireAfterWrite, TimeUnit.MINUTES)
//                .expireAfterAccess(expireAfterAccess, TimeUnit.MINUTES)
//                .maximumSize(maximumSize)
//                .build();
        // TODO： 问题出在参数注入上，我不是设定了默认值吗
        cache = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .expireAfterAccess(1, TimeUnit.MINUTES)
                .maximumSize(100)
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
        // TODO: 为什么 cache 中的 localCache 是空的
    }

    /**
     * 获取指定键的缓存值，如果不存在则返回 null。
     *
     * @param key 要获取的键
     * @return 对应的缓存值
     */
    @Override
    public V getIfPresent(K key) {
        // TODO：那么上面 put 之后，get 却拿不到数据
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
        try {
            return cache.get(key, () -> mappingFunction.apply(key));
        } catch (Exception e) {
            throw new RuntimeException("Error loading value for key " + key, e);
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
