package io.xiaozhug.ai.mcp.registry.autoconfigure.zookeeper;

import io.xiaozhug.ai.mcp.registry.autoconfigure.McpRegistryCenterAutoConfiguration;
import io.xiaozhug.ai.mcp.registry.autoconfigure.McpRegistryCenterContextFactory;

import java.util.List;

/**
 * Zookeeper MCP 注册中心上下文工厂
 *
 * @author xiaozhug
 */
public class ZookeeperMcpMcpRegistryCenterContextFactory extends McpRegistryCenterContextFactory {

    public ZookeeperMcpMcpRegistryCenterContextFactory(List<McpRegistryCenterSpecification> specifications) {
        super(specifications);
    }

    @Override
    public String getName() {
        return McpRegistryCenterAutoConfiguration.ZookeeperConfiguration.ZOOKEEPER_MCP_REGISTRY_CENTER_FACTORY_NAME;
    }
}