package io.xiaozhug.ai.mcp.registry.autoconfigure;

import io.xiaozhug.ai.mcp.common.metadata.McpToolSpecification;
import io.xiaozhug.ai.mcp.common.metadata.McpTool;
import io.xiaozhug.ai.mcp.common.util.JsonUtils;
import io.xiaozhug.ai.mcp.common.util.StringCompressionUtil;
import lombok.SneakyThrows;
import org.springframework.beans.BeansException;
import org.springframework.cloud.client.discovery.event.InstancePreRegisteredEvent;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 实例预注册应用程序监听器
 *
 * @author xiaozhug
 */
public class InstancePreRegisteredApplicationListener implements ApplicationListener<InstancePreRegisteredEvent>, ApplicationContextAware {

    public final static String MCP_FILE_KEY = "mcp-file";
    public final static String MCP_FILE_SIZE_KEY = "mcp-file-size";
    public final static String MCP_SERVER_KEY = "mcp-server";
    public final static String MCP_SERVER_SIZE_KEY = "mcp-server-size";
    private final List<McpToolSpecification> mcpConfigurationMetadatas;

    private ApplicationContext applicationContext;

    public InstancePreRegisteredApplicationListener(List<McpToolSpecification> mcpConfigurationMetadatas) {
        this.mcpConfigurationMetadatas = mcpConfigurationMetadatas;
    }

    @Override
    @SneakyThrows
    public void onApplicationEvent(InstancePreRegisteredEvent event) {
        Class<?> clazz = event.getSource().getClass();
        Field contextField = ReflectionUtils.findField(clazz, "context");;
        contextField.setAccessible(true);
        ApplicationContext context = (ApplicationContext) contextField.get(event.getSource());
        if(applicationContext == context){
            Registration registration = event.getRegistration();

            List<McpToolSpecification> mcpFileMetadatas = mcpConfigurationMetadatas.stream().filter(mcpFileMetadata -> mcpFileMetadata.getType().equals("MCP_FILE")).collect(Collectors.toList());
            if(!CollectionUtils.isEmpty(mcpFileMetadatas)){
                putMetadata(MCP_FILE_KEY, registration, mcpFileMetadatas);
//                metadata.putIfAbsent(MCP_FILE_KEY, MetadataUtils.encodeToBase64(JsonUtils.toJSONString(mergeMetadata(mcpFileMetadatas))));
            }

            List<McpToolSpecification> mcpDiscoveryMetadatas = mcpConfigurationMetadatas.stream().filter(mcpDiscoveryMetadata -> mcpDiscoveryMetadata.getType().equals("MCP_SERVER")).collect(Collectors.toList());
            if(!CollectionUtils.isEmpty(mcpDiscoveryMetadatas)){
                putMetadata(MCP_SERVER_KEY, registration, mcpDiscoveryMetadatas);
//                metadata.putIfAbsent(MCP_SERVER_KEY, MetadataUtils.encodeToBase64(JsonUtils.toJSONString(mergeMetadata(mcpDiscoveryMetadatas))));
            }
        }
    }

    @SneakyThrows
    private void putMetadata(String key, Registration registration, List<McpToolSpecification> metadatas) {
        String registrationName = registration.getClass().getName();
        //针对zookeeper，初始化ServiceInstance<ZookeeperInstance> serviceInstance
        if(registrationName.equals("org.springframework.cloud.zookeeper.serviceregistry.ServiceInstanceRegistration")){
            Method method = ReflectionUtils.findMethod(registration.getClass(), "setPort", int.class);
            method.setAccessible(true);
            method.invoke(registration, 0);
        }

        Map<String, String> metadata = registration.getMetadata();
        String value = StringCompressionUtil.compress(JsonUtils.toJSONString(mergeMetadata(metadatas)));

        int length = value.length();
        int chunkSize = length;
        if(registrationName.equals("org.springframework.cloud.consul.serviceregistry.ConsulAutoRegistration")){
            chunkSize = 512;
        } else if(registrationName.equals("com.alibaba.cloud.nacos.registry.NacosRegistration")){
            chunkSize = 1024;
        }

        int splitSize = length / chunkSize + (length % chunkSize == 0 ? 0 : 1);
        metadata.putIfAbsent(key + "-size", String.valueOf(splitSize));
        for(int i = 0; i < splitSize; i++){
            int startIndex = i * chunkSize;
            int endIndex = Math.min((i + 1) * chunkSize, length);
            String partValue = value.substring(startIndex, endIndex);
            metadata.putIfAbsent(key + "-" + i, partValue);
        }
    }

    private McpToolSpecification mergeMetadata(List<McpToolSpecification> mcpConfigurationMetadatas) {
        if(CollectionUtils.isEmpty(mcpConfigurationMetadatas)){
            return null;
        }

        if(mcpConfigurationMetadatas.size() == 1){
            return mcpConfigurationMetadatas.get(0);
        }

        McpToolSpecification mergedMetadata = new McpToolSpecification();
        for(McpToolSpecification metadata : mcpConfigurationMetadatas){
            List<McpTool> tools = metadata.getTools();
            if(!CollectionUtils.isEmpty(tools)){
                mergedMetadata.getTools().addAll(tools);
            }
        }
        return mergedMetadata;
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}