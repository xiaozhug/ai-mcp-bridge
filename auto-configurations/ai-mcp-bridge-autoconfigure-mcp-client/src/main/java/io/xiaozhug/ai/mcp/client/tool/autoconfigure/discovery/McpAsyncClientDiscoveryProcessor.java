package io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery;

import io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.transport.LoadbalancedMcpAsyncClient;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.transport.LoadbalancedMcpClient;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.transport.LoadbalancedMcpClientManager;
import io.xiaozhug.ai.mcp.common.metadata.McpTool;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.model.ModelOptionsUtils;

import java.util.List;
import java.util.Map;

/**
 * MCP异步客户端发现处理器
 *
 * @author xiaozhug
 */
@Slf4j
public class McpAsyncClientDiscoveryProcessor extends McpClientDiscoveryProcessor {

    public McpAsyncClientDiscoveryProcessor(LoadbalancedMcpClientManager<LoadbalancedMcpAsyncClient> loadbalancedMcpClientManager) {
        super(loadbalancedMcpClientManager);
    }

    @Override
    protected String doProcess(String toolName, LoadbalancedMcpClient<?> loadbalancedMcpClient, List<McpTool> exposeItemMetadatas,
                               Map<String, Object> args) {
        return ((LoadbalancedMcpAsyncClient)loadbalancedMcpClient).callTool(new McpSchema.CallToolRequest(toolName, args))
                .map((response) -> {
                    if (response.isError() != null && response.isError()) {
                        log.error("Error calling tool {}: {}", toolName, response.content());
                        return null;
                    }
                    else {
                        return ModelOptionsUtils.toJsonString(response.content());
                    }
                })
                .block();
    }
}
