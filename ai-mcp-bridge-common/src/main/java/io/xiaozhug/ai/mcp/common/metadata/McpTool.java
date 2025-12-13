package io.xiaozhug.ai.mcp.common.metadata;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * MCP工具
 *
 * @author xiaozhug
 */
@Setter
@Getter
public class McpTool {

    private String serviceName;
    private String scheme;
    private String protocol;
    private String name;
    private String description;
    private String inputSchema;

    //toolName_methodNameDescription
    private String md5;
}
