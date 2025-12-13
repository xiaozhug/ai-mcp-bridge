package io.xiaozhug.demo.metadata.apt.extension;

import io.xiaozhug.ai.mcp.apt.extension.LLMServiceExtension;

public class CustomLLMServiceExtension implements LLMServiceExtension {


    @Override
    public int getOrder() {
        return 0;
    }
}
