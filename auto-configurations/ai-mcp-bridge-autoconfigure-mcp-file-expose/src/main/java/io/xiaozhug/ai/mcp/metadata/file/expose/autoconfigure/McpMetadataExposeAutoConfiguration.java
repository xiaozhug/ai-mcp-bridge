package io.xiaozhug.ai.mcp.metadata.file.expose.autoconfigure;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.xiaozhug.ai.mcp.common.metadata.McpMetadata;
import io.xiaozhug.ai.mcp.common.metadata.McpToolSpecification;
import io.xiaozhug.ai.mcp.common.metadata.McpTool;
import io.xiaozhug.ai.mcp.common.metadata.McpMetadataItem;
import io.xiaozhug.ai.mcp.common.util.InputSchemaUtils;
import io.xiaozhug.ai.mcp.common.util.JsonUtils;
import io.xiaozhug.ai.mcp.common.util.MD5Utils;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.xiaozhug.ai.mcp.common.metadata.McpMetadataCollector.fetchConfigurationMetadata;

/**
 * MCP 元数据暴露自动配置
 *
 * @author xiaozhug
 */
@Configuration
public class McpMetadataExposeAutoConfiguration {

    @Bean
    public McpToolSpecification mcpFileConfigurationMetadata(){
        return convertToExposeMetadata(fetchConfigurationMetadata());
    }

    @SneakyThrows
    private McpToolSpecification convertToExposeMetadata(McpMetadata mcpMetadata){
        McpToolSpecification mcpToolSpecification = new McpToolSpecification();
        List<McpMetadataItem> mcpMetadataItems = mcpMetadata.getItems();

        if(mcpMetadataItems != null && !mcpMetadataItems.isEmpty()){
            MD5Utils md5Utils = new MD5Utils();
            List<McpTool> tools = new ArrayList<>();
            mcpToolSpecification.setTools(tools);
            mcpToolSpecification.setType("MCP_FILE");

            for(McpMetadataItem item : mcpMetadataItems){
                Class<?> clazz = Class.forName(item.getClassName());
                List<McpMetadataItem.Param> params = item.getParams();
                Class<?>[] parameterTypes = new Class<?>[0];
                if (!CollectionUtils.isEmpty(params)) {
                    parameterTypes = params.stream().map(param -> {
                        try {
                            return getClassForName(param.getParamType());
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }).toArray(Class[]::new);
                }

                Method method = clazz.getDeclaredMethod(item.getMethodName(), parameterTypes);
                String inputSchema = JsonSchemaGenerator.generateForMethodInput(method, JsonSchemaGenerator.SchemaOption.ALLOW_ADDITIONAL_PROPERTIES_BY_DEFAULT);
                ObjectNode inputSchemaNodes = JsonUtils.fromJson(inputSchema, ObjectNode.class);
                JsonNode defsJsonNode = inputSchemaNodes.get("$defs");
                if(defsJsonNode == null){
                    inputSchemaNodes.put("$defs", "{}");
                    defsJsonNode = inputSchemaNodes.get("$defs");
                }

                Map<String, Object> map = JsonUtils.fromJson(defsJsonNode.asText(), new TypeReference<Map<String, Object>>() {
                });
                map.put("requestTemplateInfo", item.getRequestTemplateInfo());
                inputSchemaNodes.set("$defs", JsonUtils.valueToTree(map));

                InputSchemaUtils.fillInputSchema(params, inputSchemaNodes);

                McpTool tool = new McpTool();
                tool.setName(item.getMethodName());
                tool.setDescription(item.getMethodNameDescription());
                tool.setInputSchema(inputSchemaNodes.toString());
                tool.setProtocol("http");
                tool.setMd5(md5Utils.getMd5(tool.getName() + " " + item.getMethodNameDescription()));
                mcpToolSpecification.getTools().add(tool);
            }
        }
        return mcpToolSpecification;
    }

    private Class<?> getClassForName(String className) throws ClassNotFoundException {
        switch (className) {
            case "int":
                return int.class;
            case "long":
                return long.class;
            case "double":
                return double.class;
            case "float":
                return float.class;
            case "boolean":
                return boolean.class;
            case "char":
                return char.class;
            case "byte":
                return byte.class;
            case "short":
                return short.class;
            case "void":
                return void.class;
            default:
                return Class.forName(className);
        }
    }

    @Bean
    public McpMetadataExposeController mcpMetadataExposeController(){
        return new McpMetadataExposeController(mcpFileConfigurationMetadata());
    }
}
