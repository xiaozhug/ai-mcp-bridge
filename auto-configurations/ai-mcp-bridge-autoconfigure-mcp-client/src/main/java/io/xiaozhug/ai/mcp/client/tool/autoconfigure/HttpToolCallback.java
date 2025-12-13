package io.xiaozhug.ai.mcp.client.tool.autoconfigure;

import io.xiaozhug.ai.mcp.client.tool.autoconfigure.client.rest.RestMcpClientTransport;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.util.RequestTemplateUtils;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.util.ToolUtils;
import io.xiaozhug.ai.mcp.common.metadata.McpTool;
import io.xiaozhug.ai.mcp.common.util.JsonUtils;
import io.xiaozhug.ai.mcp.common.metadata.RequestTemplateInfo;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.mcp.client.autoconfigure.properties.McpSseClientProperties;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.DefaultToolDefinition;
import org.springframework.ai.tool.definition.ToolDefinition;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xiaozhug
 */
public class HttpToolCallback implements ToolCallback {

    protected RestMcpClientTransport restMcpClientTransport;
    protected ToolDefinition toolDefinition;
    protected RequestTemplateInfo info;
    protected String baseUrl;

    public HttpToolCallback() {
    }

    public HttpToolCallback(RestMcpClientTransport restMcpClientTransport, McpSchema.Implementation clentInfo, McpSseClientProperties sseProperties, McpSchema.Tool tool) {
        this.restMcpClientTransport = restMcpClientTransport;

        Map<String, Object> defs = tool.inputSchema().defs();
        Object requestTemplateInfoObj = defs.get("requestTemplateInfo");

        this.toolDefinition = DefaultToolDefinition.builder()
                .name(tool.name())
                .description(tool.description())
                .inputSchema(ModelOptionsUtils.toJsonString(tool.inputSchema()))
                .build();
        this.info = JsonUtils.fromJson(JsonUtils.toJSONString(requestTemplateInfoObj), RequestTemplateInfo.class);
        String connectionKey = clentInfo.name().split(" - ")[1];
        this.baseUrl = sseProperties.getConnections().get(connectionKey).url();
    }

    public HttpToolCallback(RestMcpClientTransport restMcpClientTransport, String serviceName, McpTool tool, String baseUrl){
        this.restMcpClientTransport = restMcpClientTransport;
        this.info = RequestTemplateUtils.extractRequestTemplateInfo(tool.getInputSchema());
        this.toolDefinition = DefaultToolDefinition.builder()
                .name(ToolUtils.getToolName(serviceName, tool, this.info))
                .description(tool.getDescription())
                .inputSchema(tool.getInputSchema())
                .build();
        this.baseUrl = baseUrl;
    }

    @Override
    public ToolDefinition getToolDefinition() {
        return toolDefinition;
    }

    @Override
    public String call(String input) {
        return call(input, new ToolContext(new HashMap<>()));
    }

    @Override
    public String call(String input, ToolContext toolContext) {
        // 参数验证
        if (this.toolDefinition == null) {
            throw new IllegalStateException("Tool definition is null");
        }

        // input解析
        ToolCallback.logger.info("[Mcp client http protocol call] input string: {}", input);

        return restMcpClientTransport.call(baseUrl, info, ModelOptionsUtils.jsonToMap(input), toolContext != null ? toolContext.getContext() : null);
    }

}
