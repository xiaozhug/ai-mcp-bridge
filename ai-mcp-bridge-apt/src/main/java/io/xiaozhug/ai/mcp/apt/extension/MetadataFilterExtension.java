package io.xiaozhug.ai.mcp.apt.extension;

import io.xiaozhug.ai.mcp.common.metadata.McpMetadataItem;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.List;

/**
 * 元数据过滤器扩展点接口
 * 允许过滤和修改元数据
 *
 * @author xiaozhug
 */
public interface MetadataFilterExtension extends ConfigurableExtension{
    
    /**
     * 过滤类
     */
    default boolean shouldProcessClass(TypeElement classElement) {
        return true;
    }
    
    /**
     * 过滤方法
     */
    default boolean shouldProcessMethod(ExecutableElement methodElement, 
                                      TypeElement classElement) {
        return true;
    }
    
    /**
     * 过滤参数
     */
    default boolean shouldProcessParameter(ExecutableElement methodElement, VariableElement variableElement) {
        return true;
    }
    
    /**
     * 过滤字段
     */
    default boolean shouldProcessField(String fieldName, 
                                     String fieldType, 
                                     String parentType) {
        return true;
    }
    
    /**
     * 过滤最终元数据
     */
    default List<McpMetadataItem> filterMetadata(List<McpMetadataItem> allMetadata) {
        return allMetadata;
    }
}