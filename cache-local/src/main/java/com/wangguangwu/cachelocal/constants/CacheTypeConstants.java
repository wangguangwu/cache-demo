package com.wangguangwu.cachelocal.constants;

/**
 * 常量类，用于定义缓存类型的常量。
 * <p>
 * 该类包含缓存类型常量，便于在应用程序中进行缓存配置和选择。
 * 不允许实例化该类。
 * </p>
 *
 * @author wangguangwu
 */
public final class CacheTypeConstants {

    /**
     * 本地缓存类型的配置键
     */
    public static final String LOCAL_CACHE_TYPE = "local.cache.type";

    /**
     * Guava 缓存类型
     */
    public static final String GUAVA = "guava";

    /**
     * Caffeine 缓存类型
     */
    public static final String CAFFEINE = "caffeine";

    /**
     * Map 类型缓存
     */
    public static final String MAP = "map";

    // 私有构造方法，防止实例化
    private CacheTypeConstants() {
        throw new UnsupportedOperationException("This is a constants class and cannot be instantiated");
    }
}
