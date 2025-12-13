package io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery;

import io.xiaozhug.ai.mcp.common.metadata.McpTool;
import io.xiaozhug.ai.mcp.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 发现MCP服务处理器管理器
 *
 * @author xiaozhug
 */
@Slf4j
public class DiscoveryMcpServerProcessorManager {

    private final DiscoveryMcpServerFetcher discoveryMcpServerFetcher;
    private Map<String, DiscoveryMcpServerProcessor> typeToProcessorMap;

    public DiscoveryMcpServerProcessorManager(List<DiscoveryMcpServerProcessor> processors, DiscoveryMcpServerFetcher discoveryMcpServerFetcher) {
        this.typeToProcessorMap = processors.stream().collect(Collectors.groupingBy(DiscoveryMcpServerProcessor::type,
                Collectors.collectingAndThen(Collectors.toList(), list -> list.get(0))));
        this.discoveryMcpServerFetcher = discoveryMcpServerFetcher;
    }

    public String process(String toolName, String input, @Nullable Map<String, Object> context) {
        try {
            List<McpTool> exposeItemMetadatas;
            exposeItemMetadatas = discoveryMcpServerFetcher.getHttpMetadataCache().getToolNameToExposeItemMetadataMap().get(toolName);

            if(CollectionUtils.isEmpty(exposeItemMetadatas)){
                exposeItemMetadatas = discoveryMcpServerFetcher.getMcpServerMetadataCache().getToolNameToExposeItemMetadataMap().get(toolName);
            }

            if(CollectionUtils.isEmpty(exposeItemMetadatas)){
                throw new RuntimeException("No MCP server found for tool: " + toolName);
            }

            Map<String, Object> args = new HashMap<>();
            if (!input.isEmpty()) {
                try {
                    args = JsonUtils.fromJson(input, Map.class);
                    log.info("[call] parsed args: {}", args);
                }
                catch (Exception e) {
                    log.error("[call] Failed to parse input to args", e);
                }
            }

            return typeToProcessorMap.get(exposeItemMetadatas.get(0).getProtocol()).process(toolName, exposeItemMetadatas, args, context);
        } catch (Throwable t) {
            throw new RuntimeException("Error processing tool request: " + t.getMessage(), t);
        }
    }
}
