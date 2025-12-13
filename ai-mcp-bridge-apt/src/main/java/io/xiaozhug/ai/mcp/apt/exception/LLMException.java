package io.xiaozhug.ai.mcp.apt.exception;

/**
 * LLM相关异常
 *
 * @author xiaozhug
 */
public class LLMException extends RuntimeException {
    
    public LLMException(String message) {
        super(message);
    }
    
    public LLMException(String message, Throwable cause) {
        super(message, cause);
    }
}