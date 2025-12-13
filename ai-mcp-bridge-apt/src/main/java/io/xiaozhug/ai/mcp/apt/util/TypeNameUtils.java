package io.xiaozhug.ai.mcp.apt.util;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.HashMap;
import java.util.Map;

/**
 * 类型名称工具类
 *
 * @author xiaozhug
 */
public class TypeNameUtils {

    private static final Map<String, String> typeNameCache = new HashMap<>();

    /**
     * 获取类型名称
     */
    public static String getTypeName(TypeMirror type) {
        return type.toString();
    }

    public static String getQualifiedName(TypeMirror type){
        return typeNameCache.computeIfAbsent(type.toString(), key -> {
            if (type instanceof DeclaredType) {
                DeclaredType declaredType = (DeclaredType) type;
                Element element = declaredType.asElement();
                if (element instanceof TypeElement) {
                    return ((TypeElement) element).getQualifiedName().toString();
                }
            }
            return type.toString();
        });
    }
}
