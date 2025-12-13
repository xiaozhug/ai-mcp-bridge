package io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.transport;

import io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.McpClientProperties;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.transport.creator.AbstractMcpClientCreator;
import io.xiaozhug.ai.mcp.common.metadata.McpToolSpecification;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 负载均衡MCP客户端
 *
 * @author xiaozhug
 */
@Slf4j
@Data
public abstract class LoadbalancedMcpClient<T> implements DisposableBean {

    protected String serviceName;
    protected volatile List<McpToolSpecification> metadatas;
    protected volatile Set<String> metadataKeys;
    protected volatile Map<String, List<String>> keyToToolNameMap = new HashMap<>();

    protected final McpClientProperties mcpClientProperties;
    protected final AbstractMcpClientCreator mcpClientCreator;
    protected final Map<String, List<T>> keyToClientMap = new ConcurrentHashMap<>();
    protected final Map<String, Integer> client2CountMap = new ConcurrentHashMap<>();

    public LoadbalancedMcpClient(String serviceName, List<McpToolSpecification> metadatas,
                                 McpClientProperties mcpClientProperties, AbstractMcpClientCreator mcpClientCreator) {
        this.serviceName = serviceName;
        this.metadatas = metadatas;
        this.mcpClientProperties = mcpClientProperties;
        this.mcpClientCreator = mcpClientCreator;
        this.metadataKeys = this.metadatas.stream().map(metadata -> {
            keyToToolNameMap.putIfAbsent(metadata.getInstanceId() + "-" + metadata.getUrl(), metadata.getToolNames());
            return metadata.getInstanceId() + "-" + metadata.getUrl();
        }).collect(Collectors.toSet());
    }

    protected void init(){
        metadatas.forEach(this::createMcpClient);
    }

    public void refreshExposeConfigurationMetadataList(List<McpToolSpecification> newMetadataList) {
        Map<String, McpToolSpecification> keyToNewMetadataMap = newMetadataList.stream()
                .collect(Collectors.toMap(metadata -> metadata.getInstanceId() + "-" + metadata.getUrl(), metadata -> metadata));
        Set<String> newMetadataKeys = keyToNewMetadataMap.keySet();

        for(String metadataKey : metadataKeys) {
            if(!newMetadataKeys.contains(metadataKey)){
                removeMcpClient(metadataKey);
                keyToToolNameMap.remove(metadataKey);
                log.info("Removed MCP client for metadata key: {}", metadataKey);
            }
        }

        for(String metadataKey : newMetadataKeys) {
            if(!metadataKeys.contains(metadataKey)){
                McpToolSpecification mcpToolSpecification = keyToNewMetadataMap.get(metadataKey);
                createMcpClient(mcpToolSpecification);
                keyToToolNameMap.putIfAbsent(metadataKey, mcpToolSpecification.getToolNames());
                log.info("Created MCP client for metadata key: {}", metadataKey);
            }
        }

        metadatas = newMetadataList;
        metadataKeys = newMetadataKeys;
    }

    public T chooseMcpClient() {
        return chooseMcpClient(getMcpClientList());
    }

    public T chooseMcpClient(List<T> asynClients) {
        return getMcpClient(asynClients);
    }

    protected List<T> getMcpClientList() {
        return keyToClientMap.values().stream().flatMap(List::stream).toList();
    }

    protected String connectedClientName(String clientName, String serverConnectionName) {
        return clientName + " - " + serverConnectionName;
    }

    protected abstract T getMcpClient(List<T> asynClients);

    protected abstract void createMcpClient(McpToolSpecification mcpToolSpecification);

    protected abstract void removeMcpClient(String metadataKey);

}
