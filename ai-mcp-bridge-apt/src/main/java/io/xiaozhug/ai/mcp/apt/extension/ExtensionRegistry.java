package io.xiaozhug.ai.mcp.apt.extension;

import io.xiaozhug.ai.mcp.apt.util.LogUtils;

import java.util.*;

/**
 * 扩展点注册表
 *
 * @author xiaozhug
 */
public class ExtensionRegistry {
    
    private final List<MetadataProcessorExtension> metadataProcessors = new ArrayList<>();
    private final List<FieldExplorerExtension> fieldExplorers = new ArrayList<>();
    private final List<LLMServiceExtension> llmServices = new ArrayList<>();
    private final List<TemplateGeneratorExtension> templateGenerators = new ArrayList<>();
    private final List<MetadataFilterExtension> metadataFilters = new ArrayList<>();
    
    /**
     * 添加元数据处理器扩展
     */
    public void addMetadataProcessor(MetadataProcessorExtension extension) {
        if (extension != null) {
            metadataProcessors.add(extension);
            LogUtils.debug("注册元数据处理器扩展: " + extension.getName());
        }
    }
    
    /**
     * 添加字段探索器扩展
     */
    public void addFieldExplorer(FieldExplorerExtension extension) {
        if (extension != null) {
            fieldExplorers.add(extension);
            LogUtils.debug("注册字段探索器扩展: " + extension.getName());
        }
    }
    
    /**
     * 添加LLM服务扩展
     */
    public void addLlmService(LLMServiceExtension extension) {
        if (extension != null) {
            llmServices.add(extension);
            LogUtils.debug("注册LLM服务扩展: " + extension.getName());
        }
    }
    
    /**
     * 添加模板生成器扩展
     */
    public void addTemplateGenerator(TemplateGeneratorExtension extension) {
        if (extension != null) {
            templateGenerators.add(extension);
            LogUtils.debug("注册模板生成器扩展: " + extension.getName());
        }
    }
    
    /**
     * 添加元数据过滤器扩展
     */
    public void addMetadataFilter(MetadataFilterExtension extension) {
        if (extension != null) {
            metadataFilters.add(extension);
            LogUtils.debug("注册元数据过滤器扩展: " + extension.getName());
        }
    }
    
    // Getter方法
    public List<MetadataProcessorExtension> getMetadataProcessors() {
        return Collections.unmodifiableList(metadataProcessors);
    }
    
    public List<FieldExplorerExtension> getFieldExplorers() {
        return Collections.unmodifiableList(fieldExplorers);
    }
    
    public List<LLMServiceExtension> getLlmServices() {
        return Collections.unmodifiableList(llmServices);
    }
    
    public List<TemplateGeneratorExtension> getTemplateGenerators() {
        return Collections.unmodifiableList(templateGenerators);
    }
    
    public List<MetadataFilterExtension> getMetadataFilters() {
        return Collections.unmodifiableList(metadataFilters);
    }
    
}