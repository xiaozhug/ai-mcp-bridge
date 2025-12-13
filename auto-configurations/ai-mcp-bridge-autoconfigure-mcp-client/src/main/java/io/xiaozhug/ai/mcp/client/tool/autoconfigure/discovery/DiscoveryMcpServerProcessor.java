package io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery;

import io.xiaozhug.ai.mcp.common.metadata.McpTool;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map;

/**
 * 发现MCP服务处理器
 *
 * @author xiaozhug
 */
public abstract class DiscoveryMcpServerProcessor {

    public abstract String process(String toolName, List<McpTool> exposeItemMetadatas, Map<String, Object> args, @Nullable Map<String, Object> context);

    public abstract String type();
}
