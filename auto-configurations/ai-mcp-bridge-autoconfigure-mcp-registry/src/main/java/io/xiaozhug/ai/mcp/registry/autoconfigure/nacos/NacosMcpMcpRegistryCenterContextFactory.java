package io.xiaozhug.ai.mcp.registry.autoconfigure.nacos;

import io.xiaozhug.ai.mcp.registry.autoconfigure.McpRegistryCenterAutoConfiguration;
import io.xiaozhug.ai.mcp.registry.autoconfigure.McpRegistryCenterContextFactory;

import java.util.List;

/**
 * Nacos MCP 注册中心上下文工厂
 *
 * @author xiaozhug
 */
public class NacosMcpMcpRegistryCenterContextFactory extends McpRegistryCenterContextFactory {

    public NacosMcpMcpRegistryCenterContextFactory(List<McpRegistryCenterSpecification> specifications) {
        super(specifications);
    }

    @Override
    public String getName() {
        return McpRegistryCenterAutoConfiguration.NacosConfiguration.NACOS_MCP_REGISTRY_CENTER_FACTORY_NAME;
    }
}