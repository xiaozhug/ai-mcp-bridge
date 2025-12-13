package io.xiaozhug.ai.mcp.apt.exception;

/**
 * 存储相关异常
 *
 * @author xiaozhug
 */
public class StoreException extends RuntimeException {
    
    public StoreException(String message) {
        super(message);
    }
    
    public StoreException(String message, Throwable cause) {
        super(message, cause);
    }
}