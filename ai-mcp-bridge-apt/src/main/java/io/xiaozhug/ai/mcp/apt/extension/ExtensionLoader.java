package io.xiaozhug.ai.mcp.apt.extension;

import io.xiaozhug.ai.mcp.apt.config.ProcessorConfig;
import io.xiaozhug.ai.mcp.apt.util.LogUtils;

import java.util.*;

import static io.xiaozhug.ai.mcp.apt.util.LogUtils.debug;

/**
 * 扩展点加载器 - 支持SPI自动发现
 *
 * @author xiaozhug
 */
public class ExtensionLoader {
    
    /**
     * 加载所有扩展点
     */
    public static ExtensionRegistry loadAllExtensions(ProcessorConfig config) {
        ExtensionRegistry registry = new ExtensionRegistry();
        
        // 从配置加载
        loadFromConfig(registry, config);
        
        // 从SPI加载
        loadFromSPI(registry, config);
        
        return registry;
    }
    
    /**
     * 从配置加载扩展点
     */
    private static void loadFromConfig(ExtensionRegistry registry, ProcessorConfig config) {
        Set<String> extensionClasses = config.getExtensionClasses();
        if (extensionClasses.isEmpty()) {
            return;
        }
        
        for (String className : extensionClasses) {
            try {
                loadExtension(registry, className.trim());
            } catch (Exception e) {
                LogUtils.error("从配置加载扩展点失败: " + className, e);
            }
        }
    }
    
    /**
     * 从SPI加载扩展点
     */
    private static void loadFromSPI(ExtensionRegistry registry, ProcessorConfig config) {
        ClassLoader classLoader = ExtensionLoader.class.getClassLoader();
        // 加载所有扩展点类型
        loadSPIExtensions(classLoader, MetadataProcessorExtension.class, registry::addMetadataProcessor);
        loadSPIExtensions(classLoader, FieldExplorerExtension.class, registry::addFieldExplorer);
        loadSPIExtensions(classLoader, LLMServiceExtension.class, registry::addLlmService);
        loadSPIExtensions(classLoader, TemplateGeneratorExtension.class, registry::addTemplateGenerator);
        loadSPIExtensions(classLoader, MetadataFilterExtension.class, registry::addMetadataFilter);
    }
    
    /**
     * 加载特定类型的SPI扩展
     */
    private static <T extends ConfigurableExtension> void loadSPIExtensions(ClassLoader classLoader,
                                            Class<T> extensionType,
                                            ExtensionConsumer<T> consumer) {

        List<T> extensions = new ArrayList();
        ServiceLoader<T> serviceLoader = ServiceLoader.load(extensionType, classLoader);
        serviceLoader.iterator().forEachRemaining(extensions::add);

        extensions.stream().sorted(Comparator.comparingInt(ConfigurableExtension::getOrder)).forEach(consumer::accept);

        debug("已加载 " + extensions.size() + " 个 " + extensionType.getSimpleName() + " 扩展点");
    }
    
    /**
     * 加载单个扩展点
     */
    @SuppressWarnings("unchecked")
    private static <T> T loadExtension(Class<T> extensionType, String className) {
        try {
            Class<?> clazz = Class.forName(className);
            if (extensionType.isAssignableFrom(clazz)) {
                return (T) clazz.newInstance();
            } else {
                LogUtils.error("类 " + className + " 未实现 " + extensionType.getName());
            }
        } catch (Exception e) {
            LogUtils.error("实例化扩展点失败: " + className, e);
        }
        return null;
    }
    
    /**
     * 加载扩展点
     */
    private static void loadExtension(ExtensionRegistry registry, String className) {
        // 尝试加载为各种类型的扩展点
        Object extension = null;
        
        try {
            extension = loadExtension(MetadataProcessorExtension.class, className);
            if (extension != null) {
                registry.addMetadataProcessor((MetadataProcessorExtension) extension);
                return;
            }
            
            extension = loadExtension(FieldExplorerExtension.class, className);
            if (extension != null) {
                registry.addFieldExplorer((FieldExplorerExtension) extension);
                return;
            }
            
            extension = loadExtension(LLMServiceExtension.class, className);
            if (extension != null) {
                registry.addLlmService((LLMServiceExtension) extension);
                return;
            }
            
            extension = loadExtension(TemplateGeneratorExtension.class, className);
            if (extension != null) {
                registry.addTemplateGenerator((TemplateGeneratorExtension) extension);
                return;
            }
            
            extension = loadExtension(MetadataFilterExtension.class, className);
            if (extension != null) {
                registry.addMetadataFilter((MetadataFilterExtension) extension);
                return;
            }
            
            LogUtils.error("无法确定扩展点类型: " + className);
            
        } catch (Exception e) {
            LogUtils.error("加载扩展点失败: " + className, e);
        }
    }
    
    /**
     * 扩展点消费者接口
     */
    @FunctionalInterface
    private interface ExtensionConsumer<T> {
        void accept(T extension);
    }
}