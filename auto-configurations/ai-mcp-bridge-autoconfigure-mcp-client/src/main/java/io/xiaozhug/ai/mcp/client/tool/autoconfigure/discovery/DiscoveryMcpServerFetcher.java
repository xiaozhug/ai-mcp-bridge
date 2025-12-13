package io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery;

import io.xiaozhug.ai.mcp.client.tool.autoconfigure.util.ToolUtils;
import io.xiaozhug.ai.mcp.common.metadata.McpToolSpecification;
import io.xiaozhug.ai.mcp.common.metadata.McpTool;
import io.xiaozhug.ai.mcp.common.util.JsonUtils;
import io.xiaozhug.ai.mcp.common.util.StringCompressionUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 发现MCP服务器抓取器
 *
 * @author xiaozhug
 */
@Slf4j
public class DiscoveryMcpServerFetcher implements ApplicationListener<ContextRefreshedEvent>, DisposableBean, ApplicationEventPublisherAware {

    public final static String MCP_FILE_KEY = "mcp-file";
    public final static String MCP_FILE_SIZE_KEY = "mcp-file-size";
    public final static String MCP_SERVER_KEY = "mcp-server";
    public final static String MCP_SERVER_SIZE_KEY = "mcp-server-size";

    private volatile HttpMetadataCache httpMetadataCache = new HttpMetadataCache(null, null);
    private volatile McpServerMetadataCache mcpServerMetadataCache = new McpServerMetadataCache(null, null);

    private final DiscoveryClient discoveryClient;
    private final DiscoveryFetchProperties discoveryFetchProperties;
    private final ScheduledExecutorService schedule = new ScheduledThreadPoolExecutor(1, r -> {
        return new Thread(r, "DiscoveryMcpFetch, DiscoveryMcpFetch-" + r.hashCode());
    });

    private ApplicationEventPublisher applicationEventPublisher;

    public DiscoveryMcpServerFetcher(DiscoveryClient discoveryClient, DiscoveryFetchProperties discoveryFetchProperties) {
        this.discoveryClient = discoveryClient;
        this.discoveryFetchProperties = discoveryFetchProperties;
        this.fetchMetadata();
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        schedule.scheduleWithFixedDelay(this::fetchMetadata,
                discoveryFetchProperties.getRefreshInterval(), discoveryFetchProperties.getRefreshInterval(), TimeUnit.SECONDS);
    }

    private void fetchMetadata() {
        try {
            List<String> services = CollectionUtils.isEmpty(discoveryFetchProperties.getServices()) ?
                    discoveryClient.getServices() : discoveryFetchProperties.getServices();

            if(!CollectionUtils.isEmpty(services)){
                List<McpToolSpecification> httpMetadataList = new ArrayList<>();
                List<McpToolSpecification> mcpServerMetadataList = new ArrayList<>();

                for (String service : services) {
                    List<ServiceInstance> instances = discoveryClient.getInstances(service);
                    if(!CollectionUtils.isEmpty(instances)){

                        instances.stream().filter(instance -> {
                            Map<String, String> metadata = instance.getMetadata();
                            return metadata != null && (metadata.containsKey(MCP_FILE_SIZE_KEY) || metadata.containsKey(MCP_SERVER_SIZE_KEY));
                        }).forEach(instance -> {
                            Map<String, String> metadata = instance.getMetadata();

                            int mcpFileSize = Integer.parseInt(Optional.ofNullable(metadata.get(MCP_FILE_SIZE_KEY)).orElse("0"));
                            if(mcpFileSize > 0){
                                StringBuilder httpMetadata = new StringBuilder();
                                for(int i = 0; i < mcpFileSize; i++){
                                    String splitInfo = metadata.get(MCP_FILE_KEY + "-" + i);
                                    if(StringUtils.hasLength(splitInfo)){
                                        httpMetadata.append(splitInfo);
                                    }
                                }

                                if(!httpMetadata.isEmpty()){
                                    McpToolSpecification configurationMetadata = JsonUtils.fromJson(StringCompressionUtil.decompress(httpMetadata.toString()), McpToolSpecification.class);
                                    if(configurationMetadata != null){
                                        List<McpTool> tools = configurationMetadata.getTools();
                                        if(!CollectionUtils.isEmpty(tools)){
                                            configurationMetadata.setToolNames(new ArrayList<>());
                                            for (McpTool tool : tools) {
                                                tool.setServiceName(service);
                                                tool.setScheme(instance.isSecure() ? "https" : "http");
                                                tool.setName(ToolUtils.getToolName(service, tool));
                                                configurationMetadata.getToolNames().add(tool.getName());
                                            }

                                            configurationMetadata.setServiceName(service);
                                            configurationMetadata.setInstanceId(instance.getInstanceId());
                                            configurationMetadata.setUrl(instance.getUri().toString());
                                            httpMetadataList.add(configurationMetadata);
                                        }
                                    }
                                }
                            }

                            int mcpServerSize = Integer.parseInt(Optional.ofNullable(metadata.get(MCP_SERVER_SIZE_KEY)).orElse("0"));
                            if(mcpServerSize > 0){
                                StringBuilder mcpServerMetadata = new StringBuilder();
                                for(int i = 0; i < mcpServerSize; i++){
                                    String splitInfo = metadata.get(MCP_SERVER_KEY + "-" + i);
                                    if(StringUtils.hasLength(splitInfo)){
                                        mcpServerMetadata.append(splitInfo);
                                    }
                                }

                                if(!mcpServerMetadata.isEmpty()){
                                    McpToolSpecification configurationMetadata = JsonUtils.fromJson(StringCompressionUtil.decompress(mcpServerMetadata.toString()), McpToolSpecification.class);
                                    if(configurationMetadata != null){
                                        List<McpTool> tools = configurationMetadata.getTools();
                                        if(!CollectionUtils.isEmpty(tools)){
                                            configurationMetadata.setToolNames(new ArrayList<>());
                                            for (McpTool tool : tools) {
                                                tool.setServiceName(service);
                                                tool.setName(ToolUtils.getToolName(service, tool, null));
                                                configurationMetadata.getToolNames().add(tool.getName());
                                            }

                                            configurationMetadata.setServiceName(service);
                                            configurationMetadata.setInstanceId(instance.getInstanceId());
                                            configurationMetadata.setUrl(instance.getUri().toString());
                                            mcpServerMetadataList.add(configurationMetadata);
                                        }
                                    }
                                }
                            }
                        });
                    }
                }

                this.httpMetadataCache = new HttpMetadataCache(
                        Collections.unmodifiableMap(httpMetadataList.stream().collect(Collectors.groupingBy(McpToolSpecification::getServiceName))),
                        Collections.unmodifiableMap(buildDiscoveryRequestTemplateInfoMap(httpMetadataList))
                );

                this.mcpServerMetadataCache = new McpServerMetadataCache(
                        Collections.unmodifiableMap(mcpServerMetadataList.stream().collect(Collectors.groupingBy(McpToolSpecification::getServiceName))),
                        Collections.unmodifiableMap(buildDiscoveryRequestTemplateInfoMap(mcpServerMetadataList))
                );
            } else {
                this.httpMetadataCache = new HttpMetadataCache(null, null);
                this.mcpServerMetadataCache = new McpServerMetadataCache(null, null);
            }
        } catch (Throwable t) {
            log.error("Failed to fetch MCP metadata from DiscoveryClient", t);
        }

        if(applicationEventPublisher != null) {
            applicationEventPublisher.publishEvent(new DiscoveryMcpServerRefreshEvent(new DiscoveryMcpServerRefreshEventSource(httpMetadataCache, mcpServerMetadataCache)));
        }
    }

    private Map<String, List<McpTool>> buildDiscoveryRequestTemplateInfoMap(List<McpToolSpecification> cachedMetadataList) {
        if(!CollectionUtils.isEmpty(cachedMetadataList)) {
            return cachedMetadataList.stream().flatMap(configurationMetadata -> configurationMetadata.getTools().stream())
                    .collect(Collectors.groupingBy(McpTool::getName));
        }

        return Collections.emptyMap();
    }

    @Override
    public void destroy() throws Exception {
        schedule.shutdownNow();
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public HttpMetadataCache getHttpMetadataCache() {
        return httpMetadataCache;
    }

    public McpServerMetadataCache getMcpServerMetadataCache() {
        return mcpServerMetadataCache;
    }


    @Data
    public static class HttpMetadataCache {
        private final Map<String, List<McpToolSpecification>> serviceNameToExposeConfigurationMetadatasMap;
        private final Map<String, List<McpTool>> toolNameToExposeItemMetadataMap;
    }

    @Data
    public static class McpServerMetadataCache {
        private final Map<String, List<McpToolSpecification>> serviceNameToExposeConfigurationMetadatasMap;
        private final Map<String, List<McpTool>> toolNameToExposeItemMetadataMap;
    }
}
