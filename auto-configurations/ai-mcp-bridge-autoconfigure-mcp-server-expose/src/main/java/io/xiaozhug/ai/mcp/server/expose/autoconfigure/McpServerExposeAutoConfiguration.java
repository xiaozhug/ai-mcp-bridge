package io.xiaozhug.ai.mcp.server.expose.autoconfigure;

import io.xiaozhug.ai.mcp.common.metadata.McpToolSpecification;
import io.modelcontextprotocol.server.McpAsyncServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.spec.McpServerTransportProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MCP服务器信息暴露自动配置
 *
 * @author xiaozhug
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(McpAsyncServer.class)
public class McpServerExposeAutoConfiguration {

    @Bean
    public McpToolSpecification mcpServerInfoCollector(@Autowired(required = false) McpAsyncServer mcpAsyncServer,
                                                       @Autowired(required = false) McpSyncServer mcpSyncServer,
                                                       McpServerTransportProvider mcpServerTransport){
        if(mcpAsyncServer == null){
            mcpAsyncServer = mcpSyncServer.getAsyncServer();
        }
        return new McpServerInfoCollector(mcpAsyncServer, mcpServerTransport).getMcpServerInfo();
    }

}
