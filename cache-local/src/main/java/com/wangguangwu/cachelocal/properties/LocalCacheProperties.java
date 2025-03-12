package com.wangguangwu.cachelocal.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 本地缓存配置类，提供缓存参数的可配置性。
 * 通过 `@ConfigurationProperties` 进行自动绑定。
 * <p>
 * 配置示例：
 * cache:
 * expireAfterWrite: 10
 * expireAfterAccess: 10
 * maximumSize: 100
 *
 * @author wangguangwu
 */
@Configuration
@ConfigurationProperties(prefix = "cache")
@Getter
@Setter
public class LocalCacheProperties {

    /**
     * 缓存写入后过期时间，单位为秒
     */
    private int expireAfterWrite = 10;

    /**
     * 缓存访问后过期时间，单位为秒
     */
    private int expireAfterAccess = 10;

    /**
     * 缓存最大大小
     */
    private int maximumSize = 100;

}
