package io.xiaozhug.ai.mcp.apt.extension;

import java.util.Map;

/**
 * 可配置的扩展点接口
 *
 * @author xiaozhug
 */
public interface ConfigurableExtension {
    
    /**
     * 配置扩展点
     */
    default void configure(Map<String, Object> properties){}
    
    /**
     * 获取扩展点名称
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }
    
    /**
     * 扩展点是否启用
     */
    default boolean isEnabled() {
        return true;
    }
    
    /**
     * 设置启用状态
     */
    default void setEnabled(boolean enabled) {
        // 默认实现，具体扩展点可以覆盖
    }
    
    /**
     * 获取执行顺序（值越小优先级越高）
     */
    default int getOrder() {
        return Integer.MAX_VALUE;
    }
}