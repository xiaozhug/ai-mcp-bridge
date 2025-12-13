package io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery;

import io.xiaozhug.ai.mcp.client.tool.autoconfigure.client.rest.RestMcpClientTransport;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.util.RequestTemplateUtils;
import io.xiaozhug.ai.mcp.common.metadata.McpTool;
import io.xiaozhug.ai.mcp.common.metadata.RequestTemplateInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map;

/**
 * HTTP客户端发现处理器
 *
 * @author xiaozhug
 */
@Slf4j
public class HttpClientDiscoveryProcessor extends DiscoveryMcpServerProcessor{

    private final RestMcpClientTransport restMcpClientTransport;

    public HttpClientDiscoveryProcessor(RestMcpClientTransport restMcpClientTransport) {
        this.restMcpClientTransport = restMcpClientTransport;
    }

    @Override
    public String process(String toolName, List<McpTool> tools, Map<String, Object> args, @Nullable Map<String, Object> context) {
        log.info("Http Discovery Calling tool {} ", toolName);

        McpTool tool = tools.get(0);

        RequestTemplateInfo info = RequestTemplateUtils.extractRequestTemplateInfo(tool.getInputSchema());

        return restMcpClientTransport.call(tool.getScheme() + "://" + tool.getServiceName(), info, args, context);
    }

    @Override
    public String type() {
        return "http";
    }
}
