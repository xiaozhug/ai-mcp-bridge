package io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.tool;

import io.xiaozhug.ai.mcp.client.tool.autoconfigure.HttpToolCallback;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.DiscoveryMcpServerProcessorManager;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.util.RequestTemplateUtils;
import io.xiaozhug.ai.mcp.common.metadata.McpTool;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.definition.DefaultToolDefinition;


/**
 * 发现HTTP工具回调
 *
 * @author xiaozhug
 */
public class DiscoveryHttpToolCallback extends HttpToolCallback {

    private final DiscoveryMcpServerProcessorManager discoveryMcpServerProcessorManager;

    public DiscoveryHttpToolCallback(DiscoveryMcpServerProcessorManager discoveryMcpServerProcessorManager, McpTool tool) {
        super();
        this.discoveryMcpServerProcessorManager = discoveryMcpServerProcessorManager;
        this.toolDefinition = DefaultToolDefinition.builder()
                .name(tool.getName())
                .description(tool.getDescription())
                .inputSchema(tool.getInputSchema())
                .build();
        this.info = RequestTemplateUtils.extractRequestTemplateInfo(tool.getInputSchema());
    }

    @Override
    public String call(String input, ToolContext toolContext) {
        return discoveryMcpServerProcessorManager.process(toolDefinition.name(), input, toolContext != null ? toolContext.getContext() : null);
    }
}