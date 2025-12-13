package io.xiaozhug.ai.mcp.apt.extension;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import java.util.*;

import static io.xiaozhug.ai.mcp.apt.util.TypeNameUtils.getQualifiedName;

/**
 * 默认字段探索扩展点实现
 *
 * @author xiaozhug
 */
public class DefaultFieldExplorerExtension implements FieldExplorerExtension {

    private static final Set<String> PRIMITIVE_TYPE_NAMES = new HashSet<>(Arrays.asList(
            ObjectNode.class.getTypeName(),
            ArrayNode.class.getTypeName()
    ));

    @Override
    public boolean isComplexType(TypeMirror typeMirror, String typeName) {
        String mapInterface = Map.class.getName();
        return !PRIMITIVE_TYPE_NAMES.contains(typeName) && !isSubtypeOf(typeMirror, mapInterface);
    }

    @Override
    public boolean isCollectionType(TypeMirror type) {
        if (type == null) {
            return false;
        }

        String collectionInterface = Collection.class.getName();

        // 检查是否是 Collection 或 Map 的子类型
        return isSubtypeOf(type, collectionInterface);
    }


    private boolean isSubtypeOf(TypeMirror type, String targetTypeName) {
        // 遍历类型的所有父类和接口
        while (type != null && !type.getKind().isPrimitive()) {
            String currentTypeName = getQualifiedName(type);
            if (currentTypeName.equals(targetTypeName)) {
                return true;
            }

            // 检查父类
            if (type instanceof DeclaredType) {
                DeclaredType declaredType = (DeclaredType) type;
                Element element = declaredType.asElement();
                if (element instanceof TypeElement) {
                    TypeElement typeElement = (TypeElement) element;

                    // 检查父类
                    TypeMirror superClass = typeElement.getSuperclass();
                    if (superClass != null && isSubtypeOf(superClass, targetTypeName)) {
                        return true;
                    }

                    // 检查接口
                    for (TypeMirror interfaceType : typeElement.getInterfaces()) {
                        if (isSubtypeOf(interfaceType, targetTypeName)) {
                            return true;
                        }
                    }
                }
            }
            break;
        }
        return false;
    }
}
