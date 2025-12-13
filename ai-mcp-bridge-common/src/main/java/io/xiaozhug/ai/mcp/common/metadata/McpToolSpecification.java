package io.xiaozhug.ai.mcp.common.metadata;

import lombok.Data;

import java.util.List;

/**
 * MCP工具规范
 *
 * @author xiaozhug
 */
@Data
public class McpToolSpecification {

    private String type;
    private String serviceName;
    private String instanceId;
    private String url;
    private List<String> toolNames;
    private List<McpTool> tools;
}
