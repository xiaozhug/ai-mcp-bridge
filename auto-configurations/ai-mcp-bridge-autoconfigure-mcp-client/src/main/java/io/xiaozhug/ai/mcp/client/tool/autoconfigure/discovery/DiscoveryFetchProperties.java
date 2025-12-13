package io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 发现抓取配置属性
 *
 * @author xiaozhug
 */
@Data
@ConfigurationProperties(DiscoveryFetchProperties.CONFIG_PREFIX)
public class DiscoveryFetchProperties {

    public static final String CONFIG_PREFIX = "spring.cloud.discovery.fetch";

    private boolean enabled;

    /**
     * 抓取服务名,为空则抓取所有服务
     */
    private List<String> services;

    /**
     * 刷新在线实例间隔（秒）
     */
    private int refreshInterval = 20;
}
