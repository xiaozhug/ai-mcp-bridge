package io.xiaozhug.ai.mcp.apt.config;

import java.util.HashSet;
import java.util.Set;

/**
 * 注解处理器配置类
 *
 * @author xiaozhug
 */
public class ProcessorConfig {
    
    // 默认配置
    public static final String DEFAULT_OUTPUT_PATH = "src/main/resources";
    public static final String DEFAULT_METADATA_PATH = "/META-INF/mcp-metadata.json";
    public static final String PARAM_PREFIX = "mcp.";
    public static final boolean DEFAULT_ENABLED = true;
    public static final int DEFAULT_CHUNK_SIZE = 1;
    public static final boolean DEFAULT_DEBUG_MODE = false;
    public static final int DEFAULT_MAX_RETRIES = 3;
    
    // 配置项
    private boolean enabled = DEFAULT_ENABLED;
    private int chunkSize = DEFAULT_CHUNK_SIZE;
    private boolean debugMode = DEFAULT_DEBUG_MODE;
    private String outputPath = DEFAULT_OUTPUT_PATH;
    private Set<String> targetPackages = new HashSet<>();
    private int maxRetries = DEFAULT_MAX_RETRIES;
    
    // LLM配置
    private String apiUrl;
    private String apiKey;
    private String model;
    
    // 扩展点
    private Set<String> extensionClasses = new HashSet<>();
    
    public ProcessorConfig() {
    }
    
    // Getter 和 Setter 方法
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public int getChunkSize() {
        return chunkSize;
    }
    
    public void setChunkSize(int chunkSize) {
        this.chunkSize = Math.max(1, chunkSize);
    }
    
    public boolean isDebugMode() {
        return debugMode;
    }
    
    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }
    
    public String getOutputPath() {
        return outputPath;
    }
    
    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }
    
    public Set<String> getTargetPackages() {
        return targetPackages;
    }
    
    public void setTargetPackages(Set<String> targetPackages) {
        this.targetPackages = targetPackages;
    }
    
    public void addTargetPackage(String packageName) {
        this.targetPackages.add(packageName);
    }
    
    public int getMaxRetries() {
        return maxRetries;
    }
    
    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }
    
    public String getApiUrl() {
        return apiUrl;
    }
    
    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }
    
    public String getApiKey() {
        return apiKey;
    }
    
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public Set<String> getExtensionClasses() {
        return extensionClasses;
    }
    
    public void setExtensionClasses(Set<String> extensionClasses) {
        this.extensionClasses = extensionClasses;
    }
    
    /**
     * 验证配置是否有效
     */
    public boolean isValid() {
        if (!enabled) {
            return false;
        }
        
        if (targetPackages.isEmpty()) {
            return false;
        }
        
        // 验证LLM配置
        if (apiUrl == null || apiUrl.trim().isEmpty() ||
            apiKey == null || apiKey.trim().isEmpty() ||
            model == null || model.trim().isEmpty()) {
            return false;
        }
        
        return true;
    }
}