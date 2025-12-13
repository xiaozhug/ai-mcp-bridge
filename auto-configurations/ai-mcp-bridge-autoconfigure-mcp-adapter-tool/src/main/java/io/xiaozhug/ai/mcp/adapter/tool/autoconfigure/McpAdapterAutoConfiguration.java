package io.xiaozhug.ai.mcp.adapter.tool.autoconfigure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MCP适配器自动配置
 *
 * @author xiaozhug
 */
@Configuration(proxyBeanMethods = false)
public class McpAdapterAutoConfiguration {

    @Bean
    public McpMetadataToolCallbackProvider restControllerToMcpAdapter(){
        return new McpMetadataToolCallbackProvider();
    }
}
