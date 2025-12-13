package io.xiaozhug.ai.mcp.apt.extension;

import io.xiaozhug.ai.mcp.common.metadata.McpMetadataItem;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.List;

/**
 * 元数据处理器扩展点接口
 * 允许外部扩展自定义的元数据处理逻辑
 *
 * @author xiaozhug
 */
public interface MetadataProcessorExtension extends ConfigurableExtension{
    
    /**
     * 在处理类之后调用
     */
    default void afterProcessClass(TypeElement classElement, 
                                  List<McpMetadataItem> mcpMetadataItems) {
    }
    
    /**
     * 在处理方法后调用
     */
    default void afterProcessMethod(TypeElement classElement,
                                    ExecutableElement methodElement,
                                    McpMetadataItem mcpMetadataItem) {
    }
    
    /**
     * 自定义参数处理
     */
    default void afterProcessParameter(ExecutableElement methodElement,
                                       McpMetadataItem mcpMetadataItem,
                                       McpMetadataItem.Param param) {
    }

    /**
     * 自定义字段处理
     */
    default void afterProcessField(McpMetadataItem.Param.Field field) {
    }
}