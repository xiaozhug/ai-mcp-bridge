package io.xiaozhug.ai.mcp.client.tool.autoconfigure.http;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * HTTP获取配置属性
 *
 * @author xiaozhug
 */
@Data
@ConfigurationProperties(HttpFetchProperties.CONFIG_PREFIX)
public class HttpFetchProperties {

    public static final String CONFIG_PREFIX = "spring.ai.mcp.fetch.http";

    private boolean enabled;

    private Map<String, String> connections;

    private Long timeoutDuration = 10L;
}
