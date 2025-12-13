package io.xiaozhug.ai.mcp.client.tool.autoconfigure.util;

import io.xiaozhug.ai.mcp.common.metadata.McpTool;
import io.xiaozhug.ai.mcp.common.metadata.RequestTemplateInfo;


/**
 * 工具类
 *
 * @author xiaozhug
 */
public class ToolUtils {


    public static String getToolName(String serviceName, McpTool tool){
        return getToolName(serviceName, tool, RequestTemplateUtils.extractRequestTemplateInfo(tool.getInputSchema()));
    }

    public static String getToolName(String serviceName, McpTool tool, RequestTemplateInfo requestTemplateInfo){
        String toolName = tool.getName();
        if(requestTemplateInfo != null){
            return serviceName + "_" + toolName + "_" + requestTemplateInfo.getUrl().replaceAll("/","_");
        }

        return toolName;
    }
}
