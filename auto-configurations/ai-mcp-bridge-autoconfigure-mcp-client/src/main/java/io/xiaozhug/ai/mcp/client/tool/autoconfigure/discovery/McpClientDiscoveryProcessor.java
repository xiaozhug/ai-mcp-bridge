package io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery;


import io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.transport.LoadbalancedMcpClient;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.transport.LoadbalancedMcpClientManager;
import io.xiaozhug.ai.mcp.common.metadata.McpTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * MCP客户端发现处理器
 *
 * @author xiaozhug
 */
@Slf4j
public abstract class McpClientDiscoveryProcessor extends DiscoveryMcpServerProcessor {

    protected final LoadbalancedMcpClientManager<? extends LoadbalancedMcpClient<?>> loadbalancedMcpClientManager;

    protected McpClientDiscoveryProcessor(LoadbalancedMcpClientManager<? extends LoadbalancedMcpClient<?>> loadbalancedMcpClientManager) {
        this.loadbalancedMcpClientManager = loadbalancedMcpClientManager;
    }

    @Override
    public String process(String toolName, List<McpTool> exposeItemMetadatas, Map<String, Object> args, @Nullable Map<String, Object> context) {
        log.info("Mcp Discovery Calling tool {} ", toolName);
        String serviceName = exposeItemMetadatas.get(0).getServiceName();

        List<? extends LoadbalancedMcpClient<?>> loadbalancedMcpClients = loadbalancedMcpClientManager.getLoadbalancedMcpClients();
        LoadbalancedMcpClient<?> loadbalancedMcpClient = getLoadbalancedMcpClientByServiceName(serviceName, loadbalancedMcpClients);

        if (!CollectionUtils.isEmpty(context)) {
            args.put("toolContext", context);
        }

        return doProcess(toolName, loadbalancedMcpClient, exposeItemMetadatas, args);
    }

    protected abstract String doProcess(String toolName, LoadbalancedMcpClient<?> loadbalancedMcpClient, List<McpTool> exposeItemMetadatas, Map<String, Object> args);

    private LoadbalancedMcpClient<?> getLoadbalancedMcpClientByServiceName(String serviceName, List<? extends LoadbalancedMcpClient<?>> loadbalancedMcpClients) {
        for (LoadbalancedMcpClient<?> client : loadbalancedMcpClients) {
            if (client.getServiceName().equals(serviceName)) {
                return client;
            }
        }

        log.warn("No LoadbalancedMcpClient found for service: {}", serviceName);

        return null;
    }


    @Override
    public String type() {
        return "mcp-sse";
    }
}
