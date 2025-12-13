package io.xiaozhug.ai.mcp.registry.autoconfigure.eureka;

import io.xiaozhug.ai.mcp.registry.autoconfigure.McpRegistryCenterAutoConfiguration;
import io.xiaozhug.ai.mcp.registry.autoconfigure.McpRegistryCenterContextFactory;

import java.util.List;

/**
 * Eureka MCP 注册中心上下文工厂
 *
 * @author xiaozhug
 */
public class EurekaMcpMcpRegistryCenterContextFactory extends McpRegistryCenterContextFactory {

    public EurekaMcpMcpRegistryCenterContextFactory(List<McpRegistryCenterContextFactory.McpRegistryCenterSpecification> specifications) {
        super(specifications);
    }

    @Override
    public String getName() {
        return McpRegistryCenterAutoConfiguration.EurekaConfiguration.EUREKA_MCP_REGISTRY_CENTER_FACTORY_NAME;
    }
}