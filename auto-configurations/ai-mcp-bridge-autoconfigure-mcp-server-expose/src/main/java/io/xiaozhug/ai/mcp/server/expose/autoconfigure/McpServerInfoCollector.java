package io.xiaozhug.ai.mcp.server.expose.autoconfigure;

import io.xiaozhug.ai.mcp.common.metadata.McpToolSpecification;
import io.xiaozhug.ai.mcp.common.metadata.McpTool;
import io.xiaozhug.ai.mcp.common.util.JsonUtils;
import io.xiaozhug.ai.mcp.common.util.MD5Utils;
import io.modelcontextprotocol.server.McpAsyncServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpServerTransportProvider;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * MCP 服务器信息收集器
 *
 * @author xiaozhug
 */
public class McpServerInfoCollector {

    private final McpAsyncServer mcpAsyncServer;
    private final McpServerTransportProvider mcpServerTransport;

    public McpServerInfoCollector(McpAsyncServer mcpAsyncServer, McpServerTransportProvider mcpServerTransport) {
        this.mcpAsyncServer = mcpAsyncServer;
        this.mcpServerTransport = mcpServerTransport;
    }

    @SneakyThrows
    public McpToolSpecification getMcpServerInfo() {
        Field toolsField = McpAsyncServer.class.getDeclaredField("tools");
        toolsField.setAccessible(true);
        CopyOnWriteArrayList<McpServerFeatures.AsyncToolSpecification> tools = (CopyOnWriteArrayList<McpServerFeatures.AsyncToolSpecification>) toolsField.get(mcpAsyncServer);
        List<McpSchema.Tool> toolsNeedtoRegister = tools.stream()
                .map(McpServerFeatures.AsyncToolSpecification::tool)
                .toList();

        MD5Utils md5Utils = new MD5Utils();
        String protocol = mcpServerTransport instanceof StdioServerTransportProvider ? "local" : "mcp-sse";
        McpToolSpecification mcpToolSpecification = new McpToolSpecification();
        mcpToolSpecification.setType("MCP_SERVER");
        mcpToolSpecification.setTools(toolsNeedtoRegister.stream().map(tool -> {
            McpTool mcpTool = new McpTool();
            mcpTool.setProtocol(protocol);
            mcpTool.setName(tool.name());
            mcpTool.setDescription(tool.description());
            mcpTool.setInputSchema(JsonUtils.toJSONString(tool.inputSchema()));
            mcpTool.setMd5(md5Utils.getMd5(tool.name() + " " + tool.description()));
            return mcpTool;
        }).collect(Collectors.toList()));

        return mcpToolSpecification;
    }
}
