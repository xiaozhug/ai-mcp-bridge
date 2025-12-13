package io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery;

import lombok.Data;

import java.time.Duration;

/**
 * MCP客户端配置属性
 *
 * @author xiaozhug
 */
@Data
public class McpClientProperties {

    private String name;
    private String version;
    private Duration requestTimeout;
    private boolean initialized;

}
