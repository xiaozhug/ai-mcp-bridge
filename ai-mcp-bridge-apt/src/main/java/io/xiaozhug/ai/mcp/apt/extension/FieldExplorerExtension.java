package io.xiaozhug.ai.mcp.apt.extension;

import io.xiaozhug.ai.mcp.common.metadata.McpMetadataItem;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;

/**
 * 字段探索器扩展点接口
 * 允许自定义字段探索逻辑
 *
 * @author xiaozhug
 */
public interface FieldExplorerExtension extends ConfigurableExtension{
    
    /**
     * 判断是否为复杂类型
     * 
     * @param typeMirror 类型镜像
     * @param typeName 类型名称
     * @return 是否为复杂类型
     */
    default boolean isComplexType(TypeMirror typeMirror, String typeName) {
        return true;
    }
    
    /**
     * 判断是否为Java核心类型
     * 
     * @param typeName 类型名称
     * @return 是否为Java核心类型
     */
    default boolean isJavaCoreType(String typeName) {
        return false;
    }

    /**
     * 判断是否为集合类型
     * @param type
     * @return
     */
    default boolean isCollectionType(TypeMirror type) {
        return false;
    }
    
    /**
     * 处理自定义类型
     * 
     * @param typeMirror 自定义类型
     * @param fields 字段列表（可修改）
     */
    default void processCustomType(TypeMirror typeMirror, List<McpMetadataItem.Param.Field> fields) {
    }
    
    /**
     * 检查字段是否应该被处理
     * 
     * @param fieldName 字段名
     * @param fieldType 字段类型
     * @param declaringType 声明类型
     * @return 是否应该处理
     */
    default boolean shouldProcessField(String fieldName, String fieldType, 
                                     TypeMirror declaringType) {
        return true;
    }
}