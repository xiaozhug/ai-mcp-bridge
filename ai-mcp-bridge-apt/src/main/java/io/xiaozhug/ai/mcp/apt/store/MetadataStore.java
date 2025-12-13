package io.xiaozhug.ai.mcp.apt.store;

import io.xiaozhug.ai.mcp.apt.config.ProcessorConfig;
import io.xiaozhug.ai.mcp.apt.exception.StoreException;
import io.xiaozhug.ai.mcp.apt.util.LogUtils;
import io.xiaozhug.ai.mcp.common.metadata.McpMetadata;
import io.xiaozhug.ai.mcp.common.metadata.McpMetadataItem;
import io.xiaozhug.ai.mcp.common.util.JsonUtils;

import javax.annotation.processing.ProcessingEnvironment;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 元数据存储
 * <p>
 * 负责读取和写入元数据文件
 * </p>
 *
 * @author xiaozhug
 */
public class MetadataStore {
    
    private final ProcessingEnvironment environment;
    private final ProcessorConfig config;
    private final String metadataPath;
    
    public MetadataStore(ProcessingEnvironment environment, ProcessorConfig config) {
        this.environment = environment;
        this.config = config;
        this.metadataPath = config.getOutputPath() + ProcessorConfig.DEFAULT_METADATA_PATH;
    }
    
    /**
     * 读取元数据
     * 
     * @return 配置元数据，如果读取失败则返回null
     */
    public McpMetadata readMetadata() {
        try {
            Path path = Paths.get(metadataPath);
            
            // 检查文件是否存在
            if (!Files.exists(path)) {
                LogUtils.debug("元数据文件不存在: " + metadataPath);
                return null;
            }
            
            // 读取文件内容
            String content = new String(Files.readAllBytes(path));
            if (content == null || content.trim().isEmpty()) {
                return null;
            }
            
            // 解析JSON
            McpMetadata metadata = JsonUtils.fromJson(content, McpMetadata.class);
            
            LogUtils.debug(String.format("从 %s 读取 %d 项元数据",
                metadataPath,
                metadata.getItems() == null ? 0 : metadata.getItems().size()));

            return metadata;
            
        } catch (IOException e) {
            LogUtils.error("读取元数据文件失败: " + e.getMessage());
            if (config.isDebugMode()) {
                e.printStackTrace();
            }
            return null;
        } catch (Exception e) {
            LogUtils.error("解析元数据失败: " + e.getMessage());
            if (config.isDebugMode()) {
                e.printStackTrace();
            }
            return null;
        }
    }
    
    /**
     * 写入元数据
     * 
     * @param metadata 配置元数据
     * @throws StoreException 写入失败时抛出异常
     */
    public void writeMetadata(McpMetadata metadata) throws StoreException {
        if (metadata == null) {
            throw new StoreException("元数据不能为null");
        }
        
        if (metadata.getItems() == null || metadata.getItems().isEmpty()) {
            LogUtils.debug("没有元数据需要写入");
            return;
        }
        
        try {
            // 清理和优化元数据
            cleanMetadata(metadata);
            
            // 创建目录（如果不存在）
            Path path = Paths.get(metadataPath);
            Path parentDir = path.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }
            
            // 写入格式化JSON
            JsonUtils.writeFormattedJson(metadata, metadataPath);
            
            LogUtils.debug(String.format("元数据已写入 %s，共 %d 项",
                metadataPath, 
                metadata.getItems().size()));
            
        } catch (IOException e) {
            throw new StoreException("写入元数据文件失败: " + metadataPath, e);
        } catch (Exception e) {
            throw new StoreException("序列化元数据失败", e);
        }
    }
    
    /**
     * 清理和优化元数据
     */
    private void cleanMetadata(McpMetadata metadata) {
        if (metadata == null || metadata.getItems() == null) {
            return;
        }
        
        metadata.getItems().forEach(item -> {
            if (item.getParams() != null) {
                item.getParams().forEach(param -> {
                    // 清理参数类型
                    param.setParamType(param.getParamQualifiedName());
                    
                    // 清理字段
                    if (param.getFields() != null) {
                        param.getFields().forEach(this::cleanField);
                    }
                });
            }
        });
    }
    
    /**
     * 清理字段
     */
    private void cleanField(McpMetadataItem.Param.Field field) {
        if (field == null) {
            return;
        }
        
        // 清理字段类型
        field.setFieldType(field.getFieldQualifiedName());
        
        // 递归清理嵌套字段
        if (field.getFields() != null) {
            field.getFields().forEach(this::cleanField);
        }
    }
    
    /**
     * 获取元数据路径
     */
    public String getMetadataPath() {
        return metadataPath;
    }
}