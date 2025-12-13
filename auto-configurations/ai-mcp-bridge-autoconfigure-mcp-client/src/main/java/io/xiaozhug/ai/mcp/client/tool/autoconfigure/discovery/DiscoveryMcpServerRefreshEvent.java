package io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery;

import org.springframework.context.ApplicationEvent;

/**
 * 发现MCP服务刷新事件
 *
 * @author xiaozhug
 */
public class DiscoveryMcpServerRefreshEvent extends ApplicationEvent {

    public DiscoveryMcpServerRefreshEvent(DiscoveryMcpServerRefreshEventSource source) {
        super(source);
    }

    @Override
    public DiscoveryMcpServerRefreshEventSource getSource() {
        return (DiscoveryMcpServerRefreshEventSource) super.getSource();
    }
}
