package io.xiaozhug.ai.mcp.apt.explorer;

import io.xiaozhug.ai.mcp.apt.config.ProcessorConfig;
import io.xiaozhug.ai.mcp.apt.extension.ExtensionRegistry;
import io.xiaozhug.ai.mcp.apt.extension.FieldExplorerExtension;
import io.xiaozhug.ai.mcp.apt.util.LogUtils;
import io.xiaozhug.ai.mcp.common.metadata.McpMetadataItem;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

import static io.xiaozhug.ai.mcp.apt.util.TypeNameUtils.getQualifiedName;
import static io.xiaozhug.ai.mcp.apt.util.TypeNameUtils.getTypeName;

/**
 * 字段处理器
 * <p>
 * 递归探索Java类型中的所有嵌套字段
 * </p>
 *
 * @author xiaozhug
 */
public class FieldExplorer {

    // Java核心类型（不需要深入探索）
    private static final Set<String> JAVA_CORE_TYPES = new HashSet<>(Arrays.asList(
            // 基本类型包装类
            "java.lang.Boolean", "java.lang.Byte", "java.lang.Character",
            "java.lang.Double", "java.lang.Float", "java.lang.Integer",
            "java.lang.Long", "java.lang.Short", "java.lang.String", "java.lang.Void",
            // 时间相关类
            "java.time.LocalDate", "java.time.LocalTime", "java.time.LocalDateTime",
            "java.time.ZonedDateTime", "java.util.Date", "java.sql.Date",
            "java.sql.Timestamp", "java.sql.Time", "java.time.Instant",
            // 其他
            "java.math.BigDecimal", "java.math.BigInteger"
    ));

    private final ProcessingEnvironment processingEnv;
    private final Types typeUtils;
    private final Elements elementUtils;
    private final ProcessorConfig config;
    private final ExtensionRegistry extensionRegistry;
    private final List<FieldExplorerExtension> fieldExplorerExtensions;
    private final TypeChecker typeChecker;

    // 缓存以提升性能
    private final Map<String, List<McpMetadataItem.Param.Field>> fieldCache = new HashMap<>();

    public FieldExplorer(ProcessingEnvironment processingEnv, ProcessorConfig config,
                         ExtensionRegistry extensionRegistry) {
        this.processingEnv = Objects.requireNonNull(processingEnv);
        this.typeUtils = processingEnv.getTypeUtils();
        this.elementUtils = processingEnv.getElementUtils();
        this.config = Objects.requireNonNull(config);
        this.extensionRegistry = extensionRegistry != null ? extensionRegistry : new ExtensionRegistry();
        this.fieldExplorerExtensions = extensionRegistry.getFieldExplorers();
        this.typeChecker = new TypeChecker();
    }

    /**
     * 构建字段树
     */
    public List<McpMetadataItem.Param.Field> buildFieldTree(TypeMirror type) {
        return buildFieldTreeInternal(type, new HashSet<>(), type instanceof DeclaredType ? ((DeclaredType) type).asElement() : null, 1);
    }

    /**
     * 构建字段树的内部实现
     */
    private List<McpMetadataItem.Param.Field> buildFieldTreeInternal(TypeMirror type,
                                                                     Set<String> visitedTypes,
                                                                     Element context,
                                                                     int depth) {

        String typeName = getTypeName(type);

        List<McpMetadataItem.Param.Field> fieldsCached = fieldCache.get(typeName);

        if(fieldsCached != null){
            return fieldsCached;
        }

        if (visitedTypes.contains(typeName)) {
            debugLog(() -> "检测到循环引用: " + typeName, depth);
            fieldCache.put(typeName, Collections.emptyList());
            return fieldCache.get(typeName);
        }

        visitedTypes.add(typeName);

        debugLog(() -> "处理类型: " + type + ", 类型: " + type.getKind(), depth);

        try {
            List<McpMetadataItem.Param.Field> fields = processCustomType(type,
                    (ext, customFields) -> ext.processCustomType(type, customFields));

            if (fields != null && !fields.isEmpty()) {
                fieldCache.put(typeName, fields);
                return fieldCache.get(typeName);
            }

            // 检查是否为简单类型
            boolean isComplexByExtension = checkExtensions(ext -> ext.isComplexType(type, typeName));
            if (!isComplexByExtension || !typeChecker.isComplexType(type, typeName)) {
                debugLog(() -> "简单类型，跳过: " + typeName, depth);
                fieldCache.put(typeName, Collections.emptyList());
                return fieldCache.get(typeName);
            }

            // 处理集合类型
            if (isCollectionType(type)) {
                fieldCache.put(typeName, handleCollectionType(type, visitedTypes, context, depth));
                return fieldCache.get(typeName);
            }

            DeclaredType declaredType = (DeclaredType) type;
            TypeElement typeElement = (TypeElement) declaredType.asElement();
            fieldCache.put(typeName, processClassFields(typeElement, visitedTypes, context, depth));
            return fieldCache.get(typeName);
        } finally {
            visitedTypes.remove(typeName);
        }
    }

    /**
     * 处理普通类的字段
     */
    private List<McpMetadataItem.Param.Field> processClassFields(TypeElement typeElement,
                                                                 Set<String> visitedTypes,
                                                                 Element context,
                                                                 int depth) {
        List<McpMetadataItem.Param.Field> result = getAllFields(typeElement).stream()
                .filter(field -> !field.getModifiers().contains(Modifier.STATIC))
                .filter(field -> shouldProcessField(field, typeElement))
                .map(field -> createField(field, visitedTypes, context, depth + 1))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        debugLog(() -> "找到 " + result.size() + " 个字段", depth);
        return result;
    }

    /**
     * 使用扩展点处理特定类型
     */
    private List<McpMetadataItem.Param.Field> processCustomType(
            TypeMirror type,
            BiConsumer<FieldExplorerExtension, List<McpMetadataItem.Param.Field>> processor) {

        if (fieldExplorerExtensions.isEmpty()) {
            return null;
        }

        List<McpMetadataItem.Param.Field> fields = new ArrayList<>();
        for (FieldExplorerExtension extension : fieldExplorerExtensions) {
            processor.accept(extension, fields);
            if (!fields.isEmpty()) {
                LogUtils.debug("扩展点处理: " + getTypeName(type));
                return fields;
            }
        }

        return null;
    }

    /**
     * 检查是否应该处理字段
     */
    private boolean shouldProcessField(VariableElement field, TypeElement declaringType) {
        if (fieldExplorerExtensions.isEmpty()) {
            return true;
        }

        String fieldName = field.getSimpleName().toString();
        String fieldType = field.asType().toString();
        TypeMirror declaringTypeMirror = declaringType.asType();

        return allExtensionsMatch(ext ->
                ext.shouldProcessField(fieldName, fieldType, declaringTypeMirror));
    }

    /**
     * 创建字段对象
     */
    private McpMetadataItem.Param.Field createField(VariableElement element,
                                                    Set<String> visitedTypes,
                                                    Element context,
                                                    int depth) {
        try {
            String fieldName = element.getSimpleName().toString();
            String fieldType = element.asType().toString();

            debugLog(() -> "处理字段: " + fieldName + " : " + fieldType, depth);

            McpMetadataItem.Param.Field field = new McpMetadataItem.Param.Field();
            field.setFieldName(fieldName);
            field.setFieldType(fieldType);
            field.setFieldQualifiedName(getQualifiedName(element.asType()));

            // 递归处理嵌套字段
            List<McpMetadataItem.Param.Field> nestedFields = buildFieldTreeInternal(
                    element.asType(), visitedTypes, context, depth + 1);

            if (!nestedFields.isEmpty()) {
                field.setFields(nestedFields);
            }

            extensionRegistry.getMetadataProcessors().forEach(processor ->
                    processor.afterProcessField(field));

            return field;

        } catch (Exception e) {
            LogUtils.debug("处理字段失败: " + element.getSimpleName() + ", 错误: " + e.getMessage());
            if (config.isDebugMode()) {
                e.printStackTrace();
            }
            return null;
        }
    }

    /**
     * 处理集合类型
     */
    private List<McpMetadataItem.Param.Field> handleCollectionType(TypeMirror type,
                                                                   Set<String> visitedTypes,
                                                                   Element context,
                                                                   int depth) {

        DeclaredType declaredType = (DeclaredType) type;
        List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();

        if (typeArguments.isEmpty()) {
            return Collections.emptyList();
        }

        TypeMirror elementType = typeArguments.get(0);
        debugLog(() -> "集合元素类型: " + elementType, depth);

        List<McpMetadataItem.Param.Field> elementFields = buildFieldTreeInternal(
                elementType, visitedTypes, context, depth + 1);

        if (elementFields.isEmpty()) {
            return Collections.emptyList();
        }

        // 创建表示集合元素的字段
        McpMetadataItem.Param.Field elementField = new McpMetadataItem.Param.Field();
        elementField.setFieldName("element");
        elementField.setFieldType(elementType.toString());
        elementField.setFieldQualifiedName(getQualifiedName(elementType));
        elementField.setFields(elementFields);

        extensionRegistry.getMetadataProcessors().forEach(processor ->
                processor.afterProcessField(elementField));

        return Collections.singletonList(elementField);
    }

    /**
     * 获取类的所有字段（包括继承的字段）
     */
    private List<VariableElement> getAllFields(TypeElement typeElement) {
        List<VariableElement> allFields = new ArrayList<>();
        TypeElement current = typeElement;

        while (current != null && !"java.lang.Object".equals(current.getQualifiedName().toString())) {
            current.getEnclosedElements().stream()
                    .filter(element -> element.getKind() == ElementKind.FIELD)
                    .map(element -> (VariableElement) element)
                    .forEach(allFields::add);

            // 获取父类
            TypeMirror superClass = current.getSuperclass();
            if (superClass.getKind() == TypeKind.NONE || !(superClass instanceof DeclaredType)) {
                break;
            }

            Element superElement = ((DeclaredType) superClass).asElement();
            if (!(superElement instanceof TypeElement)) {
                break;
            }

            current = (TypeElement) superElement;

            // 不处理Java核心类的父类
            if (current.getQualifiedName().toString().startsWith("java.lang.")) {
                break;
            }
        }

        return allFields;
    }

    /**
     * 判断是否为集合类型
     */
    private boolean isCollectionType(TypeMirror type) {
        if (!(type instanceof DeclaredType)) {
            return false;
        }

        DeclaredType declaredType = (DeclaredType) type;
        Element element = declaredType.asElement();

        if (!(element instanceof TypeElement)) {
            return false;
        }

        return fieldExplorerExtensions.stream().anyMatch(ext -> ext.isCollectionType(type));
    }

    /**
     * 生成缩进字符串
     */
    private String getIndent(int depth) {
        return String.join("", Collections.nCopies(depth * 4, " "));
    }

    /**
     * 获取类型缓存键
     */
    private String getTypeKey(TypeMirror type) {
        return type.toString();
    }

    /**
     * 调试日志
     */
    private void debugLog(Supplier<String> messageSupplier, int depth) {
        if (config.isDebugMode()) {
            LogUtils.debug(getIndent(depth) + messageSupplier.get());
        }
    }

    /**
     * 检查是否有扩展点匹配
     */
    private boolean checkExtensions(Predicate<FieldExplorerExtension> predicate) {
        return !fieldExplorerExtensions.isEmpty() && fieldExplorerExtensions.stream().anyMatch(predicate);
    }

    /**
     * 检查所有扩展点是否匹配
     */
    private boolean allExtensionsMatch(Predicate<FieldExplorerExtension> predicate) {
        return fieldExplorerExtensions.isEmpty() || fieldExplorerExtensions.stream().allMatch(predicate);
    }

    /**
     * 类型检查器 - 内部辅助类
     */
    private class TypeChecker {

        /**
         * 判断是否为复杂类型
         */
        boolean isComplexType(TypeMirror type, String typeName) {
            return !isSimpleType(type) && !isJavaCoreType(typeName);
        }

        /**
         * 判断是否为简单类型
         */
        private boolean isSimpleType(TypeMirror type) {
            if (type == null) {
                return true;
            }

            TypeKind kind = type.getKind();
            if (kind.isPrimitive() || kind == TypeKind.VOID) {
                return true;
            }

            //DeclaredType 代表：类、接口或枚举类型
            if (type instanceof DeclaredType) {
                DeclaredType declaredType = (DeclaredType) type;
                Element element = declaredType.asElement();
                return element != null && element.getKind() == ElementKind.ENUM;
            }

            return true;
        }

        /**
         * 判断是否为Java核心类型
         */
        private boolean isJavaCoreType(String typeName) {
            return (!fieldExplorerExtensions.isEmpty() && fieldExplorerExtensions.stream().allMatch(ext -> ext.isJavaCoreType(typeName))) ||
                    JAVA_CORE_TYPES.contains(typeName);
        }
    }
}