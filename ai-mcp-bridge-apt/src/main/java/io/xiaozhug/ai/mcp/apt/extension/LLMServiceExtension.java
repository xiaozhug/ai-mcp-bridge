package io.xiaozhug.ai.mcp.apt.extension;

/**
 * LLM服务扩展点接口
 * 允许自定义LLM调用逻辑
 *
 * @author xiaozhug
 */
public interface LLMServiceExtension extends ConfigurableExtension{
    
    /**
     * 构建提示词
     */
    default String buildPrompt(String originalPrompt, String metadataJson) {
        return String.format(originalPrompt, metadataJson);
    }
    
    /**
     * 预处理元数据JSON
     */
    default String preprocessMetadata(String metadataJson) {
        return metadataJson;
    }
    
    /**
     * 后处理LLM响应
     */
    default String postprocessResponse(String llmResponse) {
        return llmResponse;
    }
    
    /**
     * 验证LLM响应
     */
    default boolean validateResponse(String llmResponse) {
        return llmResponse != null && 
               !llmResponse.trim().isEmpty() &&
               (llmResponse.contains("{") && llmResponse.contains("}"));
    }
    
    /**
     * 处理LLM调用失败
     */
    default String handleFailure(String originalMetadata, Exception e) {
        return originalMetadata; // 返回原始元数据
    }
}