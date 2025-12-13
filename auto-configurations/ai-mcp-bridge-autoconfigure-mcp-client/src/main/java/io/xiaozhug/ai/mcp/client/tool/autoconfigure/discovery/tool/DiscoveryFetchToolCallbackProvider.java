package io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.tool;

import io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.DiscoveryMcpServerFetcher;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.DiscoveryMcpServerProcessorManager;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.mcp.HttpToolCallbackProvider;
import io.xiaozhug.ai.mcp.common.metadata.McpTool;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 发现获取工具回调提供者
 *
 * @author xiaozhug
 */
public class DiscoveryFetchToolCallbackProvider  implements HttpToolCallbackProvider {

    private final DiscoveryMcpServerFetcher discoveryMcpServerFetcher;
    private final DiscoveryMcpServerProcessorManager discoveryMcpServerProcessorManager;

    public DiscoveryFetchToolCallbackProvider(DiscoveryMcpServerFetcher discoveryMcpServerFetcher,
                                              DiscoveryMcpServerProcessorManager discoveryMcpServerProcessorManager) {
        this.discoveryMcpServerFetcher = discoveryMcpServerFetcher;
        this.discoveryMcpServerProcessorManager = discoveryMcpServerProcessorManager;
    }

    @Override
    public ToolCallback[] getToolCallbacks() {
        List<ToolCallback> toolCallbackList = new ArrayList<>();

        Map<String, List<McpTool>> cachedDiscoveryExposeItemMetadatasMap = discoveryMcpServerFetcher.getHttpMetadataCache().getToolNameToExposeItemMetadataMap();
        if(!CollectionUtils.isEmpty(cachedDiscoveryExposeItemMetadatasMap)) {
            for (Map.Entry<String, List<McpTool>> entry : cachedDiscoveryExposeItemMetadatasMap.entrySet()) {
                toolCallbackList.add(new DiscoveryHttpToolCallback(discoveryMcpServerProcessorManager, entry.getValue().get(0)));
            }
        }

        ToolCallback[] toolCallbacks = toolCallbackList.toArray(new ToolCallback[0]);

        validateToolCallbacks(toolCallbacks);

        return toolCallbacks;
    }
}
