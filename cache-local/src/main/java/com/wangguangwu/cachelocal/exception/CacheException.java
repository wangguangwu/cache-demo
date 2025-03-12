package com.wangguangwu.cachelocal.exception;

/**
 * 自定义缓存异常类，用于缓存操作时抛出特定的异常信息。
 * 继承 RuntimeException 以便在运行时抛出异常。
 *
 * @author wangguangwu
 */
@SuppressWarnings("unused")
public class CacheException extends RuntimeException {

    // 默认构造函数
    public CacheException() {
        super();
    }

    // 带消息构造函数
    public CacheException(String message) {
        super(message);
    }

    // 带消息和原始异常构造函数
    public CacheException(String message, Throwable cause) {
        super(message, cause);
    }

    // 带原始异常构造函数
    public CacheException(Throwable cause) {
        super(cause);
    }
}
