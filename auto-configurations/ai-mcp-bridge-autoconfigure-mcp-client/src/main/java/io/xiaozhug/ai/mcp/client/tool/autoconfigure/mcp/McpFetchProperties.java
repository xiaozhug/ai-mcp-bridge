package io.xiaozhug.ai.mcp.client.tool.autoconfigure.mcp;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MCP获取配置属性
 *
 * @author xiaozhug
 */
@Data
@ConfigurationProperties(McpFetchProperties.CONFIG_PREFIX)
public class McpFetchProperties {

    public static final String CONFIG_PREFIX = "spring.ai.mcp.fetch.client";

    private boolean enabled;
}
