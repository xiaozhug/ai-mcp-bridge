package io.xiaozhug.ai.mcp.adapter.tool.autoconfigure;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.xiaozhug.ai.mcp.common.metadata.McpMetadata;
import io.xiaozhug.ai.mcp.common.util.InputSchemaUtils;
import io.xiaozhug.ai.mcp.common.metadata.McpMetadataItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.method.MethodToolCallback;
import org.springframework.ai.util.json.schema.JsonSchemaGenerator;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.xiaozhug.ai.mcp.common.metadata.McpMetadataCollector.fetchConfigurationMetadata;

/**
 * MCP元数据工具回调提供者
 *
 * @author xiaozhug
 */
@Slf4j
public class McpMetadataToolCallbackProvider implements ToolCallbackProvider, ApplicationContextAware {

    private ApplicationContext applicationContext;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ToolCallback[] getToolCallbacks() {
        McpMetadata mcpMetadata = fetchConfigurationMetadata();
        if(Objects.nonNull(mcpMetadata)){
            ClassLoader classLoader = this.getClass().getClassLoader();
            List<McpMetadataItem> items = mcpMetadata.getItems();
            if(!CollectionUtils.isEmpty(items)){
                return items.stream().map(item -> {
                    try {
                        Class<?> clazz = classLoader.loadClass(item.getClassName());
                        Method method = getMethod(classLoader, clazz, item);
                        return createToolCallback(clazz, method, item);
                    } catch (ClassNotFoundException e) {
                        log.error("Failed to load class: " + item.getClassName(), e);
                    }

                    return null;
                }).filter(Objects::nonNull).toArray(ToolCallback[]::new);
            }
        }

        return new ToolCallback[0];
    }

    private Method getMethod(ClassLoader classLoader, Class<?> clazz, McpMetadataItem mcpMetadataItem){
        try {
            if(Objects.nonNull(clazz)){
                Method method = null;
                String methodName = mcpMetadataItem.getMethodName();
                List<McpMetadataItem.Param> params = mcpMetadataItem.getParams();
                if(!CollectionUtils.isEmpty(params)){
                    Class[] paramClassArray = mcpMetadataItem.getParams().stream().map(param -> {
                        try {
                            return classLoader.loadClass(param.getParamType());
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }).toArray(Class[]::new);
                    method = clazz.getDeclaredMethod(methodName, paramClassArray);
                } else {
                    method = clazz.getDeclaredMethod(methodName);
                }

                return method;
            }
        } catch (Exception e) {
        }

        return null;
    }

    private ToolCallback createToolCallback(Class<?> clazz, Method method, McpMetadataItem mcpMetadataItem) {
        try {
            if(Objects.nonNull(method)){
                String inputSchema = JsonSchemaGenerator.generateForMethodInput(method, JsonSchemaGenerator.SchemaOption.ALLOW_ADDITIONAL_PROPERTIES_BY_DEFAULT);
                ObjectNode inputSchemaNodes = objectMapper.readValue(inputSchema, ObjectNode.class);
                JsonNode defsJsonNode = inputSchemaNodes.get("$defs");
                if(defsJsonNode == null){
                    inputSchemaNodes.put("$defs", "{}");
                    defsJsonNode = inputSchemaNodes.get("$defs");
                }

                Map<String, Object> map = toMap(defsJsonNode.asText());
                map.put("requestTemplateInfo", mcpMetadataItem.getRequestTemplateInfo());
                inputSchemaNodes.set("$defs", objectMapper.valueToTree(map));

                List<McpMetadataItem.Param> params = mcpMetadataItem.getParams();
                InputSchemaUtils.fillInputSchema(params, inputSchemaNodes);

                return MethodToolCallback.builder().toolDefinition(
                        ToolDefinition.builder()
                                .name(method.getName())
                                .description(mcpMetadataItem.getMethodNameDescription())
                                .inputSchema(inputSchemaNodes.toString())
                                .build()
                ).toolMethod(method).toolObject(applicationContext.getBean(clazz)).build();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create ToolCallback for method: " + method.getName(), e);
        }

        return null;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private Map<String, Object> toMap(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON: " + json, e);
        }
    }

}
