package io.xiaozhug.ai.mcp.apt.exception;

/**
 * 类型处理异常
 *
 * @author xiaozhug
 */
public class TypeProcessingException extends RuntimeException {
    
    public TypeProcessingException(String message) {
        super(message);
    }
    
    public TypeProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}