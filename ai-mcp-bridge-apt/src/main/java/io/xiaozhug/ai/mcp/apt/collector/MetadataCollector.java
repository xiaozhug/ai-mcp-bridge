package io.xiaozhug.ai.mcp.apt.collector;

import com.fasterxml.jackson.core.type.TypeReference;
import io.xiaozhug.ai.mcp.apt.config.ProcessorConfig;
import io.xiaozhug.ai.mcp.apt.extension.ExtensionRegistry;
import io.xiaozhug.ai.mcp.apt.extension.MetadataFilterExtension;
import io.xiaozhug.ai.mcp.apt.exception.ProcessorException;
import io.xiaozhug.ai.mcp.apt.llm.LLMService;
import io.xiaozhug.ai.mcp.apt.store.MetadataStore;
import io.xiaozhug.ai.mcp.apt.util.LogUtils;
import io.xiaozhug.ai.mcp.common.metadata.LLMRequestMetadataItem;
import io.xiaozhug.ai.mcp.common.metadata.McpMetadata;
import io.xiaozhug.ai.mcp.common.metadata.McpMetadataItem;
import io.xiaozhug.ai.mcp.common.metadata.RequestTemplateInfo;
import io.xiaozhug.ai.mcp.common.util.JsonUtils;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.*;

/**
 * 元数据收集器
 * <p>
 * 负责收集、处理和合并元数据
 * </p>
 *
 * @author xiaozhug
 */
public class MetadataCollector {

    private final ProcessingEnvironment processingEnvironment;
    private final McpMetadata previousMetadata;
    private final LLMService llmService;
    private final ProcessorConfig config;
    private final ExtensionRegistry extensionRegistry;

    private final List<McpMetadataItem> metadataItems = new ArrayList<>();
    private final List<McpMetadata> currentMetadatas = new ArrayList<>();

    /**
     * 创建元数据收集器实例
     *
     * @param processingEnvironment 处理环境
     * @param previousMetadata 之前的元数据（可为null）
     * @param config 处理器配置
     */
    public MetadataCollector(ProcessingEnvironment processingEnvironment,
                             McpMetadata previousMetadata,
                             ProcessorConfig config,
                             ExtensionRegistry extensionRegistry) {
        this.processingEnvironment = Objects.requireNonNull(processingEnvironment);
        this.previousMetadata = previousMetadata;
        this.config = Objects.requireNonNull(config);
        this.extensionRegistry = extensionRegistry != null ? extensionRegistry : new ExtensionRegistry();
        this.llmService = new LLMService(config, extensionRegistry);
    }

    /**
     * 添加元数据项（带扩展点处理）
     */
    public void add(TypeElement classElement, ExecutableElement methodElement, McpMetadataItem metadata) {
        if (metadata == null || classElement == null || methodElement == null) {
            return;
        }

        // 检查是否已存在于历史元数据中
        if (isMetadataExistInHistory(metadata)) {
            LogUtils.debug("跳过已存在的元数据项: " +
                    metadata.getClassName() + "." + metadata.getMethodName());
            return;
        }

        // 3. 应用参数和字段过滤器
        metadata = filterParametersAndFields(metadata);

        // 4. 添加到当前收集列表
        this.metadataItems.add(metadata);

        LogUtils.debug("添加元数据项: " +
                metadata.getClassName() + "." + metadata.getMethodName());
    }

    /**
     * 过滤参数和字段
     */
    private McpMetadataItem filterParametersAndFields(McpMetadataItem metadata) {
        if (metadata == null) {
            return metadata;
        }

        // 过滤字段
        if (metadata.getParams() != null) {
            for (McpMetadataItem.Param param : metadata.getParams()) {
                filterParamFields(param);
            }
        }

        return metadata;
    }

    /**
     * 递归过滤参数字段
     */
    private void filterParamFields(McpMetadataItem.Param param) {
        if (param == null || param.getFields() == null || param.getFields().isEmpty()) {
            return;
        }

        List<McpMetadataItem.Param.Field> filteredFields = new ArrayList<>();
        for (McpMetadataItem.Param.Field field : param.getFields()) {
            boolean shouldKeep = true;

            // 应用字段过滤器
            for (MetadataFilterExtension filter : extensionRegistry.getMetadataFilters()) {
                if (!filter.shouldProcessField(
                        field.getFieldName(),
                        field.getFieldType(),
                        param.getParamType())) {
                    shouldKeep = false;
                    break;
                }
            }

            if (shouldKeep) {
                // 递归过滤嵌套字段
                filterNestedFields(field);
                filteredFields.add(field);
            }
        }

        param.setFields(filteredFields);
    }

    /**
     * 递归过滤嵌套字段
     */
    private void filterNestedFields(McpMetadataItem.Param.Field field) {
        if (field == null || field.getFields() == null || field.getFields().isEmpty()) {
            return;
        }

        List<McpMetadataItem.Param.Field> filteredFields = new ArrayList<>();
        for (McpMetadataItem.Param.Field nestedField : field.getFields()) {
            boolean shouldKeep = true;

            // 应用字段过滤器
            for (MetadataFilterExtension filter : extensionRegistry.getMetadataFilters()) {
                if (!filter.shouldProcessField(
                        nestedField.getFieldName(),
                        nestedField.getFieldType(),
                        field.getFieldType())) {
                    shouldKeep = false;
                    break;
                }
            }

            if (shouldKeep) {
                // 递归过滤更深层的嵌套字段
                filterNestedFields(nestedField);
                filteredFields.add(nestedField);
            }
        }

        field.setFields(filteredFields);
    }


    /**
     * 检查元数据是否已存在于历史数据中
     */
    private boolean isMetadataExistInHistory(McpMetadataItem metadata) {
        if (previousMetadata == null) {
            return false;
        }

        List<McpMetadataItem> mcpMetadataItems = previousMetadata.getItems();
        if (mcpMetadataItems == null || mcpMetadataItems.isEmpty()) {
            return false;
        }

        return mcpMetadataItems.contains(metadata);
    }

    /**
     * 生成元数据
     */
    public void generateMetadata() {
        if (metadataItems.isEmpty()) {
            LogUtils.debug("没有需要处理的元数据项");
            return;
        }

        try {
            LogUtils.debug(String.format("开始生成元数据，共 %d 项", metadataItems.size()));

            McpMetadata metadata = new McpMetadata();
            metadata.setItems(new ArrayList<>(metadataItems));

            // 调用LLM服务处理元数据
            McpMetadata processedMetadata = processWithLLM(metadata);

            // 应用最终元数据过滤器
            processedMetadata = applyFinalMetadataFilter(processedMetadata);

            // 添加到当前元数据列表
            currentMetadatas.add(processedMetadata);

            // 清空当前收集的项
            metadataItems.clear();

            LogUtils.debug("元数据生成完成");

        } catch (Exception e) {
            throw new ProcessorException("生成元数据失败", e);
        }
    }

    /**
     * 应用最终元数据过滤器
     */
    private McpMetadata applyFinalMetadataFilter(McpMetadata metadata) {
        if (metadata == null || metadata.getItems() == null || metadata.getItems().isEmpty()) {
            return metadata;
        }

        List<McpMetadataItem> filteredItems = metadata.getItems();

        for (MetadataFilterExtension filter : extensionRegistry.getMetadataFilters()) {
            filteredItems = filter.filterMetadata(filteredItems);

            LogUtils.debug("过滤器处理后剩余 " + filteredItems.size() + " 项元数据");
        }

        McpMetadata filteredMetadata = new McpMetadata();
        filteredMetadata.setItems(filteredItems);
        return filteredMetadata;
    }

    /**
     * 使用LLM处理元数据
     */
    private McpMetadata processWithLLM(McpMetadata metadata) {
        try {

            List<LLMRequestMetadataItem> llmItems = new ArrayList<>();
            List<McpMetadataItem> items = metadata.getItems();
            items.forEach(item -> {
                llmItems.add(item.toLLMRequestMetadataItem());
            });

            // 转换为JSON字符串
            String metadataString = JsonUtils.toJSONString(llmItems);

            // 调用LLM服务
            String callResult = llmService.callLargeModel(metadataString, config.getMaxRetries());

            if (callResult == null || callResult.trim().isEmpty()) {
                LogUtils.debug("警告: LLM返回结果为空，使用原始元数据");
                return metadata;
            }

            // 解析LLM返回的结果
            List<McpMetadataItem> processedMetadataItems =
                    JsonUtils.fromJson(callResult, new TypeReference<List<McpMetadataItem>>() {});

            this.fillDescription(items, processedMetadataItems);

            return metadata;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void fillDescription(List<McpMetadataItem> mcpMetadataItems, List<McpMetadataItem> processedMetadataItems) {
        if (mcpMetadataItems == null || processedMetadataItems == null ||
            mcpMetadataItems.size() != processedMetadataItems.size()) {
            throw new RuntimeException("LLM返回的元数据项数量与原始不匹配");
        }

        for (int i = 0; i < mcpMetadataItems.size(); i++) {
            McpMetadataItem originalItem = mcpMetadataItems.get(i);
            McpMetadataItem processedItem = processedMetadataItems.get(i);

            // 填充方法描述
            originalItem.setMethodNameDescription(processedItem.getMethodNameDescription());


            // 填充参数描述
            List<McpMetadataItem.Param> originalParams = originalItem.getParams();
            List<McpMetadataItem.Param> processedParams = processedItem.getParams();

            if(originalParams == null){
                continue;
            }

            if(processedParams == null || originalParams.size() != processedParams.size()){
                throw new RuntimeException("LLM返回的元数据参数数量与原始不匹配");
            }

            for (int j = 0; j < originalItem.getParams().size(); j++) {
                McpMetadataItem.Param originalParam = originalItem.getParams().get(j);
                McpMetadataItem.Param processedParam = processedItem.getParams().get(j);

                originalParam.setParamNameDescription(processedParam.getParamNameDescription());

                // 递归填充feild描述
                fillFieldDescriptions(originalParam.getFields(), processedParam.getFields());
            }
        }

    }

    private void fillFieldDescriptions(List<McpMetadataItem.Param.Field> originalFields, List<McpMetadataItem.Param.Field> processedFields) {
        if (originalFields == null) {
            return;
        }

        if (processedFields == null || originalFields.size() != processedFields.size()) {
            throw new RuntimeException("LLM返回的元数据字段数量与原始不匹配");
        }

        for (int i = 0; i < originalFields.size(); i++) {
            McpMetadataItem.Param.Field originalField = originalFields.get(i);
            McpMetadataItem.Param.Field processedField = processedFields.get(i);

            originalField.setFieldNameDescription(processedField.getFieldNameDescription());

            // 递归填充嵌套字段描述
            fillFieldDescriptions(originalField.getFields(), processedField.getFields());
        }
    }

    /**
     * 提取请求模板信息
     */
    private List<RequestTemplateInfo> extractRequestTemplates(
            List<McpMetadataItem> mcpMetadataItems) {
        List<RequestTemplateInfo> templates = new ArrayList<>();

        for (McpMetadataItem item : mcpMetadataItems) {
            templates.add(item.getRequestTemplateInfo());
            item.setRequestTemplateInfo(null); // 临时移除
        }

        return templates;
    }

    /**
     * 恢复请求模板信息
     */
    private void restoreRequestTemplates(List<McpMetadataItem> mcpMetadataItems,
                                       List<RequestTemplateInfo> templates) {
        if (mcpMetadataItems == null || templates == null ||
            mcpMetadataItems.size() != templates.size()) {
            return;
        }

        for (int i = 0; i < mcpMetadataItems.size(); i++) {
            mcpMetadataItems.get(i).setRequestTemplateInfo(templates.get(i));
        }
    }

    /**
     * 合并配置元数据
     * 将历史元数据和当前轮次的元数据合并
     *
     * @return 合并后的配置元数据
     */
    public McpMetadata mergeConfigurationMetadata() {
        McpMetadata merged = new McpMetadata();
        merged.setItems(new ArrayList<>());

        // 合并当前轮次元数据
        int currentItemCount = 0;
        for (McpMetadata config : currentMetadatas) {
            List<McpMetadataItem> items = config.getItems();
            if (items != null && !items.isEmpty()) {
                merged.getItems().addAll(items);
                currentItemCount += items.size();
            }
        }

        LogUtils.debug(String.format("合并当前轮次元数据 %d 项", currentItemCount));

        if(currentItemCount == 0){
            return merged;
        }

        // 合并历史元数据
        if (previousMetadata != null) {
            List<McpMetadataItem> items = previousMetadata.getItems();
            if (items != null && !items.isEmpty()) {
                merged.getItems().addAll(items);
                LogUtils.debug(String.format("合并历史元数据 %d 项", items.size()));
            }
        }

        LogUtils.debug(String.format("最终元数据总计 %d 项",
            merged.getItems() == null ? 0 : merged.getItems().size()));

        return merged;
    }

    /**
     * 写入元数据到存储
     *
     * @param metadataStore 元数据存储
     * @throws Exception 写入失败时抛出异常
     */
    public void writeMetadata(MetadataStore metadataStore) throws Exception {
        McpMetadata mergedMetadata = mergeConfigurationMetadata();
        metadataStore.writeMetadata(mergedMetadata);
    }

    /**
     * 清空当前轮次的元数据
     */
    public void clearCurrentMetadatas() {
        this.currentMetadatas.clear();
    }

    public McpMetadata getPreviousMetadata() {
        return previousMetadata;
    }

    public List<McpMetadata> getCurrentMetadatas() {
        return Collections.unmodifiableList(currentMetadatas);
    }

    public List<McpMetadataItem> getMetadataItems() {
        return Collections.unmodifiableList(metadataItems);
    }
}