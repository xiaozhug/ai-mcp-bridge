package io.xiaozhug.ai.mcp.apt.exception;

/**
 * 注解处理器异常
 *
 * @author xiaozhug
 */
public class ProcessorException extends RuntimeException {
    
    public ProcessorException(String message) {
        super(message);
    }
    
    public ProcessorException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ProcessorException(Throwable cause) {
        super(cause);
    }
}