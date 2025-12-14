package io.xiaozhug.ai.mcp.apt.explorer;

import io.xiaozhug.ai.mcp.apt.collector.MetadataCollector;
import io.xiaozhug.ai.mcp.apt.config.ProcessorConfig;
import io.xiaozhug.ai.mcp.apt.extension.ExtensionRegistry;
import io.xiaozhug.ai.mcp.apt.extension.MetadataFilterExtension;
import io.xiaozhug.ai.mcp.apt.extension.MetadataProcessorExtension;
import io.xiaozhug.ai.mcp.apt.extension.TemplateGeneratorExtension;
import io.xiaozhug.ai.mcp.apt.util.LogUtils;
import io.xiaozhug.ai.mcp.apt.util.RequestTemplateInfoBuilder;
import io.xiaozhug.ai.mcp.common.metadata.McpMetadataItem;
import io.xiaozhug.ai.mcp.common.metadata.RequestTemplateInfo;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Elements;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.xiaozhug.ai.mcp.apt.util.TypeNameUtils.getQualifiedName;

/**
 * @author xiaozhug
 */
public class MethodExplorer {

    private static final String REQUEST_MAPPING = "org.springframework.web.bind.annotation.RequestMapping";
    private static final String GET_MAPPING = "org.springframework.web.bind.annotation.GetMapping";
    private static final String POST_MAPPING = "org.springframework.web.bind.annotation.PostMapping";
    private static final String PUT_MAPPING = "org.springframework.web.bind.annotation.PutMapping";
    private static final String DELETE_MAPPING = "org.springframework.web.bind.annotation.DeleteMapping";
    private static final String PATCH_MAPPING = "org.springframework.web.bind.annotation.PatchMapping";

    private final ProcessingEnvironment processingEnv;
    private final Elements elementUtils;
    private final ProcessorConfig config;
    private final ExtensionRegistry extensionRegistry;
    private final FieldExplorer fieldExplorer;

    public MethodExplorer(ProcessingEnvironment processingEnv, ProcessorConfig config, ExtensionRegistry extensionRegistry) {
        this.processingEnv = processingEnv;
        this.elementUtils = processingEnv.getElementUtils();
        this.config = config;
        this.extensionRegistry = extensionRegistry != null ? extensionRegistry : new ExtensionRegistry();
        this.fieldExplorer = new FieldExplorer(processingEnv, config, extensionRegistry);
    }

    /**
     * 处理方法
     */
    public McpMetadataItem processMethod(ExecutableElement methodElement,
                                  TypeElement classElement,
                                  MetadataCollector metadataCollector) {
        // 1. 检查是否有@RequestMapping或其派生注解
        AnnotationMirror requestMapping = findRequestMappingAnnotation(methodElement);
        if (requestMapping == null) {
            return null;
        }

        // 2. 应用方法级别过滤器
        if (!applyMethodFilters(methodElement, classElement)) {
            LogUtils.debug("过滤器排除方法: " +
                    classElement.getQualifiedName() + "." + methodElement.getSimpleName());
            return null;
        }

        LogUtils.debug("");
        LogUtils.debug("处理方法: " + classElement.getQualifiedName() + "." +
                methodElement.getSimpleName());

        // 3. 创建元数据项
        McpMetadataItem mcpMetadataItem = createItemMetadata(methodElement, classElement);

        // 4. 设置类名和方法名
        mcpMetadataItem.setClassName(classElement.getQualifiedName().toString());
        mcpMetadataItem.setMethodName(methodElement.getSimpleName().toString());

        // 5. 处理方法参数
        processMethodParameters(methodElement, mcpMetadataItem);

        // 6. 生成请求模板信息
        RequestTemplateInfo requestTemplateInfo = createRequestTemplateInfo(classElement, methodElement, requestMapping);

        if (requestTemplateInfo != null) {
            mcpMetadataItem.setRequestTemplateInfo(requestTemplateInfo);
        }

        extensionRegistry.getMetadataProcessors().forEach(
                processor -> processor.afterProcessMethod(classElement, methodElement, mcpMetadataItem)
        );

        // 8. 添加到收集器
        metadataCollector.add(classElement, methodElement, mcpMetadataItem);

        return mcpMetadataItem;
    }

    /**
     * 查找RequestMapping注解
     */
    private AnnotationMirror findRequestMappingAnnotation(ExecutableElement methodElement) {
        for (AnnotationMirror annotation : methodElement.getAnnotationMirrors()) {
            DeclaredType annotationType = annotation.getAnnotationType();
            if (annotationType != null) {
                String annotationName = annotationType.toString();
                if (isRequestMappingAnnotation(annotationName)) {
                    return annotation;
                }
            }
        }
        return null;
    }

    /**
     * 应用方法级别过滤器
     */
    private boolean applyMethodFilters(ExecutableElement methodElement, TypeElement classElement) {
        for (MetadataFilterExtension filter : extensionRegistry.getMetadataFilters()) {
            if (!filter.shouldProcessMethod(methodElement, classElement)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 创建元数据项
     */
    private McpMetadataItem createItemMetadata(ExecutableElement methodElement, TypeElement classElement) {
        McpMetadataItem mcpMetadataItem = new McpMetadataItem();

        // 设置JavaDoc（如果有）
//        String methodDoc = typeProcessor.getJavaDoc(methodElement);
//        if (methodDoc != null) {
//            mcpMetadataItem.setMethodNameDescription(methodDoc);
//        }
//
//        // 设置类文档（如果有）
//        String classDoc = typeProcessor.getJavaDoc(classElement);
//        if (classDoc != null && mcpMetadataItem.getMethodNameDescription() == null) {
//            // 如果没有方法文档，使用类文档作为后备
//            mcpMetadataItem.setMethodNameDescription(classDoc);
//        }

        return mcpMetadataItem;
    }

    /**
     * 处理方法参数
     */
    private void processMethodParameters(ExecutableElement methodElement, McpMetadataItem mcpMetadataItem) {
        List<? extends VariableElement> parameters = methodElement.getParameters();
        if (parameters == null || parameters.isEmpty()) {
            return;
        }

        List<MetadataProcessorExtension> metadataProcessors = extensionRegistry.getMetadataProcessors();
        List<MetadataFilterExtension> metadataFilters = extensionRegistry.getMetadataFilters();

        List<McpMetadataItem.Param> params = parameters.stream()
                .filter(param -> {
                    // 应用参数级别过滤器
                    for (MetadataFilterExtension filter : metadataFilters) {
                        if (!filter.shouldProcessParameter(methodElement, param)) {
                            return false;
                        }
                    }
                    return true;
                })
                .map(param -> {
                    McpMetadataItem.Param paramMeta = new McpMetadataItem.Param();

                    // 设置参数名和类型
                    paramMeta.setParamName(param.getSimpleName().toString());
                    paramMeta.setParamType(param.asType().toString());
                    paramMeta.setParamQualifiedName(getQualifiedName(param.asType()));

                    // 设置参数文档（如果有）
//                String paramDoc = typeProcessor.getJavaDoc(param);
//                if (paramDoc != null) {
//                    paramMeta.setParamNameDescription(paramDoc);
//                }

                    // 探索嵌套字段
                    List<McpMetadataItem.Param.Field> fields = fieldExplorer.buildFieldTree(param.asType());
                    if (!fields.isEmpty()) {
                        paramMeta.setFields(fields);
                    }

                    metadataProcessors.forEach(
                            processor -> processor.afterProcessParameter(methodElement, mcpMetadataItem, paramMeta)
                    );

                    return paramMeta;
                })
                .collect(Collectors.toList());

        mcpMetadataItem.setParams(params);
    }

    /**
     * 创建请求模板信息
     */
    private RequestTemplateInfo createRequestTemplateInfo(
            TypeElement classElement,
            ExecutableElement methodElement,
            AnnotationMirror requestMapping) {

        try {
            Map<String, Object> context = createTemplateContext(classElement, methodElement);

            List<TemplateGeneratorExtension> templateGenerators = extensionRegistry.getTemplateGenerators();

            for (TemplateGeneratorExtension generator : templateGenerators) {
                // 检查扩展点是否启用
                if (!generator.isEnabled()) {
                    continue;
                }

                context = generator.beforeGenerateRequestTemplate(classElement, methodElement, context);
            }

            // 生成基本模板信息
            RequestTemplateInfo templateInfo =
                    RequestTemplateInfoBuilder.createRequestTemplateInfo(classElement, methodElement, requestMapping);

            if (templateInfo == null) {
                return null;
            }

            for (TemplateGeneratorExtension generator : templateGenerators) {
                // 检查扩展点是否启用
                if (!generator.isEnabled()) {
                    continue;
                }

                generator.afterGenerateRequestTemplate(classElement, methodElement, templateInfo);
            }

            return templateInfo;

        } catch (Exception e) {
            LogUtils.debug("生成请求模板失败: " + e.getMessage());
            if (config.isDebugMode()) {
                e.printStackTrace();
            }
            return null;
        }
    }

    /**
     * 创建模板上下文
     */
    private Map<String, Object> createTemplateContext(TypeElement classElement,
                                                      ExecutableElement methodElement) {
        Map<String, Object> context = new HashMap<>();
        context.put("classElement", classElement);
        context.put("methodElement", methodElement);
        context.put("className", classElement.getQualifiedName().toString());
        context.put("methodName", methodElement.getSimpleName().toString());
        context.put("timestamp", System.currentTimeMillis());
        return context;
    }

    /**
     * 检查是否为RequestMapping注解
     */
    private boolean isRequestMappingAnnotation(String annotationName) {
        return REQUEST_MAPPING.equals(annotationName) ||
                GET_MAPPING.equals(annotationName) ||
                POST_MAPPING.equals(annotationName) ||
                PUT_MAPPING.equals(annotationName) ||
                DELETE_MAPPING.equals(annotationName) ||
                PATCH_MAPPING.equals(annotationName);
    }
}
