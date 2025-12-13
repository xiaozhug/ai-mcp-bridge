package io.xiaozhug.ai.mcp.apt.explorer;

import io.xiaozhug.ai.mcp.apt.collector.MetadataCollector;
import io.xiaozhug.ai.mcp.apt.config.ProcessorConfig;
import io.xiaozhug.ai.mcp.apt.extension.*;
import io.xiaozhug.ai.mcp.apt.util.LogUtils;
import io.xiaozhug.ai.mcp.common.metadata.McpMetadataItem;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Elements;
import java.util.ArrayList;
import java.util.List;

/**
 * 类处理器
 * <p>
 * 负责处理单个RestController类，提取方法和参数信息
 * </p>
 *
 * @author xiaozhug
 */
public class ClassExplorer {
    
    // Spring注解
    private static final String REST_CONTROLLER = "org.springframework.web.bind.annotation.RestController";

    private final ProcessingEnvironment processingEnv;
    private final Elements elementUtils;
    private final ProcessorConfig config;
    private final ExtensionRegistry extensionRegistry;
    private final MethodExplorer methodExplorer;

    public ClassExplorer(ProcessingEnvironment processingEnv, ProcessorConfig config, ExtensionRegistry extensionRegistry) {
        this.processingEnv = processingEnv;
        this.elementUtils = processingEnv.getElementUtils();
        this.config = config;
        this.extensionRegistry = extensionRegistry != null ? extensionRegistry : new ExtensionRegistry();
        this.methodExplorer = new MethodExplorer(processingEnv, config, extensionRegistry);
    }
    
    /**
     * 处理RestController类
     * 
     * @param typeElement 类元素
     * @param metadataCollector 元数据收集器
     * @return 是否处理成功
     */
    public boolean processClass(TypeElement typeElement, MetadataCollector metadataCollector, int processedCount) {
        if (typeElement == null || metadataCollector == null) {
            return false;
        }

        // 1. 检查是否为RestController
        if (!hasRestControllerAnnotation(typeElement)) {
            LogUtils.debug("类 " + typeElement.getQualifiedName() + " 不是RestController，跳过");
            return false;
        }

        // 2. 检查是否在目标包中
        if (!isInTargetPackage(typeElement)) {
            LogUtils.debug("类 " + typeElement.getQualifiedName() + " 不在目标包中，跳过");
            return false;
        }

        // 3. 应用过滤器
        if (!applyClassFilters(typeElement)) {
            LogUtils.debug("过滤器排除类: " + typeElement.getQualifiedName());
            return false;
        }

        LogUtils.debug("----------------------------------------");
        LogUtils.debug("处理第[" + (processedCount + 1) + "]RestController类: " + typeElement.getQualifiedName());

        // 5. 处理类中的方法
        List<McpMetadataItem> collectedItems = new ArrayList<>();
        for (Element enclosed : typeElement.getEnclosedElements()) {
            if (enclosed.getKind() == ElementKind.METHOD) {
                McpMetadataItem mcpMetadataItem = methodExplorer.processMethod((ExecutableElement) enclosed,
                        typeElement, metadataCollector);
                if (mcpMetadataItem != null) {
                    collectedItems.add(mcpMetadataItem);
                }
            }
        }

        // 6. 调用处理器
        for (MetadataProcessorExtension processor : extensionRegistry.getMetadataProcessors()) {
            processor.afterProcessClass(typeElement, collectedItems);
        }

        return !collectedItems.isEmpty();
    }

    /**
     * 应用类级别过滤器
     */
    private boolean applyClassFilters(TypeElement classElement) {
        for (MetadataFilterExtension filter : extensionRegistry.getMetadataFilters()) {
            if (!filter.shouldProcessClass(classElement)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * 检查是否为RestController
     */
    private boolean hasRestControllerAnnotation(TypeElement typeElement) {
        for (AnnotationMirror annotation : typeElement.getAnnotationMirrors()) {
            DeclaredType annotationType = annotation.getAnnotationType();
            if (annotationType != null) {
                String annotationName = annotationType.toString();
                if (REST_CONTROLLER.equals(annotationName)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * 检查类是否在目标包中
     */
    private boolean isInTargetPackage(TypeElement typeElement) {
        String packageName = elementUtils.getPackageOf(typeElement).getQualifiedName().toString();
        
        for (String targetPackage : config.getTargetPackages()) {
            if (packageName.equals(targetPackage) || packageName.startsWith(targetPackage + ".")) {
                return true;
            }
        }
        
        return false;
    }
}