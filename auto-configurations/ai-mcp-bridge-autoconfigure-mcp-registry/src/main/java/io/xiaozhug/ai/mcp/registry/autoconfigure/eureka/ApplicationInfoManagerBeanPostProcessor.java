package io.xiaozhug.ai.mcp.registry.autoconfigure.eureka;

import io.xiaozhug.ai.mcp.registry.autoconfigure.InstancePreRegisteredApplicationListener;
import io.xiaozhug.ai.mcp.common.metadata.McpToolSpecification;
import io.xiaozhug.ai.mcp.common.metadata.McpTool;
import io.xiaozhug.ai.mcp.common.util.JsonUtils;
import io.xiaozhug.ai.mcp.common.util.MetadataUtils;
import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.InstanceInfo;
import io.xiaozhug.ai.mcp.common.util.StringCompressionUtil;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 应用信息管理器 Bean 后置处理器
 *
 * @author xiaozhug
 */
public class ApplicationInfoManagerBeanPostProcessor implements BeanPostProcessor {

    private final List<McpToolSpecification> mcpToolSpecifications;

    public ApplicationInfoManagerBeanPostProcessor(List<McpToolSpecification> mcpToolSpecifications) {
        this.mcpToolSpecifications = mcpToolSpecifications;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof ApplicationInfoManager) {
            if(mcpToolSpecifications != null){
                ApplicationInfoManager applicationInfoManager = (ApplicationInfoManager) bean;
                InstanceInfo instanceInfo = applicationInfoManager.getInfo();
                Map<String, String> metadata = instanceInfo.getMetadata();

                List<McpToolSpecification> mcpFileMetadatas = mcpToolSpecifications.stream().filter(mcpFileMetadata -> mcpFileMetadata.getType().equals("MCP_FILE")).collect(Collectors.toList());
                if(!CollectionUtils.isEmpty(mcpFileMetadatas)){
                    metadata.putIfAbsent(InstancePreRegisteredApplicationListener.MCP_FILE_KEY + "-0", StringCompressionUtil.compress(JsonUtils.toJSONString(mergeMetadata(mcpFileMetadatas))));
                    metadata.putIfAbsent(InstancePreRegisteredApplicationListener.MCP_FILE_SIZE_KEY, "1");
                }

                List<McpToolSpecification> mcpDiscoveryMetadatas = mcpToolSpecifications.stream().filter(mcpDiscoveryMetadata -> mcpDiscoveryMetadata.getType().equals("MCP_SERVER")).collect(Collectors.toList());
                if(!CollectionUtils.isEmpty(mcpDiscoveryMetadatas)){
                    metadata.putIfAbsent(InstancePreRegisteredApplicationListener.MCP_SERVER_KEY + "-0", MetadataUtils.encodeToBase64(JsonUtils.toJSONString(mergeMetadata(mcpDiscoveryMetadatas))));
                    metadata.putIfAbsent(InstancePreRegisteredApplicationListener.MCP_SERVER_SIZE_KEY, "1");
                }
            }
        }
        return bean;
    }

    private McpToolSpecification mergeMetadata(List<McpToolSpecification> mcpConfigurationMetadatas) {
        if(CollectionUtils.isEmpty(mcpConfigurationMetadatas)){
            return null;
        }

        if(mcpConfigurationMetadatas.size() == 1){
            return mcpConfigurationMetadatas.get(0);
        }

        McpToolSpecification mergedMetadata = new McpToolSpecification();
        mergedMetadata.setTools(new ArrayList<>());
        for(McpToolSpecification metadata : mcpConfigurationMetadatas){
            List<McpTool> items = metadata.getTools();
            if(!CollectionUtils.isEmpty(items)){
                mergedMetadata.getTools().addAll(items);
            }
        }
        return mergedMetadata;
    }
}