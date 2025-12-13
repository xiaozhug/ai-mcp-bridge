package io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.tool;

import io.modelcontextprotocol.spec.McpSchema;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.DiscoveryMcpServerProcessorManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;

import java.util.HashMap;

/**
 * 负载均衡MCP工具回调
 *
 * @author xiaozhug
 */
@Slf4j
public abstract class LoadbalancedMcpToolCallback implements ToolCallback {

	protected final McpSchema.Tool tool;
	protected final DiscoveryMcpServerProcessorManager discoveryMcpServerProcessorManager;
	protected volatile ToolDefinition toolDefinition;

	public LoadbalancedMcpToolCallback(String serviceName,
                                       DiscoveryMcpServerProcessorManager discoveryMcpServerProcessorManager,
                                       McpSchema.Tool tool) {
		this.discoveryMcpServerProcessorManager = discoveryMcpServerProcessorManager;
		this.tool = tool;
	}

	@Override
	public ToolDefinition getToolDefinition() {
		return this.toolDefinition;
	}

	@Override
	public String call(String input) {
		return call(input, new ToolContext(new HashMap<>()));
	}

	@Override
	public String call(String input, ToolContext toolContext) {
		return discoveryMcpServerProcessorManager.process(tool.name(), input, toolContext.getContext());
	}

}
