package io.xiaozhug.ai.mcp.metadata.file.expose.autoconfigure;

import io.xiaozhug.ai.mcp.common.metadata.McpToolSpecification;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * MCP 元数据暴露控制器
 *
 * @author xiaozhug
 */
@ResponseBody
@RequestMapping({"/v1/expose"})
public class McpMetadataExposeController {

    private final McpToolSpecification mcpToolSpecification;

    public McpMetadataExposeController(McpToolSpecification mcpToolSpecification) {
        this.mcpToolSpecification = mcpToolSpecification;
    }

    @GetMapping("/mcp-metadata")
    public McpToolSpecification exposeMcpMetadata(){
        return mcpToolSpecification;
    }
}
